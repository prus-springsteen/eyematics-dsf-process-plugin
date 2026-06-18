package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentCodeSystem;
import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.eyematics.process.tools.generator.shared.ConsentVersion;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BundleResource extends AbstractFHIRResourceBuilder<Bundle, BundleResource> {

    private Patient patient;
    private String source;
    private Date start;
    private Date end;
    private int amountMedication;
    private int amountValidObservation;
    private int amountValidDiagnosticReport;
    private int amountValidMedicationRequest;
    private int amountValidMedicationAdministration;
    private int amountInvalidObservation;
    private int amountInvalidDiagnosticReport;
    private int amountInvalidMedicationRequest;
    private int amountInvalidMedicationAdministration;
    private int maxObservation;
    private int maxDiagnosticReport;
    private int maxMedication;
    private List<Medication> medicationList;
    private int maxMedicationRequest;
    private int maxMedicationAdministration;
    private Bundle.BundleType bundleType;
    private Bundle.HTTPVerb httpVerb;
    private int amountValidConsent;
    private int amountInvalidConsent;
    private int maxValidConsent;
    private int maxInvalidConsent;

    public BundleResource() {
        super();
        this.patient = null;
        this.source = "medic";
        this.start = new Date();
        this.end = new Date();
        this.amountMedication = 0;
        this.amountValidObservation = 0;
        this.amountValidDiagnosticReport = 0;
        this.amountValidMedicationRequest = 0;
        this.amountValidMedicationAdministration = 0;
        this.amountInvalidObservation = 0;
        this.amountInvalidDiagnosticReport = 0;
        this.amountInvalidMedicationRequest = 0;
        this.amountInvalidMedicationAdministration = 0;
        this.maxObservation = this.getRandomInteger(1, 10);
        this.maxDiagnosticReport = this.getRandomInteger(1, 10);
        this.maxMedication = this.getRandomInteger(1, 10);
        this.medicationList = new ArrayList<>();
        this.maxMedicationRequest = this.getRandomInteger(1, 10);
        this.maxMedicationAdministration = this.getRandomInteger(1, 10);
        this.bundleType = Bundle.BundleType.TRANSACTION;
        this.httpVerb = Bundle.HTTPVerb.PUT;
        this.amountValidConsent = 0;
        this.amountInvalidConsent = 0;
        this.maxValidConsent = this.getRandomInteger(1, 10);
        this.maxInvalidConsent = this.getRandomInteger(1, 10);
    }

    @Override
    public BundleResource randomize() {
        this.source = "medic";
        this.randomizePeriod();
        this.amountMedication = this.getRandomInteger(0, this.maxMedication + 1);
        this.medicationList.clear();
        this.amountValidObservation = this.getRandomInteger(0, this.maxObservation + 1);
        this.amountValidDiagnosticReport = this.getRandomInteger(0, this.maxDiagnosticReport + 1);
        this.amountValidMedicationRequest = this.getRandomInteger(0, this.maxMedicationRequest + 1);
        this.amountValidMedicationAdministration = this.getRandomInteger(0, this.maxMedicationAdministration + 1);
        this.amountInvalidObservation = this.getRandomInteger(0, this.maxObservation + 1);
        this.amountInvalidDiagnosticReport = this.getRandomInteger(0, this.maxDiagnosticReport + 1);
        this.amountInvalidMedicationRequest = this.getRandomInteger(0, this.maxMedicationRequest + 1);
        this.amountInvalidMedicationAdministration = this.getRandomInteger(0, this.maxMedicationAdministration + 1);
        this.bundleType = Bundle.BundleType.values()[this.getRandomInteger(0, Bundle.BundleType.values().length)];
        this.httpVerb = Bundle.HTTPVerb.values()[this.getRandomInteger(0, Bundle.HTTPVerb.values().length)];
        this.amountValidConsent = this.getRandomInteger(0, this.maxValidConsent + 1);
        this.amountInvalidConsent = this.getRandomInteger(0, this.maxInvalidConsent + 1);
        return this;
    }

    @Override
    public Bundle build() {
        Bundle b = new Bundle();
        b.setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
        brc.setMethod(Bundle.HTTPVerb.PUT);

        // 1.) Patient
        Patient p = this.patient;
        if (p == null) {
            PatientResource pr = new PatientResource();
            p = pr.randomize().setSource(this.source).build();
        }
        if (p.getId() == null) p.setId(this.getRandomId());

        String patName = this.getResourceNameURL(p);
        b.addEntry().setResource(p).setRequest(brc.copy().setUrl(patName)).setFullUrl(patName);

        // 2.) Medication
        List<Medication> medicationList = new ArrayList<>();
        if (this.medicationList.isEmpty()) {
            MedicationResource mr = new MedicationResource();
            for (int i = 0; i < this.amountMedication + 1; i++) {
                Medication m = mr.randomize().build();
                medicationList.add(m);
                String medName = this.getResourceNameURL(m);
                b.addEntry().setResource(m).setRequest(brc.copy().setUrl(medName)).setFullUrl(medName);
            }
        } else {
            medicationList.addAll(this.medicationList);
        }

        // 3.1) Observation (Valid Consent Period)
        ArrayList<Observation> validObservationList = new ArrayList<>();
        ObservationResource or = new ObservationResource();
        for (int i = 0; i < this.amountValidObservation; i++) {
            Date observationDate = this.getRandomDate(this.start, this.end);
            Observation o = or.randomize()
                    .setEffectiveDate(observationDate)
                    .setSubject(p)
                    .build();
            String obsName = this.getResourceNameURL(o);
            b.addEntry().setResource(o).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
            validObservationList.add(o);
        }

        // 3.2) Observation (Invalid Consent Period)
        ArrayList<Observation> invalidObservationList = new ArrayList<>();
        for (int i = 0; i < this.amountInvalidObservation; i++) {
            Date observationDate = this.getInvalidRandomizedDateFromPeriod();
            Observation o = or.randomize()
                    .setEffectiveDate(observationDate)
                    .setSubject(p)
                    .build();
            String obsName = this.getResourceNameURL(o);
            b.addEntry().setResource(o).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
            invalidObservationList.add(o);
        }

        // 4.1) DiagnosticReport (Valid Consent Period)
        DiagnosticReportResource drr = new DiagnosticReportResource();
        for (int j = 0; j < this.amountValidDiagnosticReport; j++) {
            Observation randomObservation = validObservationList.get(this.getRandomInteger(0, validObservationList.size()));
            Date observationDate = randomObservation.getEffectiveDateTimeType().getValue();
            DiagnosticReport r = drr.randomize()
                    .clearResultsReferences()
                    .addResultReference(randomObservation)
                    .setEffectiveDate(this.getRandomDate(observationDate, this.getRandomDate(observationDate, this.end)))
                    .setPatientReference(p.getId())
                    .build();
            String drName = this.getResourceNameURL(r);
            b.addEntry().setResource(r).setRequest(brc.copy().setUrl(drName)).setFullUrl(drName);
        }
        // 4.2) DiagnosticReport (Invalid Consent Period)
        for (int j = 0; j < this.amountInvalidDiagnosticReport; j++) {
            Observation randomObservation = invalidObservationList.get(this.getRandomInteger(0, invalidObservationList.size()));
            Date observationDate = randomObservation.getEffectiveDateTimeType().getValue();
            DiagnosticReport r = drr.randomize()
                    .clearResultsReferences()
                    .addResultReference(randomObservation)
                    .setEffectiveDate(observationDate)
                    .setPatientReference(p.getId())
                    .build();
            String drName = this.getResourceNameURL(r);
            b.addEntry().setResource(r).setRequest(brc.copy().setUrl(drName)).setFullUrl(drName);
        }

        // 5.1) MedicationRequest (Valid Consent Period)
        MedicationRequestResource mrr = new MedicationRequestResource();
        for (int i = 0; i < this.amountValidMedicationRequest; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationRequest mr = mrr.randomize()
                    .setAuthoredOn(this.getRandomDate(this.start, this.end))
                    .setSubject(p)
                    .setMedication(randomMedication)
                    .build();
            String obsName = this.getResourceNameURL(mr);
            b.addEntry().setResource(mr).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }
        // 5.2) MedicationRequest (Invalid Consent Period)
        for (int i = 0; i < this.amountInvalidMedicationRequest; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationRequest mr = mrr.randomize()
                    .setAuthoredOn(this.getInvalidRandomizedDateFromPeriod())
                    .setSubject(p)
                    .setMedication(randomMedication)
                    .build();
            String obsName = this.getResourceNameURL(mr);
            b.addEntry().setResource(mr).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }

        // 6.1) MedicationAdministration (Valid Consent Period)
        MedicationAdministrationResource mar = new MedicationAdministrationResource();
        for (int i = 0; i < this.amountValidMedicationAdministration; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationAdministration ma = mar.randomize()
                    .setEffectiveDate(this.getRandomDate(this.start, this.end))
                    .setSubject(p)
                    .setMedication(randomMedication)
                    .build();
            String obsName = this.getResourceNameURL(ma);
            b.addEntry().setResource(ma).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }
        // 6.2 MedicationAdministration (Invalid Consent Period)
        for (int i = 0; i < this.amountInvalidMedicationAdministration; i++) {
            Medication randomMedication = medicationList.get(this.getRandomInteger(0, medicationList.size()));
            MedicationAdministration ma = mar.randomize()
                    .setEffectiveDate(this.getInvalidRandomizedDateFromPeriod())
                    .setSubject(p)
                    .setMedication(randomMedication)
                    .build();
            String obsName = this.getResourceNameURL(ma);
            b.addEntry().setResource(ma).setRequest(brc.copy().setUrl(obsName)).setFullUrl(obsName);
        }

        // 7.) Consent
        ConsentResource cr = new ConsentResource();
        for (int i = 0; i < this.amountValidConsent; i++) {
            cr.setEmpty();
            Consent c = cr.setEmpty()
                    .setState(Consent.ConsentState.ACTIVE)
                    .setId(this.getRandomId())
                    .setScope(ConsentScope.RESEARCH)
                    .addCategory(ConsentConstant.MII_DATA_PRIVACY_CODEABLE_CONCEPT)
                    .addCategory(ConsentConstant.MII_CONSENT_CATEGORY_CODEABLE_CONCEPT)
                    .setPatient(p)
                    .addIdentifier(null, null)
                    .setDateIntervalls(this.start, this.end)
                    .setPolicy(ConsentVersion.VERSION_1_7_2.getPolicyComponent())
                    .hasParentProvision()
                    .addConcept(ConsentCodeSystem.MDAT_WISSENSCHAFTLICH_NUTZEN,
                            Consent.ConsentProvisionType.PERMIT,
                            this.start,
                            this.end)
                    .addConcept(ConsentCodeSystem.MDAT_RETROSPEKTIV_WISSENSCHAFTLICH_NUTZEN,
                            Consent.ConsentProvisionType.PERMIT,
                            this.start,
                            this.end)
                    .addConcept(ConsentCodeSystem.PROMDAT_WISSENSCHAFTLICH_NUTZEN,
                            Consent.ConsentProvisionType.PERMIT,
                            this.start,
                            this.end)
                    .build();
            String consentName = this.getResourceNameURL(c);
            b.addEntry().setResource(c).setRequest(brc.copy().setUrl(consentName)).setFullUrl(consentName);
        }

        for (int i = 0; i < this.amountInvalidConsent; i++) {
            cr.setEmpty()
                    .setState(Consent.ConsentState.ACTIVE)
                    .randomizeId()
                    .setScope(ConsentScope.values()[this.getRandomInteger(0, ConsentScope.values().length - 1)])
                    .setPolicy(ConsentVersion.VERSION_1_7_2.getPolicyComponent())
                    .setPatient(p);
            if (this.getRandomBoolean()) {
                cr.addCategory(ConsentConstant.MII_DATA_PRIVACY_CODEABLE_CONCEPT)
                        .addCategory(ConsentConstant.MII_CONSENT_CATEGORY_CODEABLE_CONCEPT);
            } else {
                cr.addCategory("valid", "valid", "valid");
            }
            if (this.getRandomBoolean()) {
                cr.addIdentifier("not-valid", null);
            } else {
                cr.addIdentifier(null, null);
            }
            if (this.getRandomBoolean()) {
                cr.setDateIntervalls(this.start, this.end);
                cr.hasNoParentProvision();
            } else {
                cr.setDateIntervalls(this.end, this.start);
                cr.hasParentProvision();
            }
            cr.addConcept(ConsentCodeSystem.MDAT_WISSENSCHAFTLICH_NUTZEN,
                    Consent.ConsentProvisionType.PERMIT,
                    this.end,
                    this.start);

            Consent c = cr.build();
            String consentName = this.getResourceNameURL(c);
            b.addEntry().setResource(c).setRequest(brc.copy().setUrl(consentName)).setFullUrl(consentName);
        }
        return b;
    }

    public BundleResource setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public BundleResource removePatient() {
        this.patient = null;
        return this;
    }

    public BundleResource setSource(String source) {
        this.source = source;
        return this;
    }

    public BundleResource setAmountMedication(int amountMedication) {
        if (amountMedication >= 0) this.amountMedication = amountMedication;
        return this;
    }

    public BundleResource setMedication(List<Medication> medicationList) {
        this.medicationList = medicationList;
        return this;
    }

    public BundleResource setMedication(Medication medication) {
        this.medicationList.clear();
        this.medicationList.add(medication);
        return this;
    }

    public BundleResource addMedication(List<Medication> medicationList) {
        this.medicationList.addAll(medicationList);
        return this;
    }

    public BundleResource addMedication(Medication medication) {
        this.medicationList.add(medication);
        return this;
    }

    public BundleResource removeMedication(Medication medication) {
        this.medicationList.remove(medication);
        return this;
    }

    public BundleResource removeMedication(List<Medication> medicationList) {
        this.medicationList.removeAll(medicationList);
        return this;
    }

    public BundleResource clearMedication() {
        this.medicationList.clear();
        return this;
    }

    public BundleResource setAmountValidDiagnosticReport(int amountValidDiagnosticReport) {
        if (amountValidDiagnosticReport >= 0) this.amountValidDiagnosticReport = amountValidDiagnosticReport;
        return this;
    }

    public BundleResource setAmountInvalidDiagnosticReport(int amountInvalidDiagnosticReport) {
        if (amountInvalidDiagnosticReport >= 0) this.amountInvalidDiagnosticReport = amountInvalidDiagnosticReport;
        return this;
    }

    public BundleResource setAmountValidObservation(int amountValidObservation) {
        if (amountValidObservation >= 0) this.amountValidObservation = amountValidObservation;
        return this;
    }

    public BundleResource setAmountInvalidObservation(int amountInvalidObservation) {
        if (amountInvalidObservation >= 0) this.amountInvalidObservation = amountInvalidObservation;
        return this;
    }

    public BundleResource setAmountValidMedicationRequest(int amountValidMedicationRequest) {
        if (amountValidMedicationRequest >= 0) this.amountValidMedicationRequest = amountValidMedicationRequest;
        return this;
    }

    public BundleResource setAmountInvalidMedicationRequest(int amountInvalidMedicationRequest) {
        if (amountInvalidMedicationRequest >= 0) this.amountInvalidMedicationRequest = amountInvalidMedicationRequest;
        return this;
    }

    public BundleResource setAmountValidMedicationAdministration(int amountValidMedicationAdministration) {
        if (amountValidMedicationAdministration >= 0) this.amountValidMedicationAdministration = amountValidMedicationAdministration;
        return this;
    }

    public BundleResource setAmountInvalidMedicationAdministration(int amountInvalidMedicationAdministration) {
        if (amountInvalidMedicationAdministration >= 0) this.amountInvalidMedicationAdministration = amountInvalidMedicationAdministration;
        return this;
    }

    public BundleResource setAmountValidConsent(int amountValidConsent) {
        if (amountValidConsent >= 0) this.amountValidConsent = amountValidConsent;
        return this;
    }

    public BundleResource setAmountInvalidConsent(int amountInvalidConsent) {
        if (amountInvalidConsent >= 0) this.amountInvalidConsent = amountInvalidConsent;
        return this;
    }

    public BundleResource setMaxRandomObservation(int maxObservation) {
        if (maxObservation >= 0) this.maxObservation = maxObservation;
        return this;
    }

    public BundleResource setMaxRandomDiagnosticReport(int maxDiagnosticReport) {
        if (maxDiagnosticReport >= 0) this.maxDiagnosticReport = maxDiagnosticReport;
        return this;
    }

    public BundleResource setMaxRandomMedication(int maxMedication) {
        if (maxMedication >= 0) this.maxMedication = maxMedication;
        return this;
    }

    public BundleResource setMaxRandomMedicationRequest(int maxMedicationRequest) {
        if (maxMedicationRequest >= 0) this.maxMedicationRequest = maxMedicationRequest;
        return this;
    }

    public BundleResource setMaxRandomMedicationAdministration(int maxMedicationAdministration) {
        if (maxMedicationAdministration >= 0) this.maxMedicationAdministration = maxMedicationAdministration;
        return this;
    }

    private BundleResource setMaxRandomValidConsent(int maxValidConsent) {
        if (maxValidConsent >= 0) this.maxValidConsent = maxValidConsent;
        return this;
    }

    private BundleResource setMaxRandomInvalidConsent(int maxInvalidConsent) {
        if (maxInvalidConsent >= 0) this.maxInvalidConsent = maxInvalidConsent;
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

    public BundleResource setPeriod(Date start, Date end) {
        if (start != null && end != null) {
            if (start.before(end)) {
                this.start = start;
                this.end = end;
            } else {
                this.start = end;
                this.end = start;
            }
        }
        return this;
    }

    public BundleResource randomizePeriod() {
        return this.setPeriod(this.getRandomDate(), this.getRandomDate());
    }

    private Date getInvalidRandomizedDateFromPeriod() {
        Date invalidStart = new Date(LocalDateTime.ofInstant(this.start.toInstant(),
                        ZoneId.systemDefault())
                .minusYears(10)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        Date invalidEnd = new Date(LocalDateTime.ofInstant(this.start.toInstant(),
                        ZoneId.systemDefault())
                .minusYears(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());

        if (this.getRandomBoolean()) {
            invalidStart = new Date(LocalDateTime.ofInstant(this.end.toInstant(),
                            ZoneId.systemDefault())
                    .plusYears(1)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
            invalidEnd = new Date(LocalDateTime.ofInstant(this.end.toInstant(),
                            ZoneId.systemDefault())
                    .plusYears(10)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }
        return this.getRandomDate(invalidStart, invalidEnd);
    }
}
