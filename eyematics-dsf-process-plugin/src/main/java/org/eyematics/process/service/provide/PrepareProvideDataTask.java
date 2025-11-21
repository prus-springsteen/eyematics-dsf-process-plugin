package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ProvideConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrepareProvideDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PrepareProvideDataTask.class);
    private final boolean isAdminApprovalRequired;

    public PrepareProvideDataTask(ProcessPluginApi api, boolean isAdminApprovalRequired) {
        super(api);
        this.isAdminApprovalRequired = isAdminApprovalRequired;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Preparing provide process wether or not admin approval is required.");
        variables.setBoolean(ProvideConstants.BPMN_EXECUTION_VARIABLE_EYEMATICS_DATA_SET_ADMIN_APPROVAL_REQUIRED,
                this.isAdminApprovalRequired);
    }
}
