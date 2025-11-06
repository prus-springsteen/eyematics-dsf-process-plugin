package org.eyematics.process.service.provide;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.*;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadProvideDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadProvideDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadProvideDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, FhirClientFactory fhirClientFactory) {
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
        logger.info("-> Reading local data from FHIR repository is initiated");
        MailSender.sendInfo(this.api.getMailService(), variables.getStartTask(), "-", "Data requested", "there is a new data request which is processed.");
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        try {
            HashSet<String> patientIds = new HashSet<>();

            String observationQuery = String.format("_profile=%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_OBSERVATION_PROFILE.stream()
                    .map(s -> EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI + s)
                    .collect(Collectors.joining(",")));
            Bundle observations = this.getEyeMaticsDataBundle(fhirClient, "Observation", observationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET, observations);

            String medicationQuery = String.format("_profile=%s%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_PROFILE);
            Bundle medications = this.getEyeMaticsDataBundle(fhirClient, "Medication", medicationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET, medications);

            String medicationAdministrationQuery = String.format("_profile=%s%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_ADMINISTRATION_PROFILE);
            Bundle medicationAdministrations = this.getEyeMaticsDataBundle(fhirClient, "MedicationAdministration", medicationAdministrationQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, medicationAdministrations);

            String medicationRequestQuery = String.format("_profile=%s%s",
                    EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI,
                    EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_REQUEST_PROFILE);
            Bundle medicationRequests = this.getEyeMaticsDataBundle(fhirClient, "MedicationRequest", medicationRequestQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, medicationRequests);

            observations.getEntry()
                    .forEach((e -> { Observation o = (Observation) e.getResource();
                        patientIds.add(extractPatientId(o.getSubject().getReference()));
            }));

            medicationAdministrations.getEntry()
                    .forEach((e -> { MedicationAdministration o = (MedicationAdministration) e.getResource();
                        patientIds.add(extractPatientId(o.getSubject().getReference()));
            }));

            medicationRequests.getEntry()
                    .forEach((e -> { MedicationRequest o = (MedicationRequest) e.getResource();
                        patientIds.add(extractPatientId(o.getSubject().getReference()));
            }));

            String patientsQuery = String.format("_id=%s", String.join(",", patientIds));
            Bundle patients = this.getEyeMaticsDataBundle(fhirClient, "Patient", patientsQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read data from FHIR repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_READ_FAILURE, variables, errorMessage);
        }
    }

    private Bundle getEyeMaticsDataBundle(EyeMaticsFhirClient fhirClient, String resource, String searchQuery) throws Exception {
        String data = fhirClient.read(resource, searchQuery, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
        Bundle dataBundle = this.api.getFhirContext().newJsonParser().parseResource(Bundle.class, data);
        String nextLink = this.getNextLink(dataBundle);
        while (nextLink != null) {
            String nextData = fhirClient.read(resource, searchQuery, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
            Bundle nextBundle = this.api.getFhirContext().newJsonParser().parseResource(Bundle.class, nextData);
            dataBundle.getEntry().addAll(nextBundle.getEntry());
            nextLink = this.getNextLink(nextBundle);
        }
        return this.api.getFhirContext().newJsonParser().parseResource(Bundle.class, data);
    }

    private String getNextLink(Bundle bundle) {
        return bundle.getLink(Bundle.LINK_NEXT) != null ? bundle.getLink(Bundle.LINK_NEXT).getUrl() : null;
    }

    private String extractPatientId(String reference) {
        if (reference.contains("Patient/")) return reference.substring(reference.lastIndexOf("/") + 1);
        return null;
    }
}
