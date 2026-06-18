package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.InitiateConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.delegate.FinalizeProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FinalizeInitiateSubProcessTask extends FinalizeProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeInitiateSubProcessTask.class);

    public FinalizeInitiateSubProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the initiate subprocess.");
        Task currentTask = variables.getLatestTask();

        String correlationKey = this.getCorrelationKey(delegateExecution);
        Task errorTask = this.getErrorTask(variables, correlationKey);

        if (errorTask != null) {
            MailSender.sendError(this.api.getMailService(),
                    errorTask,
                    InitiateConstants.PROCESS_NAME_EXECUTE_INITIATE_EYEMATICS_PROCESS,
                    this.getClass().getName(),
                    this.getDataSetStatus(errorTask),
                    this.getErrorMessage(errorTask));
            currentTask.setStatus(Task.TaskStatus.FAILED);
            currentTask.setOutput(errorTask.getOutput());
            variables.updateTask(currentTask);
        } else {
            currentTask.addOutput(this.dataSetStatusGenerator.createDataSetStatusOutput(
                    EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(),
                    EyeMaticsGenericStatus.getTypeSystem(),
                    EyeMaticsGenericStatus.getTypeCode()));
        }

        variables.updateTask(currentTask);

        if (Task.TaskStatus.FAILED.equals(currentTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                            EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(currentTask);
        }
    }
}
