package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.client.FTTPClient;
import org.eyematics.process.utils.client.FTTPClientFactory;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessGlobalPseudonymTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ProcessGlobalPseudonymTask.class);
    private final FTTPClientFactory fttpClientFactory;

    public ProcessGlobalPseudonymTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator,
                                      FTTPClientFactory fttpClientFactory) {
        super(api, dataSetStatusGenerator);
        this.fttpClientFactory = fttpClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fttpClientFactory, "fttpClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Requesting and processing global pseudonyms for patient data");
        FTTPClient fttpClient = this.fttpClientFactory.getFTTPClient();
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
            List<String> bloomfilterList = patients.getEntry()
                    .stream()
                    .map(this::getBloomfilterFromBundleEntry)
                    .toList();
            HashSet<String> bloomfilter = new HashSet<>(bloomfilterList);
            Optional<HashMap<String, String>> globalPseudonyms = fttpClient.getGlobalPseudonym(bloomfilter);
            HashMap<String, String> globalPseudonymMap = globalPseudonyms.orElseThrow();
            List<Bundle.BundleEntryComponent> pseudonymizedPatients = patients.getEntry()
                    .stream()
                    .map(p -> this.setGlobalPseudonymToBundleEntry(p, globalPseudonymMap))
                    .toList();
            patients.getEntry().clear();
            patients.getEntry().addAll(pseudonymizedPatients);
            delegateExecution.setVariable(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patients);
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not process global pseudonyms from fTTP: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.PSEUDONYM_PROCESS_FAILURE, variables, errorMessage);
        }
    }

    private Patient transformToPatient(Bundle.BundleEntryComponent patient) {
        if (patient.hasResource() && patient.getResource() instanceof Patient) {
            return (Patient) patient.getResource();
        }
        return null;
    }

    private String getBloomfilterFromBundleEntry(Bundle.BundleEntryComponent patient) {
        Patient p = this.transformToPatient(patient);
        if (p != null && p.hasIdentifier()) {
            return p.getIdentifier()
                    .stream()
                    .filter(pi -> pi.getSystem().equals(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_BLOOM_FILTER))
                    .map(Identifier::getValue)
                    .toList()
                    .get(0);
        }
        return null;
    }

    private Bundle.BundleEntryComponent setGlobalPseudonymToBundleEntry(Bundle.BundleEntryComponent patient,
                                                                        HashMap<String, String> globalPseudonymMap) {
        Patient p = this.transformToPatient(patient);
        if (p != null && p.hasIdentifier()) {
            String bloomfilter = p.getIdentifier()
                    .stream()
                    .filter(pi -> pi.getSystem().equals(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_BLOOM_FILTER))
                    .map(Identifier::getValue)
                    .toList()
                    .get(0);
            if (bloomfilter == null) return null;
            String globalPseudonym = globalPseudonymMap.get(bloomfilter);
            if (globalPseudonym == null) return null;
            p.getIdentifier().add(new Identifier()
                    .setSystem(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_GLOBAL_PSEUDONYM)
                    .setValue(globalPseudonym));
            return patient;
        }
        return null;
    }
}
