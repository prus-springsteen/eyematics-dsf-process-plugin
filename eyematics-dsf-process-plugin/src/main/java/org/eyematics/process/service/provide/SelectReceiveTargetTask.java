/**
 * @author Mathias Rühle  (https://github.com/EmteZogaf)
 * @see    https://github.com/medizininformatik-initiative/mii-process-feasibility/blob/develop/mii-process-feasibility/src/main/java/de/medizininformatik_initiative/process/feasibility/service/SelectResponseTarget.java
 */

package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectReceiveTargetTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SelectReceiveTargetTask.class);

    public SelectReceiveTargetTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to select");
        Target target = SelectTarget.getRequestTarget(this.api, variables);
        variables.setTarget(target);
        logger.info("-> {}", target);
    }
}
