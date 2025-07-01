package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InitiateReceiveTargetTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InitiateReceiveTargetTask.class);

    public InitiateReceiveTargetTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Triggering the Organization for Receive ...");
        Target target = SelectTarget.getRequestTarget(this.api, variables, null);
        logger.info("-> Target -> {}", target.toString());
        variables.setTarget(target);
    }
}
