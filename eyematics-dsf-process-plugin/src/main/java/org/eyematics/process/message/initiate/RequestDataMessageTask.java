package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public class RequestDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(RequestDataMessageTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public RequestDataMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        return Stream.empty();
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        Task task = variables.getStartTask();
        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.DATA_REQUEST_FAILURE;

        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.DATA_REQUEST_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        String message = String.format("Requesting data from DIC (identifier: '%s') in Task with ID '%s': %s failed.",
                variables.getTarget().getOrganizationIdentifierValue(),
                task.getId(),
                exception.getMessage());

        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, message));
        variables.updateTask(task);
        logger.warn(message);
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) { }
}
