package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public class FinalizeProvideProcessTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeProvideProcessTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public FinalizeProvideProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the provision process");
        Task startTask = variables.getStartTask();
        Task currentTask = variables.getLatestTask();

        if (!currentTask.getId().equals(startTask.getId())) {
            this.handleReceivedResponse(startTask, currentTask);
        }

        variables.updateTask(startTask);

        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES, EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
        }
    }

    private void handleReceivedResponse(Task startTask, Task currentTask) {
        this.dataSetStatusGenerator.transformInputToOutput(currentTask, startTask, EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS);
        if (startTask.getOutput().stream().filter(Task.TaskOutputComponent::hasExtension)
                .flatMap(o -> o.getExtension().stream())
                .anyMatch(e -> EyeMaticsConstants.EXTENSION_DATA_SET_STATUS_ERROR_URL.equals(e.getUrl()))) {
            startTask.setStatus(Task.TaskStatus.FAILED);
        }
    }
}
