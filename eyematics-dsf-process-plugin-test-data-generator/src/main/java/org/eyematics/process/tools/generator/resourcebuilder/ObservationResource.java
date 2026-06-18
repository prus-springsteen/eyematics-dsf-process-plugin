package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.hl7.fhir.r4.model.*;

import java.util.Date;


public class ObservationResource extends AbstractFHIRResourceBuilder<Observation, ObservationResource> {

    private Date effectiveDate;
    private String subject;
    private double vas;
    private String position;
    private String display;
    private double sphere;
    private double cylinder;
    private String distance;
    private String optotypes;
    private String dilatedPupil;
    private String pinhole;

    public ObservationResource() {
        super();
        this.effectiveDate = new Date();
        this.subject = "";
        this.vas = 0.0d;
        this.position = "";
        this.display = "";
        this.sphere = 0.0d;
        this.cylinder = 0.0d;
        this.distance = "";
        this.optotypes = "";
        this.dilatedPupil = "";
        this.pinhole = "";
    }

    @Override
    public ObservationResource randomize() {
        this.randomizeId();
        this.randomizeEffectiveDate();
        this.vas = this.getRandomDouble(0.1d, 1.6d);
        this.position = this.getRandomString("left", "right");
        this.display = this.getRandomDisplay();
        this.sphere = this.getRandomDouble(-1.5d, 1.5d);
        this.cylinder = this.getRandomDouble(-1.5d, 1.5d);
        this.distance = this.getRandomDistance();
        this.optotypes = this.getRandomOptotypes();
        this.dilatedPupil = this.getRandomString("performed", "Not performed");
        this.pinhole = this.getRandomString("Device used (finding)", "Not used");
        return this;
    }

    @Override
    public Observation build() {
        Observation o = new Observation();
        o.setId(this.id);
        o.setMeta(new Meta().addProfile("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/observation-visual-acuity"));
        o.getMeta().getProfile().add(new CanonicalType(ConsentConstant.CHARACTERISTIC_TO_DELETE));
        o.getMeta().setLastUpdated(this.effectiveDate);
        o.setEffective(new DateTimeType(this.effectiveDate));
        o.setSubject(new Reference(this.subject));
        o.getIdentifier().add(this.getRandomIdentifier());
        o.setStatus(Observation.ObservationStatus.FINAL);
        o.addCategory().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/observation-category").setCode("exam");
        o.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "260246004", "Visual Acuity finding")));
        o.setValue(new Quantity(this.vas).setSystem("https://imi-ms.github.io/eyematics-kds/CodeSystem-vs-units.html#vs-units-VAS").setCode("Decimal"));
        o.setBodySite(new CodeableConcept(new Coding("http://snomed.info/sct", "1290041000", "Entire " + this.position + " eye proper (body structure)")));
        Extension subExtension = new Extension("https://eyematics.org/fhir/eyematics-kds/StructureDefinition/LensDuringVATestSpecification");
        subExtension.addExtension("type", new CodeableConcept(new Coding("http://snomed.info/sct", "50121007", this.display)));
        subExtension.addExtension("sphere", new DecimalType(this.sphere));
        subExtension.addExtension("cylinder", new DecimalType(this.cylinder));
        o.addComponent().addExtension(subExtension);
        o.addComponent().setCode(new CodeableConcept(new Coding("http://loinc.org", "29074-2", this.position + " Eye position")));
        o.addComponent().setValue(new CodeableConcept(new Coding("http://snomed.info/sct", "50121007", this.display)));
        o.addComponent().setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "252124009", "Test distance")));
        o.addComponent().setValue(new CodeableConcept(new Coding("http://loinc.org", "50121007", this.distance)));
        o.addComponent().setCode(new CodeableConcept(new Coding("https://eyematics.org/fhir/eyematics-kds/ValueSet/va-optotypes", "VS_VA_Optotypes", "Visual Acuity Optotypes (Experimental)")));
        o.addComponent().setValue(new CodeableConcept(new Coding("http://loinc.org", "LA25497-1", this.optotypes)));
        o.addComponent().setCode(new CodeableConcept(new Coding( "http://snomed.info/sct", "37125009", "Dilated pupil (finding)")));
        o.addComponent().setValue(new CodeableConcept(new Coding("http://loinc.org", "LA25497-1", this.dilatedPupil + " (qualifier value)")));
        o.addComponent().setCode(new CodeableConcept(new Coding( "http://snomed.info/sct", "257492003", "Pinhole (physical object)")));
        o.addComponent().setValue(new CodeableConcept(new Coding("http://snomed.info/sct", "373062004", this.pinhole)));
        return o;
    }

    public ObservationResource setSubject(String subject) {
        this.subject = "Patient/" + subject;
        return this;
    }

    public ObservationResource randomizeSubject() {
        return this.setSubject(this.getRandomId());
    }

    public ObservationResource setSubject(Patient patient) {
        return this.setSubject(patient.getId());
    }

    private String getRandomDisplay() {
        int displayNumber = this.getRandomInteger(0, 7);
        return switch (displayNumber) {
            case 0 -> "Uncorrected visual acuity";
            case 1 -> "Eye glasses, device";
            case 2 -> "Contact lenses, device";
            case 3 -> "trial-lenses-autorefraction";
            case 4 -> "trial-lenses-manifest-without-cycloplegia";
            case 5 -> "trial-lenses-manifest-with-cycloplegia";
            case 6 -> "trial-lenses-retinoscopy";
            default -> "trial-lenses-unspecified-origin";
        };
    }

    private String getRandomDistance() {
        int displayNumber = this.getRandomInteger(0, 2);
        return switch (displayNumber) {
            case 0 -> "Far";
            case 1 -> "Intermediate";
            default -> "Near";
        };
    }

    private String getRandomOptotypes() {
        int displayNumber = this.getRandomInteger(0, 16);
        return switch (displayNumber) {
            case 0 -> "Numbers";
            case 1 -> "Snellen";
            case 2 -> "Landolt C";
            case 3 -> "Allen figure";
            case 4 -> "Cambridge crowded letter charts";
            case 5 -> "Cardiff acuity cards";
            case 6 -> "E test";
            case 7 -> "HOTV";
            case 8 -> "Kay picture test";
            case 9 -> "Keeler acuity cards";
            case 10 -> "Lea Symbol Test";
            case 11 -> "Sheridan Gardiner Test";
            case 12 -> "Sjogren's Hand Test";
            case 13 -> "Sonsken charts";
            case 14 -> "Stycar vision test";
            case 15 -> "Teller acuity cards";
            default -> "Treatment chart";
        };
    }

    public ObservationResource setEffectiveDate(Date date) {
        this.effectiveDate = date;
        return this;
    }

    public ObservationResource randomizeEffectiveDate() {
        return this.setEffectiveDate(this.getRandomDate());
    }
}
