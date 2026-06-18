package org.eyematics.process.service.provide;

import java.util.Objects;
import java.util.stream.Collectors;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.bpe.EyeMaticsDataBundleRetriever;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.*;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadMedicalDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadMedicalDataTask.class);
    private final FhirClientFactory fhirClientFactory;
    private final int fhirStoreResourcePageSize;

    public ReadMedicalDataTask(ProcessPluginApi api,
                               DataSetStatusGenerator dataSetStatusGenerator,
                               FhirClientFactory fhirClientFactory,
                               int fhirStoreResourcePageSize) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
        this.fhirStoreResourcePageSize = fhirStoreResourcePageSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Reading local medical data from FHIR repository is initiated.");
        try {
            EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);

            MailSender.sendInfo(this.api.getMailService(),
                    variables.getStartTask(),
                    "-",
                    "Data requested",
                    "there is a new MDAT request which is processed.");

            this.processMeasureReport(fhirClient, variables);
            this.processMedication(fhirClient, variables);

            patients.getEntry()
                    .stream()
                    .filter(e -> e.getResource().getResourceType().equals(ResourceType.Bundle))
                    .map(e -> (Bundle) e.getResource())
                    .filter(Bundle::hasEntry)
                    .map(Bundle::getEntry)
                    .filter(e -> e.size() == 2)
                    .filter(e -> e.get(0).getResource()
                            .getResourceType().equals(ResourceType.Patient))
                    .map(be -> (Patient) be.get(0).getResource())
                    .map(Patient::getIdElement)
                    .map(IdType::getIdPart)
                    .forEach(p -> {
                        try {
                            this.processObservations(fhirClient, p, variables);
                            this.processDiagnosticReports(fhirClient, p, variables);
                            this.processMedicationAdministration(fhirClient, p, variables);
                            this.processMedicationRequest(fhirClient, p, variables);
                        } catch (Exception exception) {
                            this.handleException(exception, variables);
                        }
                    });

        } catch (Exception exception) {
            this.handleException(exception, variables);
        }
    }

    private void handleException(Exception exception, Variables variables) {
        String errorMessage = exception.getMessage();
        logger.error("Could not read EyeMatics data from FHIR repository: {}.", errorMessage);
        this.handleTaskError(EyeMaticsGenericStatus.DATA_READ_FAILURE, variables, errorMessage);
    }

    private void processMeasureReport(EyeMaticsFhirClient fhirClient,
                                      Variables variables) throws Exception {
        String measureReportQuery = String.format("?_profile=%s&_count=%s",
                EyeMaticsConstants.EYEMATICS_IVI_MEASURE_REPORT_PROFILE, this.fhirStoreResourcePageSize);
        Bundle measureReports = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "MeasureReport",
                measureReportQuery);
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEASURE_REPORT_DATA_SET, measureReports);
    }

    private void processMedication(EyeMaticsFhirClient fhirClient,
                                   Variables variables) throws Exception {
        String medicationQuery = String.format("?_profile=%s%s&_count=%s",
                EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_PROFILE, this.fhirStoreResourcePageSize);
        Bundle medications = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "Medication",
                medicationQuery);
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET, medications);
    }

    private void processObservations(EyeMaticsFhirClient fhirClient,
                                     String localPatientId,
                                     Variables variables) throws Exception {
        String observationQuery = String.format("/%s/Observation?_profile=%s&_count=%s",
                localPatientId,
                EyeMaticsConstants.EYEMATICS_CORE_DATASET_OBSERVATION_PROFILE.stream()
                        .parallel()
                        .map(s -> EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI + s)
                        .collect(Collectors.joining(",")), this.fhirStoreResourcePageSize);
        Bundle observations = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "Patient",
                observationQuery);
        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                localPatientId, observations);
    }

    private void processDiagnosticReports(EyeMaticsFhirClient fhirClient,
                                          String localPatientId,
                                          Variables variables) throws Exception {
        String diagnosticReportQuery = String.format("/%s/DiagnosticReport?_profile=%s%s&_count=%s",
                localPatientId,
                EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                EyeMaticsConstants.EYEMATICS_CORE_DATASET_DIAGNOSTIC_REPORT_PROFILE, this.fhirStoreResourcePageSize);
        Bundle diagnosticReports = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "Patient", diagnosticReportQuery);
        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET,
                localPatientId, diagnosticReports);
    }

    private void processMedicationAdministration(EyeMaticsFhirClient fhirClient,
                                                 String localPatientId,
                                                 Variables variables) throws Exception {
        String medicationAdministrationQuery = String.format("/%s/MedicationAdministration?_profile=%s%s&_count=%s",
                localPatientId,
                EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_ADMINISTRATION_PROFILE,
                this.fhirStoreResourcePageSize);
        Bundle medicationAdministrations = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "Patient", medicationAdministrationQuery);
        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET,
                localPatientId, medicationAdministrations);
    }

    private void processMedicationRequest(EyeMaticsFhirClient fhirClient,
                                          String localPatientId,
                                          Variables variables) throws Exception {
        String medicationRequestQuery = String.format("/%s/MedicationRequest?_profile=%s%s&_count=%s",
                localPatientId,
                EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_REQUEST_PROFILE, this.fhirStoreResourcePageSize);
        Bundle medicationRequests = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                "Patient", medicationRequestQuery);
        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET,
                localPatientId, medicationRequests);
    }
}
