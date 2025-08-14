package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleMissingReceiptTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HandleMissingReceiptTask.class);

    public HandleMissingReceiptTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.warn("The receipt from {} is missing for data provided.", variables.getTarget().getOrganizationIdentifierValue());
        super.processTaskError(EyeMaticsGenericStatus.DATA_RECEIPT_MISSING, variables, "Data Receipt Missing");
    }
}
