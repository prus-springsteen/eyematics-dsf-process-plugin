package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.UUID;

public class MedicationAdministrationResource extends AbstractFHIRResourceBuilder<MedicationAdministration, MedicationAdministrationResource> {

    private long dateMillis;
    private int versionId;
    private MedicationAdministration.MedicationAdministrationStatus medicationAdministrationStatus;

    public MedicationAdministrationResource() {
        super(new MedicationAdministration());
        this.getResource().setMeta(new Meta().addProfile("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/mii-eyematics-ivom-medicationadministration"));
    }

    @Override
    protected void init() {
        this.dateMillis = new Date().getTime();
        this.versionId = 1;
        this.medicationAdministrationStatus = MedicationAdministration.MedicationAdministrationStatus.COMPLETED;
    }

    @Override
    public MedicationAdministrationResource randomize() {
        this.dateMillis = this.getRandomDateTimeLong();
        this.versionId = this.getRandomInteger(1, 999);
        this.medicationAdministrationStatus = this.getRandomMedicationAdministrationStatus();
        return this;
    }

    @Override
    public MedicationAdministration build() {
        this.getResource().setId(UUID.randomUUID().toString());
        this.getResource().getMeta().setLastUpdated(new Date(this.dateMillis));
        this.getResource().getMeta().setVersionId(Integer.toString(this.versionId));
        this.getResource().setStatus(this.medicationAdministrationStatus);
        return this.getResource().copy();
    }

    public MedicationAdministrationResource setSubject(Patient patient) {
        String refStr = "Patient/" + patient.getId();
        this.getResource().setSubject(new Reference(refStr));
        return this;
    }

    public MedicationAdministrationResource setMedication(Medication medication) {
        String refStr = "Medication/" + medication.getId();
        this.getResource().setMedication(new Reference(refStr));
        return this;
    }

    private MedicationAdministration.MedicationAdministrationStatus getRandomMedicationAdministrationStatus() {
        return MedicationAdministration.MedicationAdministrationStatus.values()[this.getRandomInteger(0,
                MedicationAdministration.MedicationAdministrationStatus.values().length - 1)];
    }
}
