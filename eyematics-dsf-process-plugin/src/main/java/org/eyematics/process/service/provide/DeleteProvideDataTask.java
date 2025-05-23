package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.dsf.fhir.client.BasicFhirWebserviceClient;

import static org.eyematics.process.constant.EyeMaticsConstants.*;

public class DeleteProvideDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DeleteProvideDataTask.class);

    public DeleteProvideDataTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to delete");
        Task task = variables.getStartTask();

        IdType binaryId = new IdType(
                variables.getString(BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE));
        /*
        String dmsIdentifier = variables.getString(ConstantsDataTransfer.BPMN_EXECUTION_VARIABLE_DMS_IDENTIFIER);
        String projectIdentifier = variables
                .getString(ConstantsDataTransfer.BPMN_EXECUTION_VARIABLE_PROJECT_IDENTIFIER);
        IdType binaryId = new IdType(
                variables.getString(BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE));

        logger.info(
                "Permanently deleting encrypted Binary with id '{}' provided for DMS '{}' and project-identifier '{}' "
                        + "referenced in Task with id '{}'",
                binaryId.getValue(), dmsIdentifier, projectIdentifier, task.getId());
        */
        try
        {
            logger.info("-> nothing to delete");
            //deletePermanently(binaryId);
        }
        catch (Exception exception)
        {
            /*
            logger.warn(
                    "Could not permanently delete data-set for DMS '{}' and project-identifier '{}' referenced in Task with id '{}' - {}",
                    dmsIdentifier, projectIdentifier, task.getId(), exception.getMessage());
            */
            String error = "Permanently deleting encrypted transferable data-set failed - " + exception.getMessage();
            throw new RuntimeException(error, exception);
        }
    }

    private void deletePermanently(IdType binaryId) {
        BasicFhirWebserviceClient client = api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                .withRetry(DSF_CLIENT_RETRY_6_TIMES, DSF_CLIENT_RETRY_INTERVAL_5MIN);
        client.delete(Binary.class, binaryId.getIdPart());
        client.deletePermanently(Binary.class, binaryId.getIdPart());
    }
}
