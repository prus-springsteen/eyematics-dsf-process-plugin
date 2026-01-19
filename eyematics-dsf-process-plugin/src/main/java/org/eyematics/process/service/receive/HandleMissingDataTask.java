package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HandleMissingDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HandleMissingDataTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public HandleMissingDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        Target originTarget = (Target) variables.getVariable(ReceiveConstants.BPMN_EXECUTION_VARIABLE_TARGET_RESOURCE);
        String providingOrganization = originTarget.getOrganizationIdentifierValue();
        logger.error("-> The data-set from {} is missing for data receiving.", providingOrganization);
        Task task = new Task();
        task.setStatus(Task.TaskStatus.FAILED);
        task.setId("-");
        task.getRequester().getIdentifier().setValue("-");
        task.addOutput(
                this.dataSetStatusGenerator
                        .createDataSetStatusOutput(EyeMaticsGenericStatus.DATA_PROVIDE_MISSING.getStatusCode(),
                                EyeMaticsGenericStatus.getTypeSystem(),
                                EyeMaticsGenericStatus.getTypeCode(),
                                "Could not receive Data from " + providingOrganization + "."));
        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey, task);
    }
}
