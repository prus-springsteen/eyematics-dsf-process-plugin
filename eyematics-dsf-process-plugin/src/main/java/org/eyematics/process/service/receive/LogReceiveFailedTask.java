package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogReceiveFailedTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LogReceiveFailedTask.class);

    public LogReceiveFailedTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to log");
    }


}
