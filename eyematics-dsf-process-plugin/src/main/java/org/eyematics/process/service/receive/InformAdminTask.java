package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InformAdminTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InformAdminTask.class);

    public InformAdminTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Informing the admin about the encountered error.");
        String correlationKey = variables.getTarget().getCorrelationKey();
        Task errorTask = variables.getResource(EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey);
        if (errorTask != null) {
            Coding output = (Coding) errorTask.getOutput().get(0).getValue();
            MailSender.sendError(this.api.getMailService(),
                    errorTask,
                    ReceiveConstants.PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS,
                    this.getClass().getName(),
                    output.getCode(),
                    errorTask.getOutput().get(0).getExtension().get(0).getValue().toString());
        }
    }
}
