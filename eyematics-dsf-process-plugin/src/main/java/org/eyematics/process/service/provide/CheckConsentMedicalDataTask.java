package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CheckConsentMedicalDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CheckConsentMedicalDataTask.class);

    public CheckConsentMedicalDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Checking medical data if consent is given and permitted.");
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);

            List<Bundle.BundleEntryComponent> patiensConsentedResources = patients.getEntry()
                    .stream()
                    .filter(e -> e.getResource().getResourceType().equals(ResourceType.Bundle))
                    .map(e -> (Bundle) e.getResource())
                    .filter(b -> b.getEntry().size() == 2)
                    .filter(b -> b.getEntry().get(0).getResource()
                            .getResourceType().equals(ResourceType.Patient))
                    .filter(b -> b.getEntry().get(1).getResource()
                            .getResourceType().equals(ResourceType.Consent))
                    .map(b -> {
                        try {
                            return this.checkEntry(b, variables);
                        } catch (Exception exception) {
                            this.handleException(exception, variables);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (patiensConsentedResources.isEmpty()) throw new Exception("No valid permitted Resources found");

            patients.setEntry(patiensConsentedResources);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);

        } catch (Exception exception) {
            this.handleException(exception, variables);
        }
    }

    private void handleException(Exception exception, Variables variables) {
        String errorMessage = exception.getMessage();
        logger.error("Could not check the given Consents and FHIR Resources properly: {}.", errorMessage);
        this.handleTaskError(EyeMaticsGenericStatus.CONSENT_CHECK_FAILURE, variables, errorMessage);
    }

    private Bundle.BundleEntryComponent checkEntry(Bundle bundle, Variables variables) throws Exception {
        try {
            Patient p = (Patient) bundle.getEntry().get(0).getResource();
            Consent c = (Consent) bundle.getEntry().get(1).getResource();
            String patientId = p.getIdElement().getIdPart();

            Bundle observations = this.getResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET, patientId);
            Bundle observationsConsented = this.getConsentObservationsBundle(observations, c);

            Bundle diagnosticReports = this.getResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET, patientId);
            Bundle diagnosticReportsConsented = this.getConsentDiagnosticReportsBundle(diagnosticReports, c);

            Bundle medicationAdministrations = this.getResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, patientId);

            Bundle medicationAdministrationsConsented =
                    this.getConsentedMedicationAdministrationBundle(medicationAdministrations, c);

            Bundle medicationRequests = this.getResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, patientId);
            Bundle medicationRequestsConsented = this.getConsentedMedicationRequestBundle(medicationRequests, c);

            if (!observationsConsented.getEntry().isEmpty()
                    && !diagnosticReportsConsented.getEntry().isEmpty()
                    && !medicationAdministrationsConsented.getEntry().isEmpty()
                    && !medicationRequestsConsented.getEntry().isEmpty()) {
                Bundle patientBundle = new Bundle().setType(Bundle.BundleType.BATCH);
                patientBundle.addEntry().setResource(p);
                this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                        patientId, observationsConsented);
                this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET,
                        patientId, diagnosticReportsConsented);
                this.setResource(variables,
                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, patientId,
                        medicationAdministrationsConsented);
                this.setResource(variables,
                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, patientId,
                        medicationRequestsConsented);
                return new Bundle.BundleEntryComponent().setResource(p);
            } else {
                this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                        patientId, null);
                this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET,
                        patientId, null);
                this.setResource(variables,
                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, patientId,
                        null);
                this.setResource(variables,
                        ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, patientId, null);
            }
            return null;
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    private boolean isResourcePermitted(Date date, Consent consent) {
        int permits = consent.getProvision()
                .getProvision()
                .stream()
                .parallel()
                .filter(pc -> pc.getPeriod().getStartElement().getValue().before(date)
                        && pc.getPeriod().getEndElement().getValue().after(date))
                .toList()
                .size();
        return permits > 0;
    }

    private Bundle getConsentObservationsBundle(Bundle observationsBundle, Consent consent) {
        Bundle observationsConsented = new Bundle().setType(Bundle.BundleType.BATCH);
        if (observationsBundle == null) return observationsConsented;
        List<Bundle.BundleEntryComponent> oEntries = observationsBundle.getEntry()
                .stream()
                .parallel()
                .filter(e -> e.getResource().getResourceType().equals(ResourceType.Observation))
                .map(e -> (Observation) e.getResource())
                .filter(Observation::hasEffectiveDateTimeType)
                .filter(o -> this.isResourcePermitted(o.getEffectiveDateTimeType().getValue(), consent))
                .map(o -> new Bundle.BundleEntryComponent().setResource(o))
                .toList();
        observationsConsented.setEntry(oEntries);
        return observationsConsented;
    }

    private Bundle getConsentDiagnosticReportsBundle(Bundle diagnosticReportsBundle, Consent consent) {
        Bundle diagnosticReportsConsented = new Bundle().setType(Bundle.BundleType.BATCH);
        if (diagnosticReportsBundle == null) return diagnosticReportsConsented;
        List<Bundle.BundleEntryComponent> drEntries = diagnosticReportsBundle.getEntry()
                .stream()
                .parallel()
                .filter(e -> e.getResource().getResourceType().equals(ResourceType.DiagnosticReport))
                .map(e -> (DiagnosticReport) e.getResource())
                .filter(DiagnosticReport::hasEffectiveDateTimeType)
                .filter(dr -> this.isResourcePermitted(dr.getEffectiveDateTimeType().getValue(), consent))
                .map(dr -> new Bundle.BundleEntryComponent().setResource(dr))
                .toList();
        diagnosticReportsConsented.setEntry(drEntries);
        return diagnosticReportsConsented;
    }

    private Bundle getConsentedMedicationAdministrationBundle(Bundle medicationAdministrationBundle, Consent consent) {
        Bundle medicationAdministrationConsented = new Bundle().setType(Bundle.BundleType.BATCH);
        if (medicationAdministrationBundle == null) return medicationAdministrationConsented;
        List<Bundle.BundleEntryComponent> maEntries = medicationAdministrationBundle.getEntry()
                .stream()
                .parallel()
                .filter(e -> e.getResource().getResourceType()
                        .equals(ResourceType.MedicationAdministration))
                .map(e -> (MedicationAdministration) e.getResource())
                .filter(MedicationAdministration::hasEffectiveDateTimeType)
                .filter(o -> this.isResourcePermitted(o.getEffectiveDateTimeType().getValue(),
                        consent))
                .map(ma -> new Bundle.BundleEntryComponent().setResource(ma))
                .toList();
        medicationAdministrationConsented.setEntry(maEntries);
        return medicationAdministrationConsented;
    }

    private Bundle getConsentedMedicationRequestBundle(Bundle medicationRequestBundle, Consent consent) {
        Bundle medicationRequestConsented = new Bundle().setType(Bundle.BundleType.BATCH);
        if (medicationRequestBundle == null) return medicationRequestConsented;
        List<Bundle.BundleEntryComponent> mrEntries = medicationRequestBundle.getEntry()
                .stream()
                .parallel()
                .filter(e -> e.getResource().getResourceType()
                        .equals(ResourceType.MedicationRequest))
                .map(e -> (MedicationRequest) e.getResource())
                .filter(MedicationRequest::hasAuthoredOn)
                .filter(o -> this.isResourcePermitted(o.getAuthoredOn(), consent))
                .map(mr -> new Bundle.BundleEntryComponent().setResource(mr))
                .toList();
        medicationRequestConsented.setEntry(mrEntries);
        return medicationRequestConsented;
    }
}
