package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogReceiveFailedTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LogReceiveFailedTask.class);

    public LogReceiveFailedTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.warn("The data-set from {} is missing for data receiving.", variables.getTarget().getOrganizationIdentifierValue());
        super.processTaskError(EyeMaticsGenericStatus.DATA_PROVISION_MISSING, variables, "There data-set from DIC is missing for further processing.");
    }


}
