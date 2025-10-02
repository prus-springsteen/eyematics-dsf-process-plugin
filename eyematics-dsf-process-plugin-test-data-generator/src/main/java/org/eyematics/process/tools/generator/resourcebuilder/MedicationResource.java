package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicationResource extends AbstractFHIRResourceBuilder<Medication, MedicationResource> {

    private String id;
    private String version;
    private String pzn;
    private String atc;
    private String manufacturerId;
    private String unitOfMeasure;
    private double numerator;
    private double denominator;
    private int amountOfIngredient;

    public MedicationResource() {
        super(new Medication());
    }

    @Override
    public MedicationResource randomize() {
        this.id = UUID.randomUUID().toString();
        this.version = this.getRandomDateTimeString();
        this.pzn = "PZN" + this.getRandomInteger(1000, 9999);
        this.atc = "ATC" + this.getRandomInteger(1000, 9999);
        this.manufacturerId = "MANUFACTURER_" + this.getRandomInteger(100, 999);
        this.unitOfMeasure = this.getUnitsOfMeasure();
        this.numerator = this.getRandomDouble(0.0d, 10.0d);
        this.denominator = this.getRandomDouble(0.0d, 10.0d);
        this.amountOfIngredient = this.getRandomInteger(0, 4);
        return this;
    }

    @Override
    public Medication build() {
        Medication m = new Medication();
        m.setId(this.id);
        m.getIdentifier().add(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue(this.id));
        CodeableConcept c = new CodeableConcept();
        c.addCoding(new Coding().setSystem("http://fhir.de/CodeSystem/bfarm/atc").setVersion(this.version).setCode(this.atc).setDisplay("Medication --- " + this.id).setUserSelected(false));
        c.addCoding(new Coding().setSystem("http://fhir.de/CodeSystem/ifa/pzn").setVersion(this.version).setCode(this.pzn).setUserSelected(false));
        c.setText("Injektionslösung");
        m.setCode(c);
        m.setStatus(Medication.MedicationStatus.ACTIVE);
        Identifier manufacturerIdentifier = new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue(this.manufacturerId);
        m.setManufacturer(new Reference().setType("Organization").setIdentifier(manufacturerIdentifier).setDisplay("Pharma --- " + this.manufacturerId));
        Coding formCoding = new Coding("https://standardterms.edqm.eu", "11201000", "Solution for injection").setVersion(this.version).setUserSelected(false);
        CodeableConcept form = new CodeableConcept(formCoding);
        m.setForm(form);
        m.getForm().setText("Solution for injection");
        m.getAmount().setNumerator(new Quantity(this.numerator).setUnit(this.unitOfMeasure).setSystem("http://unitsofmeasure.org").setCode(this.unitOfMeasure));
        m.getAmount().setDenominator(new Quantity(this.denominator).setSystem("http://unitsofmeasure.org").setCode("1"));
        List<Medication.MedicationIngredientComponent> ingredients = new ArrayList<>();
        for (int i = 0; i < amountOfIngredient + 1; i++) ingredients.add(this.getMedicationIngredientComponent(i));
        m.setIngredient(ingredients);
        Meta meta = new Meta();
        meta.setSource("https://pharma-idx-data-service");
        meta.getProfile().add(new CanonicalType("https://pharma-idx-data-service"));
        m.setMeta(meta);
        this.setResource(m);
        return this.getResource();
    }

    private Medication.MedicationIngredientComponent  getMedicationIngredientComponent(int id) {
        Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
        String medId = "#ing_" + Integer.toString(id + 1);
        ingredient.setId(medId);
        return switch (id) {
            case 0 -> ingredient.setItem(new Reference().setType("Substance").setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue("15823")).setDisplay("Natriumchlorid"));
            case 1 -> ingredient.setItem(new Reference().setType("Substance").setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue("16697")).setDisplay("Sucrose"));
            case 2 -> ingredient.setItem(new Reference().setType("Substance").setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue("16819")).setDisplay("Wasser für Injektionszwecke"));
            case 3 -> ingredient.setItem(new Reference().setType("Substance").setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue("24548")).setDisplay("Dinatriumhydrogenphosphat-7-Wasser"));
            default -> ingredient.setItem(new Reference().setType("Substance").setIdentifier(new Identifier().setUse(Identifier.IdentifierUse.TEMP).setValue("10136")).setDisplay("Natrium citrat"));
        };
    }

    private String getUnitsOfMeasure() {
        return this.getRandomString("ml", "µg");
    }
}
