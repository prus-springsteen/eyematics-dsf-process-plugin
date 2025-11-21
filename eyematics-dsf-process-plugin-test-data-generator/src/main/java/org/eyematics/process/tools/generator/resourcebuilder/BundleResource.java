package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BundleResource extends AbstractFHIRResourceBuilder<Bundle, BundleResource>{

    private int patientId;
    private int maxPatientId;
    private String source;
    private int amountMedication;
    private int maxMedication;
    private int amountObservation;
    private int maxObservation;
    private int amountMedicationRequest;
    private int amountMedicationAdministration;
    private Bundle.BundleType bundleType;
    private Bundle.HTTPVerb httpVerb;

    public BundleResource() {
        super(new Bundle());
    }

    @Override
    protected void init() {
        this.patientId = 0;
        this.maxPatientId = this.getRandomInteger(1, 10);
        this.source = "clinic";
        this.amountMedication = 0;
        this.maxMedication = this.getRandomInteger(1, 10);
        this.amountObservation = 0;
        this.maxObservation = this.getRandomInteger(1, 10);
        this.amountMedicationRequest = 0;
        this.amountMedicationAdministration = 0;
    }

    @Override
    public BundleResource randomize() {
        this.amountMedication = this.getRandomInteger(0, this.maxMedication);
        this.patientId = this.getRandomInteger(0, this.maxPatientId);
        this.amountObservation = this.getRandomInteger(0, this.maxObservation);
        this.amountMedicationRequest = this.getRandomInteger(0, this.amountMedication + 1);
        this.amountMedicationAdministration = this.getRandomInteger(0, this.amountMedication + 1);
        this.bundleType = Bundle.BundleType.values()[this.getRandomInteger(0, Bundle.BundleType.values().length)];
        this.httpVerb = Bundle.HTTPVerb.values()[this.getRandomInteger(0, Bundle.HTTPVerb.values().length)];
        return this;
    }

    @Override
    public Bundle build() {
        Bundle b = new Bundle();
        b.setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
        brc.setMethod(Bundle.HTTPVerb.PUT);
        // 1.) Patient
        PatientResource pr = new PatientResource();
        Patient patient = pr.setPatientID(this.patientId).setSource(this.source).build();
        String patName = this.getResourceNameURL(patient);
        b.addEntry().setResource(patient).setRequest(brc.copy().setUrl(patName)).setFullUrl(patName);
        // 2.) Medication
        MedicationResource mr = new MedicationResource();
        List<Medication> medicationList = new ArrayList<>();
        for (int i = 0; i < this.amountMedication + 1; i++) {
            Medication medication = mr.randomize().build();
            medicationList.add(medication);
            String medName = this.getResourceNameURL(medication);
            b.addEntry().setResource(medication).setRequest(brc.copy().setUrl(medName)).setFullUrl(medName);
        }
        // 3.) Observation
        ObservationResource or = new ObservationResource();
        for (int i = 0; i < this.amountObservation + 1; i++) {
            Observation observation = or.randomize().setSubject(patient).build();
            String obsName = this.getResourceNameURL(observation);
            b.addEntry().setResource(observation).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }
        // 4.) MedicationRequest
        MedicationRequestResource mrr = new MedicationRequestResource();
        for (int i = 0; i < this.amountMedicationRequest + 1; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationRequest medicationRequest = mrr.randomize().setSubject(patient).setMedication(randomMedication).build();
            String obsName = this.getResourceNameURL(medicationRequest);
            b.addEntry().setResource(medicationRequest).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }
        // 5.) MedicationAdministration
        MedicationAdministrationResource mar = new MedicationAdministrationResource();
        for (int i = 0; i < this.amountMedicationAdministration + 1; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationAdministration medicationAdministration = mar.randomize().setSubject(patient).setMedication(randomMedication).build();
            String obsName = this.getResourceNameURL(medicationAdministration);
            b.addEntry().setResource(medicationAdministration).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }
        // 6.) Consent
        ConsentResource cr = new ConsentResource();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 5);
        Date start = calendar.getTime();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 10);
        Date end = calendar.getTime();

        Consent validConsent = cr.setConsentState(Consent.ConsentState.ACTIVE)
                .setDateIntervalls(start, end)
                .setConcept(ConsentCodeSystem.MDAT_ZUSAMMENFUEHREN_DRITTE)
                .setPatient(patient)
                .setId(1)
                .build();

        Consent inactiveConsent = cr.setConsentState(Consent.ConsentState.INACTIVE)
                .setDateIntervalls(start, end)
                .setConcept(ConsentCodeSystem.PROMDAT_PATIENTENBEFRAGUNG_ERHEBEN)
                .setPatient(patient)
                .setId(2)
                .build();

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 20);
        start = calendar.getTime();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 5);
        end = calendar.getTime();

        Consent invalidConsent = cr.setConsentState(Consent.ConsentState.ACTIVE)
                .setDateIntervalls(start, end)
                .setConcept(ConsentCodeSystem.MDAT_ZUSAMMENFUEHREN_DRITTE)
                .setPatient(patient)
                .setId(3)
                .build();

        Consent randomConsent = cr.randomize().setPatient(patient).setId(4).build();

        List<Consent> consentList = List.of(validConsent, inactiveConsent, invalidConsent, randomConsent);
        for (Consent consent : consentList) {
            String obsName = this.getResourceNameURL(consent);
            b.addEntry().setResource(consent).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }

        this.setResource(b);
        return this.getResource();
    }

    public BundleResource setMaxPatientId(int maxPatientId) {
        this.maxPatientId = maxPatientId;
        return this;
    }

    public BundleResource setPatientId(int patientId) {
        this.patientId = patientId;
        return this;
    }

    public BundleResource setSource(String source) {
        this.source = source;
        return this;
    }

    public BundleResource setMaxMedication(int maxMedication) {
        this.maxMedication = maxMedication;
        this.amountMedication = maxMedication;
        return this;
    }

    public BundleResource setAmountMedication(int amountMedication) {
        this.amountMedication = amountMedication;
        return this;
    }

    public BundleResource maxObservation(int maxObservation) {
        this.maxObservation = maxObservation;
        this.amountObservation = maxObservation;
        return this;
    }

    public BundleResource setAmountObservation(int amountObservation) {
        this.amountMedication = amountObservation;
        return this;
    }

    public BundleResource setAmountMedicationRequest(int amountMedicationRequest) {
        if (amountMedicationRequest <= this.amountMedication) this.amountMedicationRequest = amountMedicationRequest;
        return this;
    }

    public BundleResource setAmountMedicationAdministration(int amountMedicationAdministration) {
        if (amountMedicationAdministration <= this.amountMedication) this.amountMedicationAdministration = amountMedicationAdministration;
        return this;
    }

    public BundleResource setBundleType(Bundle.BundleType bundleType) {
        this.bundleType = bundleType;
        return this;
    }

    public BundleResource setHttpVerb(Bundle.HTTPVerb httpVerb) {
        this.httpVerb = httpVerb;
        return this;
    }

    private String getResourceNameURL(Resource resource) {
        return resource.getResourceType().name() + "/" + resource.getId();
    }
}
