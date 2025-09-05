package org.eyematics.process.service.receive;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ReceiveConstants;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedSubProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public class InsertRequestedDataTask extends AbstractExtendedSubProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(InsertRequestedDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public InsertRequestedDataTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator, FhirClientFactory fhirClientFactory) {
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
        logger.info("Inserting data from ...");
        String correlationKey = this.api.getVariables(delegateExecution).getTarget().getCorrelationKey();
        EyeMaticsFhirClient fhirClient = this.fhirClientFactory.getEyeMaticsFhirClient();
        IdType idType = new IdType(fhirClient.getFhirBaseUrl(),  "Bundle", "", "");
        try {
            Bundle bundle = variables.getResource(ReceiveConstants.BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET + correlationKey);
            FhirContext fhirContext = FhirContext.forR4();
            String bundleString = fhirContext.newJsonParser().encodeResourceToString(bundle);
            MethodOutcome methodOutcome = fhirClient.create(idType, bundleString, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
           logger.info("Data is inserted for further processing. {}", methodOutcome);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not insert data to FHIR-Repository: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_INSERT_FAILURE, variables, errorMessage);
        }
    }
}
