package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.UUID;


public class MedicationRequestResource extends AbstractFHIRResourceBuilder<MedicationRequest, MedicationRequestResource>{

    private long dateMillis;
    private int versionId;
    private MedicationRequest.MedicationRequestStatus medicationRequestStatus;
    private MedicationRequest.MedicationRequestIntent medicationRequestIntent;

    public MedicationRequestResource() {
        super(new MedicationRequest());
    }

    @Override
    public MedicationRequestResource randomize() {
        this.dateMillis = this.getRandomDateTimeLong();
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
        this.getResource().setId(UUID.randomUUID().toString());
        this.getResource().getExtension().clear();
        this.getResource().addExtension(this.getRandomExtension());
        this.getResource().getMeta().setLastUpdated(new Date(this.dateMillis));
        this.getResource().getMeta().setVersionId(Integer.toString(this.versionId));
        this.getResource().getMeta().getProfile().clear();
        this.getResource().getMeta().getProfile().add(new CanonicalType("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/mii-eyematics-ivi-medicationrequest"));
        this.getResource().setStatus(this.medicationRequestStatus);
        this.getResource().setIntent(this.medicationRequestIntent);
        this.getResource().getDosageInstruction().add(new Dosage());
        return this.getResource().copy();
    }

    public MedicationRequestResource setSubject(Patient patient) {
        String refStr = "Patient/" + patient.getId();
        this.getResource().setSubject(new Reference(refStr));
        return this;
    }

    public MedicationRequestResource setMedication(Medication medication) {
        String refStr = "Medication/" + medication.getId();
        this.getResource().setMedication(new Reference(refStr));
        return this;
    }

    private MedicationRequest.MedicationRequestStatus getRandomMedicationRequestStatus() {
        return MedicationRequest.MedicationRequestStatus.values()[this.getRandomInteger(0, MedicationRequest.MedicationRequestStatus.values().length)];
    }

    private MedicationRequest.MedicationRequestIntent getRandomMedicationRequestIntent() {
        return MedicationRequest.MedicationRequestIntent.values()[this.getRandomInteger(0, MedicationRequest.MedicationRequestIntent.values().length)];
    }
}
