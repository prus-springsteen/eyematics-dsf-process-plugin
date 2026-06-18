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
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;


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
        logger.info("-> Downloading the provided data.");
        try {
            Task latestTask = variables.getLatestTask();
            Bundle receivedData = new Bundle().setType(Bundle.BundleType.COLLECTION);
            receivedData.setId(UUID.randomUUID().toString());

            this.api.getTaskHelper().getInputParameters(latestTask,
                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS,
                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_DATA_SET_REFERENCE,
                    Reference.class)
                    .filter(p -> p.getValue() instanceof Reference)
                    .map(p -> (Reference) p.getValue())
                    .forEach(r -> {
                        Binary referenceBinary = this.downloadData(r.getReference());
                        String rId = referenceBinary.getIdElement().getIdPart();
                        this.setVariable(delegateExecution,
                                ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET, rId, referenceBinary);
                        receivedData.addEntry().setResource(new Basic().setSubject(new Reference(rId)));
                    });

            this.setVariable(delegateExecution,
                    ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_REFERENCE_BUNDLE,
                    receivedData);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not download data from DIC: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_DOWNLOAD_FAILURE, variables, errorMessage);
        }
    }

    private Binary downloadData(String referenceInput) {
        IdType dataReference = new IdType(referenceInput);
        BasicFhirWebserviceClient client = this.api.getFhirWebserviceClientProvider()
                .getWebserviceClient(dataReference.getBaseUrl()).withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                        EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN);
        return dataReference.hasVersionIdPart()
                ? client.read(Binary.class, dataReference.getIdPart(), dataReference.getVersionIdPart())
                : client.read(Binary.class, dataReference.getIdPart());
    }
}
