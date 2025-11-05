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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;


public class CreateDataBundleTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateDataBundleTask.class);

    public CreateDataBundleTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Bundling the local data");
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
            Bundle observations = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET);
            Bundle medications = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET);
            Bundle medicationAdministrations = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET);
            Bundle medicationRequests = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET);

            Bundle eyeMaticsBundle = new Bundle().setType(TRANSACTION);
            Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
            brc.setMethod(Bundle.HTTPVerb.PUT);
            patients.getEntry().forEach(e -> eyeMaticsBundle.addEntry().setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));
            observations.getEntry().forEach(e -> eyeMaticsBundle.addEntry().setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));
            medications.getEntry().forEach(e -> eyeMaticsBundle.addEntry().setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));
            medicationAdministrations.getEntry().forEach(e -> eyeMaticsBundle.addEntry().setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));
            medicationRequests.getEntry().forEach(e -> eyeMaticsBundle.addEntry().setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, eyeMaticsBundle);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not bundle data: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_BUNDLE_FAILURE, variables, errorMessage);
        }
    }

    private Bundle.BundleEntryRequestComponent getBundleRequest(Bundle.BundleEntryRequestComponent brc, Resource resource) {
        return brc.copy().setUrl(resource.getResourceType().name() + "/" + resource.getIdElement().getIdPart());
    }
}
