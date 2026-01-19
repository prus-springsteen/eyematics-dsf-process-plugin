package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.InitiateConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.delegate.FinalizeProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;


public class FinalizeInitiateProcessTask extends FinalizeProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeInitiateProcessTask.class);

    public FinalizeInitiateProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the initiation process");
        Task startTask = variables.getStartTask();

        List<Target> targetsList = variables.getTargets().getEntries();
        int failedTaskCount = 0;
        for (Target target : targetsList) {
            String correlationKey = target.getCorrelationKey();
            Task subTask = this.getErrorTask(variables, correlationKey);
            if (subTask == null) {
                startTask.addOutput(
                        this.dataSetStatusGenerator.createDataSetStatusOutput(
                                EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(),
                                EyeMaticsGenericStatus.getTypeSystem(),
                                EyeMaticsGenericStatus.getTypeCode()));
            } else {
                if (Task.TaskStatus.FAILED.equals(subTask.getStatus())) {
                    failedTaskCount++;
                }
                if (!subTask.getOutput().isEmpty()) {
                    Task.TaskOutputComponent errorOutput = subTask.getOutput().get(0);
                    this.sendErrorMail(subTask, errorOutput);
                    errorOutput.getExtension().clear();
                    startTask.addOutput(errorOutput);
                }
            }
        }

        if (failedTaskCount == targetsList.size()) {
            startTask.setStatus(Task.TaskStatus.FAILED);
        }

        variables.updateTask(startTask);

        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                            EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
            if (startTask.getOutput().size() == 1) {
                this.sendErrorMail(startTask, startTask.getOutput().get(0));
            }
        }
    }

    private void sendErrorMail(Task task, Task.TaskOutputComponent taskOutputComponent) {
        Coding output = (Coding) taskOutputComponent.getValue();
        if (output.hasExtension() && !output.getExtension().isEmpty()) {
            MailSender.sendError(this.api.getMailService(),
                    task,
                    InitiateConstants.PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS,
                    this.getClass().getName(),
                    output.getCode(),
                    taskOutputComponent.getExtension().get(0).getValue().toString());
        }

    }
}
