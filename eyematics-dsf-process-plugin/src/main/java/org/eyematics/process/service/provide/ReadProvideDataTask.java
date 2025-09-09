package org.eyematics.process.service.provide;

import java.util.Objects;
import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.client.*;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadProvideDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadProvideDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadProvideDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, FhirClientFactory fhirClientFactory) {
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
        logger.info("Reading Data from FHIR-Repository is initiated.");
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        IdType idType = new IdType(fhirClient.getFhirBaseUrl(),  "StructureDefinition", "", "");
        try {
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, fhirClient.read(idType, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON));
            if (!bundle.hasEntry()) {
                throw new Exception("Bundle contains no data. Please check the FHIR-Repository.");
            }
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
            logger.info("Data is stored for further processing.");
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not read data from FHIR-Repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_READ_FAILURE, variables, errorMessage);
        }
    }
}
