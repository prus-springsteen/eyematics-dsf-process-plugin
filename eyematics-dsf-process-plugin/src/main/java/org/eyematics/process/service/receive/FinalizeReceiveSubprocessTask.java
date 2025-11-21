package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FinalizeReceiveSubprocessTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeReceiveSubprocessTask.class);

    public FinalizeReceiveSubprocessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the receipt subprocess");
        Task currentTask = variables.getLatestTask();

        if (currentTask != null && Task.TaskStatus.FAILED.equals(currentTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                            EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(currentTask);
        }

        Task errorTask = (Task) this.getVariable(delegateExecution,
                ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE);

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
