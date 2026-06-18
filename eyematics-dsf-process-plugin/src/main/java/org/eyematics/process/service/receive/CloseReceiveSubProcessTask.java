package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CloseReceiveSubProcessTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CloseReceiveSubProcessTask.class);

    public CloseReceiveSubProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        Target originTarget = (Target) this.getVariable(delegateExecution,
                ReceiveConstants.BPMN_EXECUTION_VARIABLE_TARGET_RESOURCE);
        String providingOrganization = originTarget.getOrganizationIdentifierValue();
        Task latestTask = variables.getLatestTask();
        logger.warn("-> The data-set from {} is missing for data receiving.", providingOrganization);

        Coding currentTaskInputCoding = this.getStatusCodeFromInput(variables.getLatestTask());
        String statusCode = currentTaskInputCoding != null ?
                currentTaskInputCoding.getCode() : EyeMaticsGenericStatus.DATA_REQUEST_FAILURE.getStatusCode();

        Task task = new Task();
        task.setId(latestTask.getId());
        task.getRequester().getIdentifier().setValue("-");
        task.addOutput(
                this.dataSetStatusGenerator
                        .createDataSetStatusOutput(statusCode,
                                EyeMaticsGenericStatus.getTypeSystem(),
                                EyeMaticsGenericStatus.getTypeCode(),
                                "An error or other reason encountered during a request to "
                                        + providingOrganization + "."));
        this.setVariable(delegateExecution, EyeMaticsConstants.BPMN_EXECUTION_VARIABLE_ERROR_RESOURCE, task);
    }

    private Coding getStatusCodeFromInput(Task latestTask) {
        return latestTask
                .getInput().stream().filter(i -> i.getType().getCoding().stream()
                        .anyMatch(c -> EyeMaticsGenericStatus.getTypeSystem().equals(c.getSystem())
                                && EyeMaticsGenericStatus.getTypeCode().equals(c.getCode())))
                .findFirst()
                .map(c -> (Coding) c.getValue())
                .orElse(null);
    }
}
