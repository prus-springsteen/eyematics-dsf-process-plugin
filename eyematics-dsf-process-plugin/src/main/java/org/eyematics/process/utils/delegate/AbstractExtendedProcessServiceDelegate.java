package org.eyematics.process.utils.delegate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public abstract class AbstractExtendedProcessServiceDelegate extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExtendedProcessServiceDelegate.class);
    protected final DataSetStatusGenerator dataSetStatusGenerator;

    public AbstractExtendedProcessServiceDelegate(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
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

    protected void processTaskError(EyeMaticsGenericStatus eyeMaticsGenericStatus, Variables variables, String errorMessage) {
        Task task = variables.getStartTask();
        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(eyeMaticsGenericStatus.getStatusCode(), EyeMaticsGenericStatus.getTypeSystem(),
                        EyeMaticsGenericStatus.getTypeCode(), errorMessage));
        variables.updateTask(task);
    }

    private String processResourceKey(String resourceName, String id) {
        return resourceName + "_" + id;
    }

    protected void setResource(Variables variables, String resourceName, String id, Resource resource) {
        variables.setResource(this.processResourceKey(resourceName, id), resource);
    }

    protected <R extends Resource> R getResource(Variables variables, String resourceName, String id) {
        return variables.getResource(this.processResourceKey(resourceName, id));
    }

    protected void setByte(Variables variables, byte[] byteValue, String resourceName, String id) {
        variables.setByteArray(this.processResourceKey(resourceName, id), byteValue);
    }

    protected byte[] getByte(Variables variables, String resourceName, String id) {
        return variables.getByteArray(this.processResourceKey(resourceName, id));
    }
}
