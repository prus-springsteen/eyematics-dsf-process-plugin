package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.Date;


public class MedicationRequestResource extends AbstractFHIRResourceBuilder<MedicationRequest, MedicationRequestResource>{

    private Date authoredOn;
    private int versionId;
    private String subject;
    private String medication;
    private MedicationRequest.MedicationRequestStatus medicationRequestStatus;
    private MedicationRequest.MedicationRequestIntent medicationRequestIntent;

    public MedicationRequestResource() {
        super();
        this.authoredOn = new Date();
        this.versionId = 1;
        this.subject = "";
        this.medication = "";
        this.medicationRequestStatus = MedicationRequest.MedicationRequestStatus.ACTIVE;
        this.medicationRequestIntent = MedicationRequest.MedicationRequestIntent.ORDER;
    }

    @Override
    public MedicationRequestResource randomize() {
        this.randomizeId();
        this.randomizeAuthoredOn();
        this.versionId = this.getRandomInteger(1, 999);
        this.medicationRequestStatus = this.getRandomMedicationRequestStatus();
        this.medicationRequestIntent = this.getRandomMedicationRequestIntent();
        return this;
    }

    private Extension getRandomExtension() {
        int randInt = this.getRandomInteger(0, 2);
        Extension e = new Extension("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/extension-ivi-treatment-regimen");
        if (randInt == 0) {
            e.setValue(new CodeableConcept(new Coding("https://eyematics.org/fhir/eyematics-kds/CodeSystem/ivi-treatment-regimen", "TE", "Treat-and-Extend")));
        } else if (randInt == 1) {
            e.setValue(new CodeableConcept(new Coding("https://eyematics.org/fhir/eyematics-kds/CodeSystem/ivi-treatment-regimen", "PRN", "Pro Re Nata")));
        } else {
            e.setValue(new CodeableConcept(new Coding("https://eyematics.org/fhir/eyematics-kds/CodeSystem/ivi-treatment-regimen", "Fixed", "Fixed Interval")));
        }
        return e;
    }

    @Override
    public MedicationRequest build() {
        MedicationRequest mr = new MedicationRequest();
        mr.setId(this.id);
        mr.getExtension().clear();
        mr.addExtension(this.getRandomExtension());
        mr.getMeta().setLastUpdated(this.authoredOn);
        mr.getMeta().setVersionId(Integer.toString(this.versionId));
        mr.getMeta().getProfile().clear();
        mr.getMeta().getProfile().add(new CanonicalType("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/mii-eyematics-ivi-medicationrequest"));
        mr.getMeta().getProfile().add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        mr.getMeta().setLastUpdated(this.authoredOn);
        mr.setAuthoredOn(this.authoredOn);
        mr.getIdentifier().add(this.getRandomIdentifier());
        mr.setStatus(this.medicationRequestStatus);
        mr.setIntent(this.medicationRequestIntent);
        mr.getDosageInstruction().add(new Dosage());
        mr.setSubject(new Reference(this.subject));
        mr.setMedication(new Reference(this.medication));
        return mr;
    }

    public MedicationRequestResource setSubject(String subject) {
        this.subject = "Patient/" + subject;
        return this;
    }

    public MedicationRequestResource randomizeSubject() {
        return this.setSubject(this.getRandomId());
    }

    public MedicationRequestResource setSubject(Patient patient) {
        return this.setSubject(patient.getId());
    }

    public MedicationRequestResource setMedication(String medication) {
        this.medication = "Medication/" + medication;
        return this;
    }

    public MedicationRequestResource randomizeMedication() {
        return this.setMedication(this.getRandomId());
    }

    public MedicationRequestResource setMedication(Medication medication) {
        return this.setMedication(medication.getId());
    }

    private MedicationRequest.MedicationRequestStatus getRandomMedicationRequestStatus() {
        return MedicationRequest.MedicationRequestStatus.values()[this.getRandomInteger(0, MedicationRequest.MedicationRequestStatus.values().length - 1)];
    }

    private MedicationRequest.MedicationRequestIntent getRandomMedicationRequestIntent() {
        return MedicationRequest.MedicationRequestIntent.values()[this.getRandomInteger(0, MedicationRequest.MedicationRequestIntent.values().length - 1)];
    }

    public MedicationRequestResource setAuthoredOn(Date date) {
        this.authoredOn = date;
        return this;
    }

    public MedicationRequestResource randomizeAuthoredOn() {
        return this.setAuthoredOn(this.getRandomDate());
    }
}
