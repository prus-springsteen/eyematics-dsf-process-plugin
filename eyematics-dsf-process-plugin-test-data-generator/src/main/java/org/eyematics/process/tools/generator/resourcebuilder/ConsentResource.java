package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ConsentResource extends AbstractFHIRResourceBuilder<Consent, ConsentResource>{

    private int id;
    private Date startDate;
    private Date endDate;
    private int versionId;
    private String source;
    private Consent.ConsentState consentState;
    private Reference patientReference;
    private Consent.provisionComponent provisionChildComponent;

    public ConsentResource() {
        super(new Consent());
    }

    @Override
    protected void init() {
        this.id = 1;
        this.startDate = new Date();
        this.endDate = new Date();
        this.versionId = 1;
        this.setSource("clinic");
        this.consentState = Consent.ConsentState.ACTIVE;
        this.patientReference = new Reference(new IdType("Patient", UUID.randomUUID().toString()));
        this.setConcept(ConsentCodeSystem.MDAT_ZUSAMMENFUEHREN_DRITTE);
    }

    @Override
    public ConsentResource randomize() {
        this.id = this.getRandomInteger(1, 999);
        this.setRandomDateIntervalls();
        this.versionId = this.getRandomInteger(1, 999);
        this.setRandomConsentState();
        this.setRandomPatient();
        this.setRandomConcept();
        return this;
    }

    @Override
    public Consent build() {
        Consent c = new Consent();
        c.setId(this.generateConsentID());
        c.getMeta().setVersionId(Integer.toString(this.versionId));
        c.getMeta().setLastUpdated(new Date(this.getRandomDateTimeLong()));
        c.getMeta().setSource(this.source);
        c.getMeta().getProfile().add(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/modul-consent/StructureDefinition/mii-pr-consent-einwilligung"));
        c.setStatus(this.consentState);
        ConsentScope consentScope = ConsentScope.fromCode(ConsentScope.RESEARCH.toCode());
        c.setScope(new CodeableConcept().setCoding(List.of(new Coding(consentScope.getSystem(),
                        consentScope.toCode(),
                        consentScope.getDisplay()))));
        List<CodeableConcept> category = List.of(
                new CodeableConcept().addCoding(new Coding("http://loinc.org",
                        "57016-8",
                        "Privacy policy acknowledgement Document")),
                new CodeableConcept().addCoding(new Coding("https://www.medizininformatik-initiative.de/fhir/modul-consent/CodeSystem/mii-cs-consent-consent_category",
                        "2.16.840.1.113883.3.1937.777.24.2.184", null)));
        c.setCategory(category);
        c.setPatient(this.patientReference);
        c.setDateTime(new Date(this.getRandomDateTimeLong()));
        c.setPolicy(List.of(new Consent.ConsentPolicyComponent().setUri("2.16.840.1.113883.3.1937.777.24.2.1791")));
        Consent.provisionComponent provisionParentComponent = new Consent.provisionComponent()
                .setType(Consent.ConsentProvisionType.DENY)
                .setPeriod(new Period().setStart(this.startDate).setEnd(this.endDate));
        provisionParentComponent.addProvision(this.provisionChildComponent);
        c.setProvision(provisionParentComponent);
        this.setResource(c);
        return c;
    }

    public ConsentResource setId(int id) {
        this.id = id;
        return this;
    }

    public ConsentResource setSource(String source) {
        this.source = "mailto:medic@" + source + ".org";
        return this;
    }

    private String generateConsentID() {
        return "consent-" + this.id;
    }

    public ConsentResource setRandomDateIntervalls() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.getRandomDateTimeLong());
        this.startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + this.getRandomInteger(5, 10));
        this.endDate = calendar.getTime();
        return this;
    }

    public ConsentResource setDateIntervalls(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public ConsentResource setConsentState(Consent.ConsentState consentState) {
        this.consentState = consentState;
        return this;
    }

    public ConsentResource setRandomConsentState() {
        this.consentState = Consent.ConsentState.values()[this.getRandomInteger(0, Consent.ConsentState.values().length - 1)];
        return this;
    }

    public ConsentResource setConcept(ConsentCodeSystem concept) {
        Consent.ConsentProvisionType type = this.endDate.after(new Date()) ? Consent.ConsentProvisionType.PERMIT : Consent.ConsentProvisionType.DENY;
        this.provisionChildComponent = new Consent.provisionComponent().setType(type)
                .setPeriod(new Period().setStart(this.startDate).setEnd(this.endDate));
        CodeableConcept codeableConcept = concept.getCodeSystem();
        this.provisionChildComponent.addCode(codeableConcept);
        return this;
    }

    public ConsentResource setRandomConcept()  {
        ConsentCodeSystem consentCodeSystem = ConsentCodeSystem.values()[this.getRandomInteger(0, ConsentCodeSystem.values().length)];
        return this.setConcept(consentCodeSystem);
    }

    public ConsentResource setPatient(Patient patient) {
       this.patientReference = new Reference(new IdType("Patient", patient.getId()));
       return this;
    }

    public ConsentResource setRandomPatient() {
        this.patientReference = new Reference(new IdType("Patient", UUID.randomUUID().toString()));
        return this;
    }
}
