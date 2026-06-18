package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.delegate.FinalizeProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


public class FinalizeReceiveProcessTask extends FinalizeProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeReceiveProcessTask.class);

    public FinalizeReceiveProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the receipt process.");
        Task startTask = variables.getLatestTask();
        List<Target> targetsList = variables.getTargets().getEntries();
        int failedTaskCount = 0;
        for (Target target : targetsList) {
            String correlationKey = target.getCorrelationKey();
            Task subTask = this.getErrorTask(variables, correlationKey);
            if (subTask == null) {
                startTask.addOutput(
                        this.dataSetStatusGenerator.createDataSetStatusOutput(
                                EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(),
                                EyeMaticsGenericStatus.getTypeSystem(),
                                EyeMaticsGenericStatus.getTypeCode()));
            } else {
                if (this.getErrorMessage(subTask) != null) {
                    failedTaskCount++;
                }
                if (!subTask.getOutput().isEmpty()) {
                    Task.TaskOutputComponent errorOutput = subTask.getOutput().get(0);
                    errorOutput.getExtension().clear();
                    startTask.addOutput(errorOutput);
                }
            }
        }

        if (failedTaskCount == targetsList.size()) {
            startTask.setStatus(Task.TaskStatus.FAILED);
        }

        variables.updateTask(startTask);

        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                            EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
        }
    }
}
