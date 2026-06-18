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
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;


public class InitiateProvideDataMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(InitiateProvideDataMessageTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public InitiateProvideDataMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
                                                                           Variables variables) {
        logger.info("-> Preparing the initiation of the provide process.");
        return Stream.empty();
    }

    @Override
    protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
                                       String errorMessage) {

        Task task = new Task();
        EyeMaticsGenericStatus status = EyeMaticsGenericStatus.PROVIDE_INITIATE_FAILURE;

        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            status = EyeMaticsGenericStatus.PROVIDE_INITIATE_FORBIDDEN;
        }

        task.setStatus(Task.TaskStatus.FAILED);
        String message = String.format("Could not initiate data provision from DIC ('%s') - {%s}.",
                variables.getTarget().getOrganizationIdentifierValue(),
                exception.getMessage());

        task.addOutput(this.dataSetStatusGenerator.createDataSetStatusOutput(status.getStatusCode(),
                        EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                        EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS,
                        message));

        variables.updateTask(task);

        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE + "_" + correlationKey,
                task.copy());

        logger.warn(message);
        throw new BpmnError(status.getErrorCode());
    }

    // Override in order not to add error message of AbstractTaskMessageSend
    @Override
    protected void addErrorMessage(Task task, String errorMessage) { }
}
