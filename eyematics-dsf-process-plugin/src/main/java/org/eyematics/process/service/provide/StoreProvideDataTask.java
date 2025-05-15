package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.eyematics.process.constant.ProvideConstants;
import jakarta.ws.rs.core.MediaType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.eyematics.process.constant.EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE;

public class StoreProvideDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(StoreProvideDataTask.class);

    public StoreProvideDataTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("StoreProvideDataTask -> something to store");
        MediaType mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM);
        byte[] content = variables.getByteArray(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED);
        String targetOrganizationIdentifier = variables.getTarget().getOrganizationIdentifierValue();
        String localOrganizationIdentifier = api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElse(null);
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
            variables.setString(BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE, idTypeVar);
        }
        catch (Exception exception) {
            logger.warn("Could not create binary -> {}", exception.getMessage());
        }
    }

    private String getSecurityContext(String dmsIdentifier) {
        return api.getOrganizationProvider().getOrganization(dmsIdentifier)
                .orElseThrow(() -> new RuntimeException("Could not find organization with id '" + dmsIdentifier + "'"))
                .getIdElement().toVersionless().getValue();
    }

}
