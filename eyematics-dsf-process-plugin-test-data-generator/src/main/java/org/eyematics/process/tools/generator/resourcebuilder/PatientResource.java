package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PatientResource extends AbstractFHIRResourceBuilder<Patient, PatientResource> {

    private String randomPID;
    private int id;
    private int maxPatients;
    private long dateMillis;
    private int versionId;
    private String source;

    public PatientResource() {
        super(new Patient());
    }

    @Override
    protected void init() {
        this.id = 1;
        this.maxPatients = 1;
        this.setSource("clinic");
    }

    private String generatePatientId() {
        return "pseudo-" + Integer.toString(this.id);
    }

    @Override
    public PatientResource randomize() {
        this.randomPID = UUID.randomUUID().toString();
        this.randomizeID();
        this.dateMillis = this.getRandomDateTimeLong();
        this.versionId = this.getRandomInteger(1, 999);
        return this;
    }

    public PatientResource setPatientID(int id) {
        if (id > 0) {
            if (id > this.maxPatients) this.maxPatients = id;
            this.id = id;
        }
        return this;
    }

    public PatientResource setMaximumPatients(int maxPatients) {
        if (this.maxPatients > 0) this.maxPatients = maxPatients;
        return this;
    }

    public PatientResource randomizeID() {
        if (this.maxPatients > this.id) this.id = this.getRandomInteger(1, this.maxPatients);
        return this;
    }

    public PatientResource setSource(String source) {
        this.source = "mailto:medic@" + source + ".org";
        return this;
    }

    @Override
    public Patient build() {
        Extension e = new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("masked"));
        this.getResource().setId(this.generatePatientId());
        this.getResource().getMeta().setLastUpdated(new Date(this.dateMillis));
        this.getResource().getMeta().setVersionId(Integer.toString(this.versionId));
        this.getResource().getMeta().setSource(this.source);
        this.getResource().getMeta().getProfile().add(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient|2024.0.0"));
        Identifier identifier = new Identifier();
        identifier.setUse(Identifier.IdentifierUse.USUAL);
        identifier.setType(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR")));
        identifier.setSystem("http://clinic.org/KIS/PID");
        identifier.setValue(this.randomPID);
        identifier.setAssigner(new Reference().setIdentifier(new Identifier().setSystem("http://fhir.de/sid/arge-ik/iknr").setValue("007")).setDisplay("Clinic"));
        this.getResource().getIdentifier().add(identifier);
        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.getExtension().add(e);
        name.getFamilyElement().addExtension(e);
        StringType given = new StringType("");
        given.addExtension(e);
        name.setGiven(List.of(given));
        this.getResource().addName(name);
        this.getResource().setGender(this.getRandomGender());
        this.getResource().getBirthDateElement().addExtension(e);
        Address address = new Address();
        address.setType(Address.AddressType.PHYSICAL);
        StringType line = new StringType("");
        line.addExtension(e);
        address.getLine().add(line);
        address.addLineElement().addExtension(e);
        address.getCityElement().addExtension(e);
        address.getPostalCodeElement().addExtension(e);
        this.getResource().addAddress(address);
        return this.getResource();
    }

    private Enumerations.AdministrativeGender getRandomGender() {
        return Enumerations.AdministrativeGender.values()[this.getRandomInteger(0, Enumerations.AdministrativeGender.values().length - 1)];
    }
}
