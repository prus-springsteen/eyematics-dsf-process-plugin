package org.eyematics.process.service.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.BasicFhirWebserviceClient;
import org.eyematics.process.constant.ProvideConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.eyematics.process.constant.EyeMaticsConstants.CODE_SYSTEM_RECEIVE_PROCESS;
import static org.eyematics.process.constant.EyeMaticsConstants.CODE_SYSTEM_RECEIVE_PROCESS_REFERENCE;

public class DownloadDataReceiveTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DownloadDataReceiveTask.class);

    public DownloadDataReceiveTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to download");
        Task latestTask = variables.getLatestTask();

        Optional<Reference> dataReferenceParameter = api.getTaskHelper()
                                                        .getFirstInputParameterValue(latestTask,
                                                                                     CODE_SYSTEM_RECEIVE_PROCESS,
                                                                                     CODE_SYSTEM_RECEIVE_PROCESS_REFERENCE,
                                                                                     Reference.class);

        logger.info("Reference-Input extracted -> {}", dataReferenceParameter.isPresent());
        Reference s = null;
        String referenceInput = "";
        if(dataReferenceParameter.isPresent()) s = dataReferenceParameter.get();
        if (s != null) {
            referenceInput = s.getReference();
        }
        logger.info("Data extracted.");

        // https://github.com/medizininformatik-initiative/dsf-plugin-numdashboard/blob/main/src/main/java/de/medizininformatik_initiative/process/report/service/DownloadReport.java
        // HOW TO ADD CERTIFICATE? -> DIFFERENCE BETWEEN LOCAL, REMOTE, ... etc.
        logger.info("Downloading data...");
        IdType dataReference = new IdType(referenceInput);
        BasicFhirWebserviceClient client = api.getFhirWebserviceClientProvider().getWebserviceClient(dataReference.getBaseUrl()).withRetry(6, 5);
        Binary referenceBinary = null;

        if (dataReference.hasVersionIdPart())
            referenceBinary = client.read(Binary.class, dataReference.getIdPart(), dataReference.getVersionIdPart());
        else
            referenceBinary = client.read(Binary.class, dataReference.getIdPart());
        logger.info("Data downloaded. -> {}", referenceBinary.toString());

        variables.setByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED, referenceBinary.getData());

        logger.info("Data stored for Decryption.");
    }


}
