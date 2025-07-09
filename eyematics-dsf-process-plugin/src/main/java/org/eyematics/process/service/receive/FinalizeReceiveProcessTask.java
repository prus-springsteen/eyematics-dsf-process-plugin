package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinalizeReceiveProcessTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeReceiveProcessTask.class);

    public FinalizeReceiveProcessTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("Adding data status to output of task.");
        Task startTask = variables.getStartTask();
        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES, EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
        }
    }
}
