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
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


public class FinalizeReceiveProcessTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeReceiveProcessTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public FinalizeReceiveProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("Finalizing Process ...");
        Task startTask = variables.getLatestTask();
        List<Target> targetsList = variables.getTargets().getEntries();
        targetsList.forEach(target -> {
                    String correlationKey = target.getCorrelationKey();
                    Task subTask = variables.getResource(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE + correlationKey);
                    if (subTask == null) {
                        startTask.addOutput(
                                this.dataSetStatusGenerator.createDataSetStatusOutput(EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(),
                                        EyeMaticsGenericStatus.getTypeSystem(),
                                        null));
                    } else {
                        if (Task.TaskStatus.FAILED.equals(subTask.getStatus())) {
                            startTask.setStatus(subTask.getStatus());
                        }
                        subTask.getOutput().forEach(startTask::addOutput);
                    }
                });
        variables.updateTask(startTask);

        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES, EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
        }
    }
}
