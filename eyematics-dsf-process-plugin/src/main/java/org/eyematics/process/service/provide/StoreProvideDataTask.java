package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import jakarta.ws.rs.core.MediaType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class StoreProvideDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(StoreProvideDataTask.class);

    public StoreProvideDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to store");
        MediaType mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM);
        byte[] content = {};
        String targetOrganizationIdentifier = variables.getTarget().getOrganizationIdentifierValue();
        String localOrganizationIdentifier = null;

        try {
            content = variables.getByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED);
            localOrganizationIdentifier = this.api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElseThrow();
        } catch (Exception exception) {
            this.handleStoreError(exception, variables);
        }

        try (InputStream in = new ByteArrayInputStream(content)) {
            IdType created = this.storeBinaryData(in, mediaType, targetOrganizationIdentifier);
            String idTypeVar = created.toString();

            logger.info("Target-Organization-Value -> {}", targetOrganizationIdentifier);
            logger.info("Local-Organization-Value -> {}", localOrganizationIdentifier);
            logger.info("IdType -> {}", idTypeVar);
            logger.info("Security Context -> {}", this.getSecurityContext(targetOrganizationIdentifier));

            variables.setString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE, idTypeVar);
        } catch (Exception exception) {
            this.handleStoreError(exception, variables);
        }
    }

    private IdType storeBinaryData(InputStream in, MediaType mediaType, String targetOrganizationIdentifier) {
        return api.getFhirWebserviceClientProvider()
                .getLocalWebserviceClient()
                .withMinimalReturn()
                .createBinary(in, mediaType, this.getSecurityContext(targetOrganizationIdentifier));
    }

    private void handleStoreError(Exception exception, Variables variables) {
        String errorMessage = exception.getMessage();
        logger.error("Could not store dataset: {}", errorMessage);
        this.handleTaskError(EyeMaticsGenericStatus.DATA_STORE_FAILURE, variables, errorMessage);
    }

    private String getSecurityContext(String dicIdentifier) {
        return api.getOrganizationProvider().getOrganization(dicIdentifier)
                .orElseThrow(() -> new RuntimeException("Could not find organization with id '" + dicIdentifier + "'"))
                .getIdElement().toVersionless().getValue();
    }

}
