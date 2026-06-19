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
import org.eyematics.process.utils.pseudonymize.EyeMaticsMdatPseudonymizer;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


public class PseudonymizeDataTask extends AbstractExtendedProcessServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PseudonymizeDataTask.class);
    private final EyeMaticsMdatPseudonymizer eyeMaticsMdatPseudonymizer;
    private final long acknowledgementTimerDurationPerPatient;

    public PseudonymizeDataTask(ProcessPluginApi api,
                                DataSetStatusGenerator dataSetStatusGenerator,
                                EyeMaticsMdatPseudonymizer eyeMaticsMdatPseudonymizer,
                                long acknowledgementTimerDurationPerPatient) {
        super(api, dataSetStatusGenerator);
        this.eyeMaticsMdatPseudonymizer = eyeMaticsMdatPseudonymizer;
        this.acknowledgementTimerDurationPerPatient = acknowledgementTimerDurationPerPatient;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Pseudonymizing the data for provision.");
        try {
            Organization organization = this.getLocalEyeMaticsOrganization();
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_ORGANIZATION_DATA_SET, organization);

            this.pseudonymizeAndStoreMeasureReports(variables, organization);
            Map<String, Reference> medicationsPseudonymizedReferenceMap =
                    this.pseudonymizeAndStoreMedication(variables, organization);

            Map<String, Reference> patientsPseudonymizedReferenceMap =
                    this.pseudonymizeAndStorePatient(variables, organization);

            patientsPseudonymizedReferenceMap.forEach((patientId, patientReference) -> {
                Map<String, Reference> observationsPseudonymizedReferenceHashMap =
                        this.pseudonymizeAndStoreObservation(patientId, patientReference, variables, organization);
                this.pseudonymizeAndStoreDiagnosticReport(patientId,
                        patientReference, observationsPseudonymizedReferenceHashMap, variables, organization);
                this.pseudonymizeAndStoreMedicationAdministration(patientId,
                        patientReference, medicationsPseudonymizedReferenceMap, variables, organization);
                this.pseudonymizeAndStoreMedicationRequest(patientId,
                        patientReference, medicationsPseudonymizedReferenceMap, variables, organization);
            });

            long timerDuration = (long) patientsPseudonymizedReferenceMap.size()
                    * 1000 * this.acknowledgementTimerDurationPerPatient;
            long minTimerDuration = 1000 * 60 * 5;
            if (timerDuration < minTimerDuration) timerDuration = minTimerDuration;
            String timerDurationConversion = Duration.ofMillis(timerDuration).toString();
            variables.setString(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_ACKNOWLEDGEMENT_WAITING_DURATION,
                    timerDurationConversion);

        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            logger.error("Could not pseudonymize the data for provision: {}.", errorMessage);
            this.handleTaskError(EyeMaticsGenericStatus.PSEUDONYM_PROCESS_FAILURE, variables, errorMessage);
        }
    }

    private Organization getLocalEyeMaticsOrganization() {
        Optional<Organization> localOrganization = this.api.getOrganizationProvider().getLocalOrganization();
        Organization eyeMaticsOrganization = new Organization();
        eyeMaticsOrganization.setId(UUID.randomUUID().toString());
        if (localOrganization.isEmpty()) {
            eyeMaticsOrganization.setName("Unknown Organization");
            return eyeMaticsOrganization;
        }
        Organization dsfOrganization = localOrganization.get();
        Optional<String> id =
                this.eyeMaticsMdatPseudonymizer.pseudonymize(EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE
                + dsfOrganization.getName());
        id.ifPresent(eyeMaticsOrganization::setId);
        eyeMaticsOrganization.getMeta().addProfile(EyeMaticsConstants.EYEMATICS_ORGANIZATION_PROFILE);
        eyeMaticsOrganization.setName(dsfOrganization.getName());
        eyeMaticsOrganization.setAddress(dsfOrganization.getAddress());
        eyeMaticsOrganization.setTelecom(dsfOrganization.getTelecom());
        eyeMaticsOrganization.setIdentifier(dsfOrganization.getIdentifier());
        return eyeMaticsOrganization;
    }

    private void pseudonymizeAndStoreMeasureReports(Variables variables, Organization organization) {
        Bundle measureReports =
                variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEASURE_REPORT_DATA_SET);
        if (measureReports != null) {
            List<Bundle.BundleEntryComponent> pseudonymizedMeasureReports = measureReports.getEntry()
                    .stream()
                    .parallel()
                    .filter(e -> e.getResource().getResourceType()
                            .equals(ResourceType.MeasureReport))
                    .map(e -> (MeasureReport) e.getResource())
                    .filter(MeasureReport::hasIdElement)
                    .filter(m -> this.eyeMaticsMdatPseudonymizer
                            .pseudonymize(m, organization).orElse(null) != null)
                    .map(m -> {
                        String measureReportPseudonym = this.eyeMaticsMdatPseudonymizer.pseudonymize(m)
                                .orElse(null);
                        if (measureReportPseudonym == null) return null;
                        m.setId(measureReportPseudonym);
                        m.getMeta()
                                .getProfile()
                                .removeIf(profile -> !profile.getValue()
                                        .equals(EyeMaticsConstants.EYEMATICS_IVI_MEASURE_REPORT_PROFILE));
                        m.getIdentifier().clear();
                        return new Bundle.BundleEntryComponent().setResource(m);
                    })
                    .filter(Objects::nonNull)
                    .toList();
            measureReports.setEntry(pseudonymizedMeasureReports);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEASURE_REPORT_DATA_SET,
                    measureReports);
        }
    }

    private Map<String, Reference> pseudonymizeAndStoreMedication(Variables variables, Organization organization) {
        Bundle medications =
                variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET);
        if (medications == null) return new HashMap<>();
        Bundle medicationsPseudonymized = new Bundle();
        Map<String, Reference> medicationsPseudonymizedReferenceMap = medications.getEntry()
                .stream()
                .parallel()
                .filter(me -> me.getResource().getResourceType()
                        .equals(ResourceType.Medication))
                .map(me -> (Medication) me.getResource())
                .filter(Medication::hasIdElement)
                .filter(m -> this.eyeMaticsMdatPseudonymizer.pseudonymize(m, organization)
                        .orElse(null) != null)
                .map(m -> {
                    String medicationPseudonym = this.eyeMaticsMdatPseudonymizer.pseudonymize(m).orElse(null);
                    if (medicationPseudonym == null) return null;
                    m.setId(medicationPseudonym);
                    m.getMeta()
                            .getProfile()
                            .removeIf(profile -> !profile.getValue()
                                    .equals(EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI
                                            + EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_PROFILE));
                    m.getIdentifier().clear();
                    medicationsPseudonymized.addEntry().setResource(m);
                    return m;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(m -> m.getIdElement().toUnqualifiedVersionless().getValue(),
                        Reference::new));
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET,
                medicationsPseudonymized);
        return medicationsPseudonymizedReferenceMap;
    }

    private Map<String, Reference> pseudonymizeAndStorePatient(Variables variables,
                                                               Organization organization) {
        Bundle patients = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET);
        if (patients == null) return new HashMap<>();
        String organizationName = organization.getName();
        Bundle patientsPseudonymized = new Bundle().setType(Bundle.BundleType.COLLECTION);
        Map<String, Reference> patientsPseudonymizedReferenceMap = new HashMap<>();
        patients.getEntry()
                .stream()
                .filter(be -> be.getResource().getResourceType()
                        .equals(ResourceType.Patient))
                .map(be -> (Patient) be.getResource())
                .filter(Patient::hasIdElement)
                .forEach(p -> {
                    String patientId = p.getIdElement().getIdPart();
                    Identifier gpasIdentifier = p.getIdentifier()
                            .stream()
                            .filter(Identifier::hasSystem)
                            .filter(pi -> pi.getSystem()
                                    .equals(EyeMaticsConstants.IDENTIFIER_CODE_SYSTEM_EYEMATICS_GLOBAL_PSEUDONYM))
                            .findFirst()
                            .orElse(null);

                    if (gpasIdentifier != null && gpasIdentifier.hasValue()) {
                        String gpasValue = this.eyeMaticsMdatPseudonymizer.pseudonymize(organizationName + "_"
                                + gpasIdentifier.getValue()).orElse(null);
                        if (gpasValue != null) {
                            p.setId(gpasValue);
                            p.setMeta(new Meta().addProfile(EyeMaticsConstants.PATIENT_MII_PROFILE));
                            p.getIdentifier().clear();
                            p.getIdentifier().add(gpasIdentifier);
                            Reference managingOrganizationReference = new Reference("Organization/"
                                    + organization.getId());
                            p.setManagingOrganization(managingOrganizationReference);
                            patientsPseudonymized.addEntry().setResource(p);
                            Reference patientReference = new Reference(p).setReference(gpasValue);
                            patientsPseudonymizedReferenceMap.put(patientId, patientReference);
                        }
                    }
                });
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET, patientsPseudonymized);
        return patientsPseudonymizedReferenceMap;
    }

    private Map<String, Reference> pseudonymizeAndStoreObservation(String patientId,
                                                                   Reference patientReference,
                                                                   Variables variables,
                                                                   Organization organization) {
        Bundle observations = this.getResource(variables,
                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET, patientId);
        if (observations == null) return new HashMap<>();
        Bundle observationsPseudonymized = new Bundle();
        Map<String, Reference> observationsPseudonymizedReferenceMap = new HashMap<>();
        observations.getEntry()
                .stream()
                .filter(observationConsentedBundleEntry ->
                        observationConsentedBundleEntry.getResource().getResourceType()
                                .equals(ResourceType.Observation))
                .map(observationConsentedBundleEntry ->
                        (Observation) observationConsentedBundleEntry.getResource())
                .filter(Observation::hasIdElement)
                .forEach(o -> {
                    String observationPseudonym = this.eyeMaticsMdatPseudonymizer.pseudonymize(o, organization)
                            .orElse(null);
                    if (observationPseudonym != null) {
                        o.setId(observationPseudonym);
                        o.getIdentifier().clear();
                        o.getMeta().getProfile().removeIf(profile -> {
                            for (String observationProfile : EyeMaticsConstants.EYEMATICS_CORE_DATASET_OBSERVATION_PROFILE) {
                                if (profile.getValue()
                                        .equals(EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI + observationProfile))
                                    return false;
                            }
                            return true;
                        });
                        o.setSubject(patientReference);
                        observationsPseudonymizedReferenceMap.put(o.getIdElement()
                                .toUnqualifiedVersionless().getValue(), new Reference(o));
                        observationsPseudonymized.addEntry().setResource(o);
                    }
                });

        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                patientReference.getReferenceElement().getValue(), observationsPseudonymized);
        this.setResource(variables, ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET,
                patientId, null);
        return observationsPseudonymizedReferenceMap;
    }

    private void pseudonymizeAndStoreDiagnosticReport(String patientId,
                                                      Reference patientReference,
                                                      Map<String, Reference> observationsPseudonymizedReferenceMap,
                                                      Variables variables,
                                                      Organization organization) {
        Bundle diagnosticReports = this.getResource(variables,
                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET, patientId);

        if (diagnosticReports != null) {
            List<Bundle.BundleEntryComponent> pseudonymizedDiagnosticReports = diagnosticReports.getEntry()
                    .stream()
                    .parallel()
                    .filter(e -> e.getResource().getResourceType()
                            .equals(ResourceType.DiagnosticReport))
                    .map(e -> (DiagnosticReport) e.getResource())
                    .filter(DiagnosticReport::hasIdElement)
                    .map(dr -> {
                        String diagnosticReportPseudonym = this.eyeMaticsMdatPseudonymizer.pseudonymize(dr, organization)
                                .orElse(null);
                        if (diagnosticReportPseudonym == null) return null;
                        dr.setId(diagnosticReportPseudonym);
                        dr.getIdentifier().clear();
                        dr.getMeta()
                                .getProfile()
                                .removeIf(profile -> !profile.getValue()
                                        .equals(EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI
                                                + EyeMaticsConstants.EYEMATICS_CORE_DATASET_DIAGNOSTIC_REPORT_PROFILE));
                        dr.setSubject(patientReference);
                        List<Reference> results = dr.getResult()
                                .stream()
                                .filter(result ->
                                        observationsPseudonymizedReferenceMap.containsKey(result.getReference()))
                                .map(result -> observationsPseudonymizedReferenceMap.get(result.getReference()))
                                .toList();
                        dr.getResult().clear();
                        dr.getResult().addAll(results);
                        return new Bundle.BundleEntryComponent().setResource(dr);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            diagnosticReports.setEntry(pseudonymizedDiagnosticReports);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET,
                    patientReference.getReferenceElement().getValue(), diagnosticReports);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DIAGNOSTIC_REPORT_DATA_SET,
                    patientId, null);
        }
    }

    private void pseudonymizeAndStoreMedicationAdministration(String patientId,
                                                              Reference patientReference,
                                                              Map<String, Reference> medicationsPseudonymizedReferenceMap,
                                                              Variables variables,
                                                              Organization organization) {
        Bundle medicationAdministrations = this.getResource(variables,
                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET, patientId);

        if (medicationAdministrations != null) {
            List<Bundle.BundleEntryComponent> pseudonymizedMedicationAdministration = medicationAdministrations.getEntry()
                    .stream()
                    .parallel()
                    .filter(e -> e.getResource().getResourceType()
                            .equals(ResourceType.MedicationAdministration))
                    .map(e -> (MedicationAdministration) e.getResource())
                    .filter(MedicationAdministration::hasIdElement)
                    .map(ma -> {
                        String medicationAdministrationPseudonym =
                                this.eyeMaticsMdatPseudonymizer.pseudonymize(ma, organization).orElse(null);
                        if (medicationAdministrationPseudonym == null) return null;
                        ma.setId(medicationAdministrationPseudonym);
                        ma.getIdentifier().clear();
                        ma.getMeta()
                                .getProfile()
                                .removeIf(profile -> !profile.getValue()
                                        .equals(EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI
                                                + EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_ADMINISTRATION_PROFILE));
                        ma.setMedication(medicationsPseudonymizedReferenceMap.get(ma.getMedicationReference().getReference()));
                        ma.setSubject(patientReference);
                        return new Bundle.BundleEntryComponent().setResource(ma);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            medicationAdministrations.setEntry(pseudonymizedMedicationAdministration);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET,
                    patientReference.getReferenceElement().getValue(), medicationAdministrations);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET,
                    patientId, null);
        }
    }

    private void pseudonymizeAndStoreMedicationRequest(String patientId,
                                                       Reference patientReference,
                                                       Map<String, Reference> medicationsPseudonymizedReferenceMap,
                                                       Variables variables,
                                                       Organization organization) {
        Bundle medicationRequests = this.getResource(variables,
                ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET, patientId);

        if (medicationRequests != null) {
            List<Bundle.BundleEntryComponent> pseudonymizedMedicationRequest = medicationRequests.getEntry()
                    .stream()
                    .parallel()
                    .filter(e -> e.getResource().getResourceType()
                            .equals(ResourceType.MedicationRequest))
                    .map(e -> (MedicationRequest) e.getResource())
                    .filter(MedicationRequest::hasIdElement)
                    .map(mr -> {
                        String medicationRequestPseudonym =
                                this.eyeMaticsMdatPseudonymizer.pseudonymize(mr, organization).orElse(null);
                        if (medicationRequestPseudonym == null) return null;
                        mr.setId(medicationRequestPseudonym);
                        mr.getIdentifier().clear();
                        mr.getMeta()
                                .getProfile()
                                .removeIf(profile -> !profile.getValue()
                                        .equals(EyeMaticsConstants.EYEMATICS_CORE_DATA_SET_URI
                                                + EyeMaticsConstants.EYEMATICS_CORE_DATASET_MEDICATION_REQUEST_PROFILE));
                        mr.setMedication(medicationsPseudonymizedReferenceMap.get(mr.getMedicationReference()
                                .getReference()));
                        mr.setSubject(patientReference);
                        return new Bundle.BundleEntryComponent().setResource(mr);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            medicationRequests.setEntry(pseudonymizedMedicationRequest);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET,
                    patientReference.getReferenceElement().getValue(), medicationRequests);
            this.setResource(variables,
                    ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET,
                    patientId, null);
        }
    }
}
