package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.utils.generator.AbstractExtendedServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleReceiveMissingTask extends AbstractExtendedServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HandleReceiveMissingTask.class);

    public HandleReceiveMissingTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.warn("The receipt from {} is missing for data provided.", variables.getTarget().getOrganizationIdentifierValue());
        super.processTaskError(EyeMaticsGenericStatus.DATA_RECEIPT_MISSING, variables, "Data Receipt Missing");
    }


}
