package org.eyematics.process.message.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.stream.Stream;

public class ProvideDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(ProvideDataMessageTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public ProvideDataMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        return Stream.of(api.getTaskHelper()
                            .createInput(new Reference().setReference(variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE)),
                                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS,
                                    ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_REFERENCE));
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        Task task = variables.getStartTask();

        String statusCode = EyeMaticsConstants.CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_REACHABLE;
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            statusCode = EyeMaticsConstants.CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_ALLOWED;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(statusCode, EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "Send data-set failed"));
        variables.updateTask(task);

        variables.setString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_ERROR_MESSAGE,
                "Send data-set failed");

        logger.warn(
                "Could not send data-set with id '{}' to DMS with identifier '{}' referenced in Task with id '{}' - {}",
                variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE),
                variables.getString("ConstantsDataTransfer.BPMN_EXECUTION_VARIABLE_DMS_IDENTIFIER"), task.getId(),
                exception.getMessage());
        throw new BpmnError(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_ERROR,
                "Send data-set - " + exception.getMessage());
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) {}
}
