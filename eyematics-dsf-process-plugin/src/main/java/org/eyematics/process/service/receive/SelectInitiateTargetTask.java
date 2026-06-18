package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SelectInitiateTargetTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectInitiateTargetTask.class);

    public SelectInitiateTargetTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Selecting the target for the initiate provide process.");
        String correlationKey = variables.getTarget().getCorrelationKey();
        delegateExecution.setVariable(ReceiveConstants.BPMN_EXECUTION_VARIABLE_TARGET_RESOURCE
                        + "_" + correlationKey, variables.getTarget());
        Target target = SelectTarget.getRequestTarget(this.api, variables, correlationKey, true);
        variables.setTarget(target);
    }
}
