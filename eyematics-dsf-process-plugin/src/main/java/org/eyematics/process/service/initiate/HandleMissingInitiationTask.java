package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HandleMissingInitiationTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HandleMissingInitiationTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public HandleMissingInitiationTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        String providingOrganization = variables.getTarget().getOrganizationIdentifierValue();
        logger.error("-> The initiation from {} is missing for the data provide request.", providingOrganization);
        Task task = new Task();
        task.setStatus(Task.TaskStatus.FAILED);
        task.setId("-");
        task.getRequester().getIdentifier().setValue(providingOrganization);
        task.addOutput(
                this.dataSetStatusGenerator
                        .createDataSetStatusOutput(EyeMaticsGenericStatus.PROVIDE_INITIATE_MISSING.getStatusCode(),
                                EyeMaticsGenericStatus.getTypeSystem(),
                                EyeMaticsGenericStatus.getTypeCode(),
                                "Could not receive Data from " + providingOrganization + "."));
        String correlationKey = variables.getTarget().getCorrelationKey();
        variables.setResource(EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey, task);
    }
}
