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
        logger.info("-> Storing the encrypted local data on FHIR server.");
        MediaType mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM);
        String targetOrganizationIdentifier = variables.getTarget().getOrganizationIdentifierValue();

        try {
            Bundle referenceBundle =
                    variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE_BUNDLE);
            if (referenceBundle == null) throw new Exception("Reference Bundle is null");

            referenceBundle.getEntry()
                    .stream()
                    .filter(e -> e.getResource().getResourceType().equals(ResourceType.Bundle))
                    .map(e -> (Bundle) e.getResource())
                    .forEach(b -> {
                        String particularId = b.getIdElement().getIdPart();
                        byte[] bundleEncrypted = this.getByte(variables,
                                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, particularId);

                        try (InputStream in = new ByteArrayInputStream(bundleEncrypted)) {
                            IdType created = this.storeBinaryData(in, mediaType, targetOrganizationIdentifier);
                            String idTypeVar = created.toString();
                            b.addEntry().setResource(new Basic().setSubject(new Reference(idTypeVar)));

                        } catch (Exception exception) {
                            this.handleStoreError(exception, variables);
                        }
                    });

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE_BUNDLE,
                    referenceBundle);

        } catch (Exception exception) {
            this.handleStoreError(exception, variables);
        }
    }

    private IdType storeBinaryData(InputStream in, MediaType mediaType, String targetOrganizationIdentifier) {
        return this.api.getFhirWebserviceClientProvider()
                .getLocalWebserviceClient()
                .withMinimalReturn()
                .createBinary(in, mediaType, this.getSecurityContext(targetOrganizationIdentifier));
    }

    private void handleStoreError(Exception exception, Variables variables) {
        String errorMessage = exception.getMessage();
        logger.error("Could not store dataset: {}.", errorMessage);
        this.handleTaskError(EyeMaticsGenericStatus.DATA_STORE_FAILURE, variables, errorMessage);
    }

    private String getSecurityContext(String dicIdentifier) {
        return this.api.getOrganizationProvider().getOrganization(dicIdentifier)
                .orElseThrow(() -> new RuntimeException("Could not find organization with id '" + dicIdentifier + "'"))
                .getIdElement().toVersionless().getValue();
    }

}
