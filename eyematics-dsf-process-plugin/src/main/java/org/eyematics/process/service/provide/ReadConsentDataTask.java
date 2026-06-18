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
import org.eyematics.process.utils.consent.ConsentResourceValidator;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class ReadConsentDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadConsentDataTask.class);
    private final FhirClientFactory fhirClientFactory;
    private final int fhirStoreResourcePageSize;
    private final ConsentResourceValidator consentResourceValidator;

    public ReadConsentDataTask(ProcessPluginApi api,
                               DataSetStatusGenerator dataSetStatusGenerator,
                               FhirClientFactory fhirClientFactory,
                               int fhirStoreResourcePageSize,
                               ConsentResourceValidator consentResourceValidator) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
        this.fhirStoreResourcePageSize = fhirStoreResourcePageSize;
        this.consentResourceValidator = consentResourceValidator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
        Objects.requireNonNull(this.consentResourceValidator, "consentResourceValidator");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Reading Broad Consent data from local FHIR repository is initiated.");
        try {
            EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);

            List<Bundle.BundleEntryComponent> patientConsentEntries = patients.getEntry()
                    .stream()
                    .parallel()
                    .map(p -> this.requestAndProcessConsent(p, fhirClient, variables))
                    .filter(Objects::nonNull)
                    .toList();

            if (patientConsentEntries.isEmpty()) throw new Exception("No valid Broad Consent(s) found");

            patients.setEntry(patientConsentEntries);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            this.handleException(exception, variables);
        }
    }

    private void handleException(Exception exception, Variables variables) {
        String errorMessage = exception.getMessage();
        logger.error("Could not read Broad Consent data from FHIR repository: {}.", errorMessage);
        this.handleTaskError(EyeMaticsGenericStatus.CONSENT_READ_FAILURE, variables, errorMessage);
    }

    private Bundle.BundleEntryComponent requestAndProcessConsent(Bundle.BundleEntryComponent patient,
                                                                 EyeMaticsFhirClient fhirClient,
                                                                 Variables variables) {
        String consentsQuery = String.format("/%s/Consent?_count=%s",
                patient.getResource().getIdElement().getIdPart(),
                this.fhirStoreResourcePageSize);
        try {
            Bundle consents = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Patient", consentsQuery);
            List<Consent> consentsList = consents.getEntry()
                    .stream()
                    .parallel()
                    .filter(c -> c.getResource()
                            .getResourceType().equals(ResourceType.Consent))
                    .map(c -> (Consent) c.getResource())
                    .toList();
            Optional<Consent> consentOptional = this.consentResourceValidator
                    .getPermittedTimeIntervalls(consentsList,
                            List.of(EyeMaticsConstants.MII_IG_MODUL_CONSENT_PROVISION_CODE_MDAT,
                                    EyeMaticsConstants.MII_IG_MODUL_CONSENT_PROVISION_CODE_MDAT_RETRO));
            if (consentOptional.isPresent()) {
                if (!consentOptional.get().getProvision().getProvision().isEmpty()) {
                    Bundle patientConsentBundle = new Bundle();
                    patientConsentBundle.setType(Bundle.BundleType.BATCH);
                    patientConsentBundle.addEntry().setResource(patient.getResource());
                    patientConsentBundle.addEntry().setResource(consentOptional.get());
                    return new Bundle.BundleEntryComponent().setResource(patientConsentBundle);
                }
            } else {
                return null;
            }
        } catch (Exception exception) {
            this.handleException(exception, variables);
        }
        return null;
    }
}
