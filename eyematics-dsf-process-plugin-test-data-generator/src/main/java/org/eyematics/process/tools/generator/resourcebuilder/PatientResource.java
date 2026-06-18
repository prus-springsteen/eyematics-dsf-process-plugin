package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.Date;


public class PatientResource extends AbstractFHIRResourceBuilder<Patient, PatientResource> {

    private String randomPID;
    private String bloomFilter;
    private long dateMillis;
    private int versionId;
    private String source;

    public PatientResource() {
        super();
        this.randomPID = "";
        this.bloomFilter = "";
        this.dateMillis = 0;
        this.versionId = 1;
        this.setSource("clinic");
    }

    @Override
    public PatientResource randomize() {
        this.randomizeId();
        this.randomizePID();
        this.bloomFilter = this.getRandomBloomfilter();
        this.dateMillis = this.getRandomDateTimeLong();
        this.versionId = this.getRandomInteger(1, 999);
        return this;
    }

    private String generatePatientId() {
        return "pseudo-" + this.id;
    }

    public PatientResource setBloomFilter(String bloomFilter) {
        this.bloomFilter = bloomFilter;
        return this;
    }

    public PatientResource setSource(String source) {
        this.source = "mailto:medic@" + source + ".org";
        return this;
    }

    public PatientResource randomizePID() {
        this.randomPID = this.getRandomId();
        return this;
    }

    @Override
    public Patient build() {
        Patient p = new Patient();
        Extension e = new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("masked"));
        p.setId(this.generatePatientId());
        p.getIdentifier().add(new Identifier().setSystem("https://eyematics.org/sid/dic-pseudonym").setValue(this.generatePatientId()));
        p.getIdentifier().add(new Identifier().setSystem("https://eyematics.org/sid/bloom-filter").setValue(this.bloomFilter));
        p.getIdentifier().add(new Identifier().setValue(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        p.getMeta().setLastUpdated(new Date(this.dateMillis));
        p.getMeta().setVersionId(Integer.toString(this.versionId));
        p.getMeta().setSource(this.source);
        p.getMeta().getProfile().add(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient|2024.0.0"));
        p.getMeta().getProfile().add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        Identifier identifier = new Identifier();
        identifier.setUse(Identifier.IdentifierUse.USUAL);
        identifier.setType(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("MR")));
        identifier.setSystem("http://clinic.org/KIS/PID");
        identifier.setValue(this.randomPID);
        identifier.setAssigner(new Reference().setIdentifier(new Identifier().setSystem("http://fhir.de/sid/arge-ik/iknr").setValue("007")).setDisplay("Clinic"));
        p.getIdentifier().add(identifier);
        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.getExtension().add(e);
        name.getFamilyElement().addExtension(e);
        name.addGivenElement().addExtension(e);
        p.addName(name);
        p.setGender(this.getRandomGender());
        p.getBirthDateElement().addExtension(e);
        Address address = new Address();
        address.setType(Address.AddressType.PHYSICAL);
        address.addLineElement().addExtension(e);
        address.getCityElement().addExtension(e);
        address.getPostalCodeElement().addExtension(e);
        p.addAddress(address);
        return p;
    }

    private Enumerations.AdministrativeGender getRandomGender() {
        return Enumerations.AdministrativeGender.values()[this.getRandomInteger(0, Enumerations.AdministrativeGender.values().length - 1)];
    }

}
