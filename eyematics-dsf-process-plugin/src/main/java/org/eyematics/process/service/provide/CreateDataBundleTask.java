package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.bpe.PatientId;
import org.eyematics.process.utils.delegate.AbstractExtendedProcessServiceDelegate;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;


public class CreateDataBundleTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateDataBundleTask.class);
    private final List<String> provideMailConfigAdresses;

    public CreateDataBundleTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator,
                                List<String> provideMailConfigAdresses) {
        super(api, dataSetStatusGenerator);
        this.provideMailConfigAdresses = provideMailConfigAdresses;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Bundling the local data, also by replacing dic pseudonym by global pseudonym.");
        try {
            Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
            HashMap<String, String> globalPseudonymMap = new HashMap<>();
            HashSet<String> globalPseudonymSet = new HashSet<>();

            Bundle observations = variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET);
            Bundle medications = variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET);
            Bundle medicationAdministrations = variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET);
            Bundle medicationRequests = variables
                    .getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET);

            Bundle eyeMaticsBundle = new Bundle().setType(TRANSACTION);
            Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
            brc.setMethod(Bundle.HTTPVerb.PUT);
            patients.getEntry().forEach(e -> {
                Patient p = this.transformToPatient(e);
                String dicPseudonym = this.getDICPseudonym(p);
                String bloomFilter = this.getBloomFilter(p);
                String globalPseudonym = this.getGlobalPseudonym(p);
                if (dicPseudonym != null && bloomFilter != null && globalPseudonym != null) {
                    globalPseudonymMap.put(dicPseudonym, globalPseudonym);
                    globalPseudonymSet.add(globalPseudonym);
                    if (this.replaceIdentifierPatientResource(p, dicPseudonym, globalPseudonym)) {
                        eyeMaticsBundle.addEntry().setResource(p).setRequest(this.getBundleRequest(brc, p));
                    }
                }
            });

            observations.getEntry().forEach(e ->
                    this.processResource(e.getResource(), brc, globalPseudonymMap, eyeMaticsBundle));
            medications.getEntry().forEach(e ->
                    eyeMaticsBundle.addEntry()
                            .setResource(e.getResource()).setRequest(this.getBundleRequest(brc, e.getResource())));
            medicationAdministrations.getEntry().forEach(e ->
                    this.processResource(e.getResource(), brc, globalPseudonymMap, eyeMaticsBundle));
            medicationRequests.getEntry().forEach(e ->
                    this.processResource(e.getResource(), brc, globalPseudonymMap, eyeMaticsBundle));

            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, eyeMaticsBundle);
            this.sendGlobalPseudonymMail(globalPseudonymSet, variables.getStartTask());
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not bundle data: {}", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.DATA_BUNDLE_FAILURE, variables, errorMessage);
        }
    }

    private Bundle.BundleEntryRequestComponent getBundleRequest(Bundle.BundleEntryRequestComponent brc,
                                                                Resource resource) {
        return brc.copy().setUrl(resource.getResourceType().name() + "/" + resource.getIdElement().getIdPart());
    }

    private Patient transformToPatient(Bundle.BundleEntryComponent patient) {
        if (patient.hasResource() && patient.getResource() instanceof Patient) {
            return (Patient) patient.getResource();
        }
        return null;
    }

    private String getDICPseudonym(Patient patient) {
        if  (patient == null) return null;
        return patient.getIdentifier().stream().filter(pi ->
                        pi.getSystem().equals(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_DIC_PSEUDONYM))
                .map(Identifier::getValue)
                .toList()
                .get(0);
    }

    private String getBloomFilter(Patient patient) {
        if  (patient == null) return null;
        return patient.getIdentifier().stream().filter(pi ->
                        pi.getSystem().equals(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_BLOOM_FILTER))
                .map(Identifier::getValue)
                .toList()
                .get(0);
    }

    private String getGlobalPseudonym(Patient patient) {
        if  (patient == null) return null;
        return patient.getIdentifier().stream().filter(pi ->
                        pi.getSystem().equals(EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_GLOBAL_PSEUDONYM))
                .map(Identifier::getValue)
                .map(s -> s.replace('_', '-'))
                .toList()
                .get(0);
    }

    private boolean replaceIdentifierPatientResource(Patient patient, String dicPseudonym, String globalPseudonym) {
        boolean dicPsnRemoved = patient.getIdentifier().removeIf(i ->
                EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_DIC_PSEUDONYM.equals(i.getSystem()));
        boolean bloomFilterRemoved = patient.getIdentifier().removeIf(i ->
                EyeMaticsConstants.NAMING_SYSTEM_EYEMATICS_BLOOM_FILTER.equals(i.getSystem()));
        if (patient.getIdElement().getIdPart().equals(dicPseudonym)) {
            patient.setId(globalPseudonym);
            return dicPsnRemoved && bloomFilterRemoved;
        }
        return false;
    }

    private void processResource(Resource resource, Bundle.BundleEntryRequestComponent brc,
                                 HashMap<String, String> globalPseudonymMap, Bundle eyeMaticsBundle) {
        if (replaceIdentifierResource(resource, globalPseudonymMap)) {
            eyeMaticsBundle.addEntry().setResource(resource).setRequest(this.getBundleRequest(brc, resource));
        }
    }

    private boolean replaceIdentifierResource(Resource resource, HashMap<String, String> globalPseudonymMap) {
        return switch (resource.getResourceType()) {
            case Observation ->
                    this.replaceIdentifierObservationResource(resource, globalPseudonymMap);
            case MedicationRequest ->
                    this.replaceIdentifierMedicationRequestResource(resource, globalPseudonymMap);
            case MedicationAdministration ->
                    this.replaceIdentifierMedicationAdministrationResource(resource, globalPseudonymMap);
            default -> false;
        };
    }

    private boolean replaceIdentifierObservationResource(Resource resource,
                                                         HashMap<String, String> globalPseudonymMap) {
        if (resource instanceof Observation o && o.hasSubject()) {
            String dicPseudonym = PatientId.extract(o.getSubject().getReference());
            String globalPseudonym = globalPseudonymMap.get(dicPseudonym);
            if (globalPseudonym == null) return false;
            o.getSubject().setReference("Patient/" + globalPseudonym);
            return true;
        }
        return false;
    }

    private boolean replaceIdentifierMedicationRequestResource(Resource resource,
                                                               HashMap<String, String> globalPseudonymMap) {
        if (resource instanceof MedicationRequest mr && mr.hasSubject()) {
            String dicPseudonym = PatientId.extract(mr.getSubject().getReference());
            String globalPseudonym = globalPseudonymMap.get(dicPseudonym);
            if (globalPseudonym == null) return false;
            mr.getSubject().setReference("Patient/" + globalPseudonym);
            return true;
        }
        return false;
    }

    private boolean replaceIdentifierMedicationAdministrationResource(Resource resource,
                                                                      HashMap<String, String> globalPseudonymMap) {
        if (resource instanceof MedicationAdministration ma && ma.hasSubject()) {
            String dicPseudonym = PatientId.extract(ma.getSubject().getReference());
            String globalPseudonym = globalPseudonymMap.get(dicPseudonym);
            if (globalPseudonym == null) return false;
            ma.getSubject().setReference("Patient/" + globalPseudonym);
            return true;
        }
        return false;
    }

    private void sendGlobalPseudonymMail(HashSet<String> globalPseudonymSet, Task task) {
        if (!this.provideMailConfigAdresses.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Dear Admin, \n\n");
            message.append("an EyeMatics data exchange has been conducted.\n\n");
            message.append("Task Id: ");
            message.append(task.getId());
            message.append("\n");
            message.append("Requester: ");
            message.append(task.getRequester().getIdentifier().getValue());
            message.append("\n");
            message.append("Provider: ");
            message.append(task.getRestriction().getRecipientFirstRep().getIdentifier().getValue());
            message.append("\n\n");
            message.append("Data from following global pseudonyms where exchanged:\n\n");
            message.append(globalPseudonymSet);
            this.api.getMailService().send("EyeMatics Data Exchange: Global pseudonyms",
                    message.toString(),
                    this.provideMailConfigAdresses);
        }
    }

}
