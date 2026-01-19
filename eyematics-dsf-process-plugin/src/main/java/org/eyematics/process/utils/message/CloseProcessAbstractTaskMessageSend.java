package org.eyematics.process.utils.message;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public abstract class CloseProcessAbstractTaskMessageSend extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(CloseProcessAbstractTaskMessageSend.class);
    protected final DataSetStatusGenerator dataSetStatusGenerator;

    public CloseProcessAbstractTaskMessageSend(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        Task task = variables.getStartTask();
        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.RECEIVE_CLOSE_FAILURE;

        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.RECEIVE_CLOSE_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        String message = String.format("Could not send close receive subprocess to DIC ('%s') referenced in Task with id '%s' - {%s}",
                variables.getTarget().getOrganizationIdentifierValue(),
                task.getId(),
                exception.getMessage());
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(),
                        EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS,
                        message));
        variables.updateTask(task);

        logger.warn(message);
        throw new BpmnError(status.getErrorCode());
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) { }
}
