package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.bpe.EyeMaticsDataBundleRetriever;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.bpe.PatientId;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;

public class ReadPatientDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadPatientDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadPatientDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, FhirClientFactory fhirClientFactory) {
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
        logger.info("-> Reading patient data from local FHIR repository is initiated");
        MailSender.sendInfo(this.api.getMailService(), variables.getStartTask(), "-",
                "Data requested",
                "there is a new patient data request which is processed.");
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        try {
            HashSet<String> patientIdSet = new HashSet<String>();

            Bundle observations =  variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET);
            observations.getEntry()
                    .forEach((e -> { Observation o = (Observation) e.getResource();
                        patientIdSet.add(PatientId.extract(o.getSubject().getReference()));
                    }));

            Bundle medicationAdministrations =  variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET);
            medicationAdministrations.getEntry()
                    .forEach((e -> { MedicationAdministration o = (MedicationAdministration) e.getResource();
                        patientIdSet.add(PatientId.extract(o.getSubject().getReference()));
                    }));

            Bundle medicationRequests =  variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET);
            medicationRequests.getEntry()
                    .forEach((e -> { MedicationRequest o = (MedicationRequest) e.getResource();
                        patientIdSet.add(PatientId.extract(o.getSubject().getReference()));
                    }));

            String patientsQuery = String.format("_id=%s", String.join(",", patientIdSet));
            Bundle patients = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Patient",
                    patientsQuery);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read Patient data from FHIR repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.PATIENT_READ_FAILURE, variables, errorMessage);
        }
    }
}
