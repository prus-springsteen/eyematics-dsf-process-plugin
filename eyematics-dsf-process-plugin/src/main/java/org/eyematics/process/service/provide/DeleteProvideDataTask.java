package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.dsf.fhir.client.BasicFhirWebserviceClient;


public class DeleteProvideDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DeleteProvideDataTask.class);

    public DeleteProvideDataTask(ProcessPluginApi api,  DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Deleting the provided data");
        IdType binaryId = new IdType(variables.getString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE));
        try {
            if (!binaryId.isEmpty()) this.deletePermanently(binaryId);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Permanently deleting encrypted transferable data-set failed - {}", exception.getMessage());
            this.handleTaskError(EyeMaticsGenericStatus.DATA_DELETE_FAILURE, variables, errorMessage);
        }
    }

    private void deletePermanently(IdType binaryId) {
        BasicFhirWebserviceClient client = api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
                .withRetry(EyeMaticsConstants.DSF_CLIENT_RETRY_6_TIMES, EyeMaticsConstants.DSF_CLIENT_RETRY_INTERVAL_5MIN);
        client.delete(Binary.class, binaryId.getIdPart());
        client.deletePermanently(Binary.class, binaryId.getIdPart());
    }
}
