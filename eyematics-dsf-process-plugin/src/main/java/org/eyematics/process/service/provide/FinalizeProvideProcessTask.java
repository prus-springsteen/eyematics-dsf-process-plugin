package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.bpe.MailSender;
import org.eyematics.process.utils.delegate.FinalizeProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;


public class FinalizeProvideProcessTask extends FinalizeProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeProvideProcessTask.class);
    private final List<String> provideMailConfigAdresses;

    public FinalizeProvideProcessTask(ProcessPluginApi api,
                                      DataSetStatusGenerator dataSetStatusGenerator,
                                      List<String> provideMailConfigAdresses) {
        super(api, dataSetStatusGenerator);
        this.provideMailConfigAdresses = provideMailConfigAdresses;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the provision process.");
        Task startTask = variables.getStartTask();
        Task currentTask = variables.getLatestTask();

        if (!currentTask.getId().equals(startTask.getId())) {
            this.handleReceivedResponse(startTask, currentTask);
        }

        variables.updateTask(startTask);

        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES,
                            EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
            this.sendErrorMails(startTask);
        }

        if (!Task.TaskStatus.FAILED.equals(startTask.getStatus()) ||
                (Task.TaskStatus.FAILED.equals(startTask.getStatus()) &&
                        EyeMaticsGenericStatus.DATA_ACKNOWLEDGE_MISSING.getStatusCode().equals(this.getDataSetStatus(startTask)))) {
            logger.info("-> Mailing the shared pseudonyms.");
            Bundle globalPseudonymBundle = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_GLOBAL_PSEUDONYMS);
            this.sendGlobalPseudonymMail(globalPseudonymBundle, variables.getStartTask());
        }
    }

    private void handleReceivedResponse(Task startTask, Task currentTask) {
        this.dataSetStatusGenerator.transformInputToOutput(currentTask,
                startTask,
                EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS);
        if (startTask.getOutput().stream().filter(Task.TaskOutputComponent::hasExtension)
                .flatMap(o -> o.getExtension().stream())
                .anyMatch(e -> EyeMaticsConstants.EXTENSION_DATA_SET_STATUS_ERROR_URL.equals(e.getUrl()))) {
            startTask.setStatus(Task.TaskStatus.FAILED);
        }
    }

    private void sendErrorMails(Task task) {
        for (Task.TaskOutputComponent outputComponent : task.getOutput()) {
            Coding outputCoding = (Coding) outputComponent.getValue();
            String errorMessage = !outputComponent.getExtension().isEmpty() ?
                    outputComponent.getExtension().get(0).getValue().toString() : null;
            MailSender.sendError(this.api.getMailService(),
                    task,
                    ProvideConstants.PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS,
                    this.getClass().getName(),
                    outputCoding.getCode(),
                    errorMessage);
        }
    }

    private void sendGlobalPseudonymMail(Bundle globalPseudonymBundle, Task task) {
        if (!this.provideMailConfigAdresses.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Dear Admin, \n\n");
            message.append("an EyeMatics data exchange has been conducted.\n\n");
            message.append("Task Id: ");
            message.append(task.getId());
            message.append("\n");
            message.append("Requester: ");
            message.append(task.getRequester().getIdentifier().getValue());
            message.append("\n");
            message.append("Provider: ");
            message.append(task.getRestriction().getRecipientFirstRep().getIdentifier().getValue());
            message.append("\n\n");
            message.append("Data from following global pseudonyms where exchanged:\n\n");
            message.append(this.api.getFhirContext()
                    .newJsonParser()
                    .setPrettyPrint(true)
                    .encodeResourceToString(globalPseudonymBundle));
            this.api.getMailService().send("EyeMatics Data Exchange: Global pseudonyms",
                    message.toString(),
                    this.provideMailConfigAdresses);
        }
    }
}
