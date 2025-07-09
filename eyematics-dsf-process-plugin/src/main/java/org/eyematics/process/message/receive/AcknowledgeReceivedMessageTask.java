package org.eyematics.process.message.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public class AcknowledgeReceivedMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(AcknowledgeReceivedMessageTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public AcknowledgeReceivedMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        String dataSetStatus = (String) execution.getVariable(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_ERROR_MESSAGE);

        String errorMessage = null;
        if (dataSetStatus == null) {
            dataSetStatus = EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESSFUL.getStatusCode();
        } else {
            errorMessage = "Data Receive Error";
        }
        return Stream.of(this.dataSetStatusGenerator.createDataSetStatusInput(dataSetStatus,
                EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS,
                errorMessage));
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {
        Task task = variables.getStartTask();

        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.DATA_RECEIPT_FAILED;
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.DATA_RECEIPT_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(), status.getTypeSystem(),
                        status.getTypeCode(), "Send receipt failed"));
        variables.updateTask(task);

        logger.warn(
                "Could not send receipt for data-set with id '{}' to DIC with identifier '{}' referenced in Task with id '{}' - {}",
                variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE),
                variables.getTarget().getOrganizationIdentifierValue(), task.getId(),
                exception.getMessage());
        throw new BpmnError(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_ACKNOWLEDGE_ERROR,
                "Send data-set - " + exception.getMessage());
    }

    @Override
    protected void addErrorMessage(Task task, String errorMessage) {}
}
