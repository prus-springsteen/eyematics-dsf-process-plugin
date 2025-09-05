package org.eyematics.process.utils.delegate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.bpe.CopyTask;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public abstract class AbstractExtendedSubProcessServiceDelegate extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExtendedSubProcessServiceDelegate.class);
    protected final DataSetStatusGenerator dataSetStatusGenerator;

    public AbstractExtendedSubProcessServiceDelegate(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    protected void handleTaskError(EyeMaticsGenericStatus eyeMaticsGenericErrors, Variables variables, String errorMessage) {
        this.processTaskError(eyeMaticsGenericErrors, variables, errorMessage);
        throw new BpmnError(eyeMaticsGenericErrors.getErrorCode());
    }

    protected BpmnError getHandleTaskError(EyeMaticsGenericStatus eyeMaticsGenericErrors, Variables variables, String errorMessage)  {
        this.processTaskError(eyeMaticsGenericErrors, variables, errorMessage);
        return new BpmnError(eyeMaticsGenericErrors.getErrorCode());
    }

    protected void processTaskError(EyeMaticsGenericStatus eyeMaticsGenericStatus, Variables variables, String errorMessage) {
        Task task = variables.getLatestTask();
        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(eyeMaticsGenericStatus.getStatusCode(), EyeMaticsGenericStatus.getTypeSystem(),
                        EyeMaticsGenericStatus.getTypeCode(), errorMessage));
        variables.updateTask(task);

        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey, CopyTask.getTaskCopy(task));
    }
}
