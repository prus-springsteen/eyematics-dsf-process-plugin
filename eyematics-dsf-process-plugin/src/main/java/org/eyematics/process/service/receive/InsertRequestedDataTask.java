package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class InsertRequestedDataTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InsertRequestedDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public InsertRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator,
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
        logger.info("-> Insertion of the provided data into FHIR repository");
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        try {
            Bundle bundle = (Bundle) this.getVariable(delegateExecution,
                    ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET);
            String bundleString = this.api.getFhirContext().newJsonParser().encodeResourceToString(bundle);
            String methodOutcome = fhirClient.create(bundleString, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
            String providingOrganization = variables.getLatestTask().getRequester().getIdentifier().getValue();
            String countedResources = this.countResources(providingOrganization, methodOutcome);
            logger.info("Data is inserted for further processing. {}", countedResources);
            MailSender.sendInfo(this.api.getMailService(),
                    variables.getLatestTask(),
                    EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(),
                    "Data inserted",
                    countedResources);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not insert data to FHIR-Repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_INSERT_FAILURE, variables, errorMessage);
        }
    }

    private String countResources(String providingOrganization, String methodOutcome) {
        int patients = 0;
        int observations = 0;
        int medications = 0;
        int medicationAdministrations = 0;
        int medicationRequest = 0;
        try {
            Bundle methodOutcomeBundle = (Bundle) this.api.getFhirContext().newJsonParser().parseResource(methodOutcome);
            for (Bundle.BundleEntryComponent entry : methodOutcomeBundle.getEntry()) {
                if (entry.getResponse().getLocation().contains("Patient")) patients++;
                if (entry.getResponse().getLocation().contains("Observation")) observations++;
                if (entry.getResponse().getLocation().contains("Medication")) medications++;
                if (entry.getResponse().getLocation().contains("MedicationAdministration")) medicationAdministrations++;
                if (entry.getResponse().getLocation().contains("MedicationRequest")) medicationRequest++;
            }
        } catch (Exception exception) {
            logger.info("Could not parse method outcome: {}", exception.getMessage());
        }
        return String.format("%s submitted %s patients, %s observations, %s medications, %s medication administrations and %s medication requests.",
                providingOrganization,
                patients,
                observations,
                medications,
                medicationAdministrations,
                medicationRequest);
    }
}
