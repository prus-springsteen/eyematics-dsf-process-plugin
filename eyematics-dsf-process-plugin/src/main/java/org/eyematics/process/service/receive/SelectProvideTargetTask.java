package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SelectProvideTargetTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectProvideTargetTask.class);

    public SelectProvideTargetTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to select");
        Target target = SelectTarget.getRequestTargetExecution(this.api, delegateExecution);
        //variables.setTarget(target);
        logger.warn("-> nothing needs to be selected.");
        logger.info("-> {}", target);
    }
}
