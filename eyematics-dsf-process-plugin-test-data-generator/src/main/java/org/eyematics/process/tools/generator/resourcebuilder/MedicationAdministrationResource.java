package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.Date;


public class MedicationAdministrationResource extends AbstractFHIRResourceBuilder<MedicationAdministration, MedicationAdministrationResource> {

    private Date effectiveDate;
    private int versionId;
    private String subject;
    private String medication;
    private MedicationAdministration.MedicationAdministrationStatus medicationAdministrationStatus;

    public MedicationAdministrationResource() {
        super();
        this.effectiveDate = new Date();
        this.versionId = 1;
        this.subject = "";
        this.medication = "";
        this.medicationAdministrationStatus = MedicationAdministration.MedicationAdministrationStatus.COMPLETED;
    }

    @Override
    public MedicationAdministrationResource randomize() {
        this.randomizeId();
        this.randomizeEffectiveDate();
        this.randomizeSubject();
        this.randomizeMedication();
        this.versionId = this.getRandomInteger(1, 999);
        this.medicationAdministrationStatus = this.getRandomMedicationAdministrationStatus();
        return this;
    }

    @Override
    public MedicationAdministration build() {
        MedicationAdministration ma = new MedicationAdministration();
        ma.setId(this.id);
        ma.getMeta().setVersionId(Integer.toString(this.versionId));
        ma.getMeta().getProfile().add(new CanonicalType("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/mii-eyematics-ivom-medicationadministration"));
        ma.getMeta().getProfile().add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        ma.getMeta().setLastUpdated(this.effectiveDate);
        ma.setEffective(new DateTimeType(this.effectiveDate));
        ma.getIdentifier().add(this.getRandomIdentifier());
        ma.setStatus(this.medicationAdministrationStatus);
        ma.setSubject(new Reference(this.subject));
        ma.setMedication(new Reference(this.medication));
        return ma;
    }

    public MedicationAdministrationResource setSubject(String subject) {
        this.subject = "Patient/" + subject;
        return this;
    }

    public MedicationAdministrationResource randomizeSubject() {
        return this.setSubject(this.getRandomId());
    }

    public MedicationAdministrationResource setSubject(Patient patient) {
        return this.setSubject(patient.getId());
    }

    public MedicationAdministrationResource setMedication(String medication) {
        this.medication = "Medication/" + medication;
        return this;
    }

    public MedicationAdministrationResource randomizeMedication() {
        return this.setMedication(this.getRandomId());
    }

    public MedicationAdministrationResource setMedication(Medication medication) {
        return this.setMedication(medication.getId());
    }

    private MedicationAdministration.MedicationAdministrationStatus getRandomMedicationAdministrationStatus() {
        return MedicationAdministration.MedicationAdministrationStatus.values()[this.getRandomInteger(0,
                MedicationAdministration.MedicationAdministrationStatus.values().length - 1)];
    }

    public MedicationAdministrationResource setEffectiveDate(Date date) {
        this.effectiveDate = date;
        return this;
    }

    public MedicationAdministrationResource randomizeEffectiveDate() {
        return this.setEffectiveDate(this.getRandomDate());
    }
}
