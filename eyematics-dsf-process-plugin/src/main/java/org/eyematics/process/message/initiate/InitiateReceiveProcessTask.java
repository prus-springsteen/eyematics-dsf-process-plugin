/**
 * @see    https://github.com/medizininformatik-initiative/mii-process-data-sharing/blob/main/src/main/java/de/medizininformatik_initiative/process/data_sharing/message/SendMergeDataSharing.java
 */

package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.v1.constants.NamingSystems;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Stream;

public class InitiateReceiveProcessTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(InitiateReceiveProcessTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public InitiateReceiveProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        Targets targets = variables.getTargets();
        List<Task.ParameterComponent> targetInputs = targets.getEntries().stream().map(this::transformToTargetInput).toList();
        return targetInputs.stream();
    }

    private Task.ParameterComponent transformToTargetInput(Target target) {
        Task.ParameterComponent input = api.getTaskHelper().createInput(new StringType(target.getCorrelationKey()),
                ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE,
                ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE_PROCESS_CORRELATION_KEY);
        input.addExtension().setUrl(ReceiveConstants.EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER)
                .setValue(new Reference().setIdentifier(NamingSystems.OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()))
                        .setType(ResourceType.Organization.name()));
        return input;
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        Task task = variables.getStartTask();
        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.DATA_INITIATION_FAILURE;

        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.DATA_INITIATION_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        String message = String.format("Failed to initiate data sharing with DIC (identifier: '%s') in Task with ID '%s': %s",
                                       variables.getTarget().getOrganizationIdentifierValue(),
                                       task.getId(),
                                       exception.getMessage());
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, message));
        variables.updateTask(task);

        logger.warn(message);
        throw new BpmnError(status.getErrorCode());
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) { }
}
