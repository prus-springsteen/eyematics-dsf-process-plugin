package org.eyematics.process.message.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.CopyTask;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;

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
        Task task = variables.getLatestTask();

        if (task.getOutput().isEmpty()) {
            task.addOutput(
                    this.dataSetStatusGenerator.createDataSetStatusOutput(EyeMaticsGenericStatus.DATA_RECEIPT_SUCCESS.getStatusCode(),
                            EyeMaticsGenericStatus.getTypeSystem(),
                            EyeMaticsGenericStatus.getTypeCode(), null));
            variables.updateTask(task);
        }

       // return this.dataSetStatusGenerator.transformOutputToInputComponent(task, EyeMaticsGenericStatus.getTypeSystem(), EyeMaticsGenericStatus.getTypeCode());
        return Stream.empty();
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {

        Task task = variables.getLatestTask();
        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.DATA_RECEIPT_FAILURE;

        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.DATA_RECEIPT_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, exception.getMessage()));

        variables.updateTask(task);

        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey, CopyTask.getTaskCopy(task));

        logger.error(
                "Could not send receipt for data-set with id '{}' to DIC with identifier '{}' referenced in Task with id '{}' - {}",
                variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE),
                variables.getTarget().getOrganizationIdentifierValue(), task.getId(),
                exception.getMessage());
        throw new BpmnError(status.getErrorCode());
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) { }


}
