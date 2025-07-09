package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.InitiateConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetRequestTargetsTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SetRequestTargetsTask.class);

    public SetRequestTargetsTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        Targets targets = (Targets) delegateExecution.getVariable(InitiateConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_SELECTED_PROVIDE_TARGETS);
        variables.setTargets(targets);
    }
}
