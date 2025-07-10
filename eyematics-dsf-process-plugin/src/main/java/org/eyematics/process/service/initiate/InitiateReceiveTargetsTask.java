package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.InitiateConstants;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;


public class InitiateReceiveTargetsTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InitiateReceiveTargetsTask.class);

    public InitiateReceiveTargetsTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Triggering the Organization for Receive ...");
        Targets selectedProvideTargets = (Targets) delegateExecution.getVariable(InitiateConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_SELECTED_PROVIDE_TARGETS);
        List<Target> targets = selectedProvideTargets.getEntries().stream()
                                                     .map(target -> SelectTarget.getRequestTarget(this.api, variables, null)).toList();
        variables.setTargets(variables.createTargets(targets));
        Targets initiateReceiveQueue = variables.createTargets(new ArrayList<>(selectedProvideTargets.getEntries()));
        delegateExecution.setVariable(InitiateConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_INITIATE_RECEIVE_QUEUE, initiateReceiveQueue);
    }


}
