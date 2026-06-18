package org.eyematics.process.utils.delegate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;

import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public abstract class FinalizeProcessServiceDelegate extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeProcessServiceDelegate.class);
    protected final DataSetStatusGenerator dataSetStatusGenerator;

    public FinalizeProcessServiceDelegate(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    protected String getCorrelationKey(DelegateExecution delegateExecution) {
        return this.api.getVariables(delegateExecution).getTarget().getCorrelationKey();
    }

    protected Task getErrorTask(Variables variables, String correlationKey) {
        return variables.getResource(EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE
                + "_" + correlationKey);
    }

    protected String getDataSetStatus(Task task) {
        if (task == null) return null;
        return task.getOutput().stream()
                .map(output -> (Coding) output.getValue())
                .map(Coding::getCode)
                .findFirst()
                .orElse(null);
    }

    protected String getErrorMessage(Task task) {
        if (task == null) return null;
        return task.getOutput().stream()
                .findFirst()
                .map(Task.TaskOutputComponent::getExtension)
                .flatMap(extensions -> extensions.stream().findFirst())
                .map(Extension::getValue)
                .map(Object::toString)
                .orElse(null);
    }
}
