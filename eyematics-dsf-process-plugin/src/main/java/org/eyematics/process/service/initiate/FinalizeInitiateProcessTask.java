package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public class FinalizeInitiateProcessTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeInitiateProcessTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public FinalizeInitiateProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.dataSetStatusGenerator, "dataSetStatusGenerator");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Finalizing the initiation process");
        Task startTask = variables.getStartTask();
        if (Task.TaskStatus.FAILED.equals(startTask.getStatus())) {
            this.api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                    .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES, EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN)
                    .update(startTask);
            this.sendMail(startTask, "Test", "testError");
        } else {
            startTask.addOutput(this.dataSetStatusGenerator.createDataSetStatusOutput(EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(),
                                                                                      EyeMaticsGenericStatus.getTypeSystem(),
                                                                                      EyeMaticsGenericStatus.getTypeCode(),
                                                                          null));
            variables.updateTask(startTask);
        }
    }

    private void sendMail(Task latestTask, String projectIdentifier, String error) {
        String subject = "Error in process '" + "ConstantsDataSharing.PROCESS_NAME_FULL_MERGE_DATA_SHARING" + "'";
        String message = "Could not send data-set status receipt for new data-set in process '"
                + "ConstantsDataSharing.PROCESS_NAME_FULL_MERGE_DATA_SHARING" + "' for Task with id '"
                + latestTask.getId() + "' to organization '" + latestTask.getRequester().getIdentifier().getValue()
                + "' for project-identifier '" + projectIdentifier + "'.\n\nError:\n"
                + (error == null ? "Unknown" : error);

        this.api.getMailService().send(subject, message);
    }
}
