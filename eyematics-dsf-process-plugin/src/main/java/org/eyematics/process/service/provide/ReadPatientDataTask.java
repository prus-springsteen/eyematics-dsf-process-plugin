package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.bpe.EyeMaticsDataBundleRetriever;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public class ReadPatientDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadPatientDataTask.class);
    private final FhirClientFactory fhirClientFactory;
    private final int fhirStoreResourcePageSize;

    public ReadPatientDataTask(ProcessPluginApi api,
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
        logger.info("-> Reading patient data from local FHIR repository is initiated");
        try {
            EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
            MailSender.sendInfo(this.api.getMailService(),
                    variables.getStartTask(),
                    "-",
                    "Data requested",
                    "there is a new patient data request which is processed.");

            String patientQuery = String.format("?identifier=%s|&count=%s",
                    EyeMaticsConstants.IDENTIFIER_CODE_SYSTEM_EYEMATICS_BLOOM_FILTER,
                    this.fhirStoreResourcePageSize);
            Bundle patients = EyeMaticsDataBundleRetriever.getEyeMaticsDataBundle(fhirClient,
                    "Patient",
                    patientQuery);

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read Patient data from FHIR repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.PATIENT_READ_FAILURE, variables, errorMessage);
        }
    }
}
