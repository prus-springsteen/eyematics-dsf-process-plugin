package org.eyematics.process.message.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
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
        logger.info("-> nothing to send");
        return Stream.empty();
    }

    /*
    @Override
    protected void handleIntermediateThrowEventError(DelegateExecution execution, Variables variables,
                                                     Exception exception, String errorMessage) {
        logger.warn("handleIntermediateThrowEventError -> Exception: {}\tErrorMessage: {}", exception, errorMessage);
    }

    @Override
    protected void handleEndEventError(DelegateExecution execution, Variables variables,
                                       Exception exception, String errorMessage) {
        logger.warn("handleEndEventError -> Exception: {}\tErrorMessage: {}", exception, errorMessage);
    }
    */

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        logger.warn("handleSendTaskError -> Exception: {}\tErrorMessage: {}", exception, errorMessage);
             /*
        Task startTask = variables.getStartTask();
        Task currentTask = variables.getLatestTask();
        String oValue = variables.getTarget().getOrganizationIdentifierValue();

        String statusCode = String.format("%s: %s", oValue, EyeMaticsConstants.CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_REACHABLE);
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            statusCode = String.format("%s: %s", oValue, EyeMaticsConstants.CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_ALLOWED);
        }

        currentTask.setStatus(Task.TaskStatus.FAILED);
        variables.updateTask(currentTask);

        startTask.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(statusCode, EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "Initiate Data-Exchange failed."));
        variables.updateTask(startTask);

        variables.setString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_ERROR_MESSAGE,
                "Send data-set failed");

        logger.warn(
                "Could not send data-set with id '{}' to DMS with identifier '{}' referenced in Task with id '{}' - {}",
                variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE),
                variables.getString("ConstantsDataTransfer.BPMN_EXECUTION_VARIABLE_DMS_IDENTIFIER"), currentTask.getId(),
                exception.getMessage());

        throw new BpmnError(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_ERROR,
                "Send data-set - " + exception.getMessage());*/
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) {}


}
