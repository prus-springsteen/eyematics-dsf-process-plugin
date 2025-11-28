package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.bpe.EyeMaticsDataBundleRetriever;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;


public class CheckBroadConsentTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CheckBroadConsentTask.class);
    private final FhirClientFactory fhirClientFactory;

    public CheckBroadConsentTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator,
                                 FhirClientFactory fhirClientFactory) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Reading Broad Consent data from local FHIR repository is initiated");
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
            String consentsQuery = String.format("_profile=%s", EyeMaticsConstants.MII_IG_MODUL_CONSENT_PROFILE);
            Bundle consents = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Consent", consentsQuery);
            List<Bundle.BundleEntryComponent> filteredPatients = consents.getEntry()
                    .stream()
                    .map(this::transformToConsent)
                    .filter(this::isValidBroadConsent)
                    .map(c -> this.getPatient(c, patients))
                    .toList();
            patients.getEntry().clear();
            patients.getEntry().addAll(filteredPatients);
            if (patients.getEntry().isEmpty()) {
                throw new Exception("No valid Broad Consent(s) found");
            }
            delegateExecution.setVariable(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read Broad Consent data from FHIR repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.CONSENT_CHECK_FAILURE, variables, errorMessage);
        }
    }

    private Consent transformToConsent(Bundle.BundleEntryComponent consent) {
        if (consent.hasResource() && consent.getResource() instanceof Consent) {
            return (Consent) consent.getResource();
        }
        return null;
    }

    private boolean isValidBroadConsent(Consent consent) {
        if (consent == null) return false;
        if (!consent.getStatus().equals(Consent.ConsentState.ACTIVE)) return false;
        if (consent.getMeta().getProfile().size() != 1) return false;
        if (!hasValidScope(consent)) return false;
        if (!hasValidCategory(consent)) return false;
        return hasValidProvision(consent);
    }

    private boolean hasValidScope(Consent consent) {
        if (consent.getScope() == null) return false;
        ConsentScope consentScope = ConsentScope.fromCode(ConsentScope.RESEARCH.toCode());
        return consent.getScope().hasCoding(consentScope.getSystem(), consentScope.toCode());
    }

    private boolean hasValidCategory(Consent consent) {
        if (consent.getCategory() == null) return false;
        if (consent.getCategory().size() != 2) return false;
        boolean hasPrivacyAcknowledgementDocument = false;
        boolean hasMIIBroadConsent = false;
        for (CodeableConcept cc : consent.getCategory()) {
            if(cc.hasCoding(EyeMaticsConstants.MII_IG_MODUL_CONSENT_CATEGORY_LOINC_SYSTEM,
                    EyeMaticsConstants.MII_IG_MODUL_CONSENT_CATEGORY_LOINC_CODE)) {
                hasPrivacyAcknowledgementDocument = true;
            }
            if(cc.hasCoding(EyeMaticsConstants.MII_IG_MODUL_CONSENT_CATEGORY_MII_SYSTEM,
                    EyeMaticsConstants.MII_IG_MODUL_CONSENT_CATEGORY_MII_CODE)) {
                hasMIIBroadConsent = true;
            }
        }
        return hasPrivacyAcknowledgementDocument && hasMIIBroadConsent;
    }

    private boolean hasValidProvision(Consent consent) {
        Consent.provisionComponent provisionParentComponent = consent.getProvision();
        if (provisionParentComponent == null) return false;
        if (!hasValidPeriod(provisionParentComponent)) return false;
        if (!provisionParentComponent.hasProvision()) return false;
        for (Consent.provisionComponent provisionChildComponent : provisionParentComponent.getProvision()) {
            if(hasValidChildProvision(provisionChildComponent)) return true;
        }
        return false;
    }

    private boolean hasValidPeriod(Consent.provisionComponent provisionComponent) {
        Date actualDate = new Date();
        if (!provisionComponent.hasPeriod()) return false;
        if (!provisionComponent.getPeriod().hasStart()) return false;
        if (!provisionComponent.getPeriod().hasEnd()) return false;
        if (provisionComponent.getPeriod().getStart().after(actualDate)) return false;
        return provisionComponent.getPeriod().getEnd().after(actualDate);
    }

    private boolean hasValidChildProvision(Consent.provisionComponent provisionChildComponent) {
        if (provisionChildComponent == null) return false;
        if (!provisionChildComponent.hasType()) return false;
        if (!provisionChildComponent.getType().equals(Consent.ConsentProvisionType.PERMIT)) return false;
        if (!hasValidPeriod(provisionChildComponent)) return false;
        if (!provisionChildComponent.hasCode()) return false;
        if (provisionChildComponent.getCode().isEmpty()) return false;
        CodeableConcept codeableConcept = provisionChildComponent.getCode().get(0);
        return codeableConcept.hasCoding(EyeMaticsConstants.MII_IG_MODUL_CONSENT_PROVISION_SYSTEM,
                EyeMaticsConstants.MII_IG_MODUL_CONSENT_PROVISION_CODE);
    }

    private Bundle.BundleEntryComponent getPatient(Consent consent, Bundle patients) {
        if (consent == null) return null;
        if (!consent.getPatient().hasReference()) return null;
        String patientId = this.extractPatientId(consent);
        Bundle.BundleEntryComponent patient = patients.getEntry()
                .stream()
                .filter(be -> be.getResource().getIdElement().getIdPart().equals(patientId))
                .toList()
                .get(0);
        patients.getEntry().remove(patient);
        return patient;
    }

    private String extractPatientId(Consent consent) {
        if (consent == null) return null;
        if (!consent.getPatient().hasReference()) return null;
        String patientReference = consent.getPatient().getReference();
        return patientReference.substring(patientReference.lastIndexOf("/") + 1);
    }
}
