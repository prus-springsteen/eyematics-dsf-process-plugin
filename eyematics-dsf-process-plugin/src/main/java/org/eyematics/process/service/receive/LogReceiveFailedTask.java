package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogReceiveFailedTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LogReceiveFailedTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public LogReceiveFailedTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        String providingOrganization = variables.getTarget().getOrganizationIdentifierValue();
        logger.error("-> The data-set from {} is missing for data receiving.", providingOrganization);
        Task task = new Task();
        task.setStatus(Task.TaskStatus.FAILED);
        task.addOutput(
                this.dataSetStatusGenerator.createDataSetStatusOutput(EyeMaticsGenericStatus.DATA_PROVIDE_MISSING.getStatusCode(), EyeMaticsGenericStatus.getTypeSystem(),
                        EyeMaticsGenericStatus.getTypeCode(), "Could not receive Data from " + providingOrganization + "."));
        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey, task);
    }

}
