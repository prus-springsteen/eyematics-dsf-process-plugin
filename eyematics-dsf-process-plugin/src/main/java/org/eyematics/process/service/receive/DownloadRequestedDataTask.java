package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.BasicFhirWebserviceClient;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadRequestedDataTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DownloadRequestedDataTask.class);

    public DownloadRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something not to download 14");
        String correlationKey = this.api.getVariables(delegateExecution).getTarget().getCorrelationKey();
        logger.info("-> Correlation Key: {}", correlationKey);
        Task latestTask = variables.getLatestTask();

        Reference reference = api.getTaskHelper()
                .getFirstInputParameterValue(latestTask,
                        ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS,
                        ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_DATASET_REFERENCE,
                        Reference.class)
                .orElseThrow(() -> super.getHandleTaskError(EyeMaticsGenericStatus.DATA_DOWNLOAD_FAILURE,
                                                            variables,
                                                "Could not find Reference-Input for downloading Data"));
        logger.info("Reference-Input extracted -> {}", reference.getReference());

        try {
            Binary referenceBinary = this.downloadData(reference.getReference());
            logger.info("Data downloaded...");
            delegateExecution.setVariable(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED + correlationKey, referenceBinary);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not download data from DIC: {}", errorMessage);
            super.handleTaskError(EyeMaticsGenericStatus.DATA_DOWNLOAD_FAILURE, variables, errorMessage);
        }
    }

    private Binary downloadData(String referenceInput) {
        IdType dataReference = new IdType(referenceInput);
        BasicFhirWebserviceClient client = api.getFhirWebserviceClientProvider()
                .getWebserviceClient(dataReference.getBaseUrl()).withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                        EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN);

        return dataReference.hasVersionIdPart()
                ? client.read(Binary.class, dataReference.getIdPart(), dataReference.getVersionIdPart())
                : client.read(Binary.class, dataReference.getIdPart());
    }
}
