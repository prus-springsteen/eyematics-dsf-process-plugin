package org.eyematics.process.service.provide;

import java.io.*;
import java.util.Objects;
import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.client.BinaryStreamFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.generator.AbstractExtendedServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// https://github.com/medizininformatik-initiative/dsf-plugin-numdashboard/blob/main/src/main/java/de/medizininformatik_initiative/process/report/service/CreateJson.java
// https://www.baeldung.com/java-9-http-client
// Download with Pause and Resume...
// https://www.baeldung.com/spring-resttemplate-download-large-file
public class ReadProvideDataTask extends AbstractExtendedServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadProvideDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadProvideDataTask(ProcessPluginApi api, FhirClientFactory fhirClientFactory, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
        this.fhirClientFactory = fhirClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("Reading Data from FHIR-Repository...");
        // Klappt mit BinaryStreamFhirClient ABER nicht mit StandardFhirClientImpl ...
        // https://github.com/medizininformatik-initiative/mii-processes-common/tree/develop/src/main/java/de/medizininformatik_initiative/processes/common/fhir/client
        // https://github.com/medizininformatik-initiative/mii-process-data-transfer/blob/issues/36_multiple_attachments/src/main/java/de/medizininformatik_initiative/process/data_transfer/service/EncryptAndStoreData.java#L352
        BinaryStreamFhirClient fhirClient = this.fhirClientFactory.getBinaryStreamFhirClient();
        IdType idType = new IdType(this.fhirClientFactory.getFhirBaseUrl(),
                "Bundle", "StructureDefinition", "");
        Bundle bundle = new Bundle();

        try (InputStream  in = fhirClient.readBundle(idType, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_XML)) {
            logger.info("Stream -> {}", in);
            FhirContext fhirContext = FhirContext.forR4();
            bundle = fhirContext.newXmlParser().parseResource(Bundle.class, in);
        } catch (Exception exception) {
            logger.error("Could not read data from FHIR-Repository: {}", exception.getMessage());
            super.handleTaskError(EyeMaticsGenericStatus.DATA_READ_FAILED, variables, exception, "Read Data Failed");
        }

        logger.info("Data -> {}", bundle);
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
    }
}
