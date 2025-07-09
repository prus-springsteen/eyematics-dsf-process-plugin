package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import jakarta.ws.rs.core.MediaType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.generator.AbstractExtendedServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StoreProvideDataTask extends AbstractExtendedServiceDelegate {

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
            logger.error("Could not get data to store on server: {}", exception.getMessage());
            super.handleTaskError(EyeMaticsGenericStatus.DATA_STORE_FAILED, variables, exception, "Data Store Failed");
        }

        try (InputStream in = new ByteArrayInputStream(content)) {
            IdType created = api.getFhirWebserviceClientProvider()
                                .getLocalWebserviceClient()
                                .withMinimalReturn()
                                .createBinary(in, mediaType, this.getSecurityContext(targetOrganizationIdentifier));
            String idTypeVar = new IdType(api.getFhirWebserviceClientProvider().getLocalWebserviceClient().getBaseUrl(),
                                          ResourceType.Binary.name(),
                                          created.getIdPart(),
                                          created.getVersionIdPart()).getValue();
            logger.info("Target-Organization-Value -> {}", targetOrganizationIdentifier);
            logger.info("Local-Organization-Value -> {}", localOrganizationIdentifier);
            logger.info("IdType -> {}", idTypeVar);
            logger.info("Security Context -> {}", this.getSecurityContext(targetOrganizationIdentifier));
            variables.setString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE, idTypeVar);
        } catch (Exception exception) {
            logger.error("Could not store data on server: {}", exception.getMessage());
            super.handleTaskError(EyeMaticsGenericStatus.DATA_STORE_FAILED, variables, exception, "Data Store Failed");
        }
    }

    private String getSecurityContext(String dicIdentifier) {
        return api.getOrganizationProvider().getOrganization(dicIdentifier)
                .orElseThrow(() -> new RuntimeException("Could not find organization with id '" + dicIdentifier + "'"))
                .getIdElement().toVersionless().getValue();
    }

}
