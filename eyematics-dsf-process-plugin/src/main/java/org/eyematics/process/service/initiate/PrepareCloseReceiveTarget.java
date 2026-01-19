package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.bpe.SelectTarget;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrepareCloseReceiveTarget extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PrepareCloseReceiveTarget.class);

    public PrepareCloseReceiveTarget(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        String provideOrganization = variables.getTarget().getOrganizationIdentifierValue();
        logger.info("-> Preparing the termination of the receive subprocess opened for {}.", provideOrganization);
        String correlationKey = variables.getTarget().getCorrelationKey();
        Target target = SelectTarget.getRequestTarget(this.api, variables, correlationKey, true);
        variables.setTarget(target);
    }
}
