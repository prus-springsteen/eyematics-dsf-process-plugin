package org.eyematics.process.tools.generator.resourcebuilder;

import org.eyematics.process.tools.generator.shared.ConsentCodeSystem;
import org.eyematics.process.tools.generator.shared.ConsentConstant;
import org.eyematics.process.tools.generator.shared.ConsentVersion;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;

import java.util.*;


public class ConsentResource extends AbstractFHIRResourceBuilder<Consent, ConsentResource>{

    protected Date startDate;
    protected Date endDate;
    private int versionId;
    private boolean hasMeta;
    private String source;
    private ConsentScope scope;
    private List<CodeableConcept> category = new ArrayList<>();
    private Consent.ConsentPolicyComponent policy;
    private Consent.ConsentState consentState;
    private boolean isDateTime;
    private boolean isDifferentDateTime;
    private boolean hasParentProvision;
    private CodeableConcept parentAction;
    private CodeableConcept parentConcept;
    private Reference patientReference;
    private Identifier identifier;
    private List<Consent.provisionComponent> provisionChildComponents;
    private HashMap<String, CodeableConcept> provisionChildComponentActionMap;
    private HashMap<String, Consent.provisionComponent> provisionChildComponentMap;

    public ConsentResource() {
        super();
        this.startDate = new Date();
        this.endDate = new Date();
        this.versionId = 1;
        this.hasMeta = true;
        this.setSource("clinic");
        this.scope = ConsentScope.RESEARCH;
        this.policy = ConsentVersion.VERSION_1_6d.getPolicyComponent();
        this.consentState = Consent.ConsentState.ACTIVE;
        this.category = new ArrayList<>();
        this.patientReference = new Reference(new IdType("Patient", this.getRandomId()));
        this.identifier = new Identifier();
        this.isDifferentDateTime = false;
        this.isDateTime = true;
        this.hasParentProvision = true;
        this.parentAction = null;
        this.parentConcept = null;
        this.provisionChildComponents = new ArrayList<>();
        this.provisionChildComponentActionMap = new HashMap<>();
        this.provisionChildComponentMap = new HashMap<>();
    }

    @Override
    public ConsentResource randomize() {
        this.randomizeId();
        this.setRandomDateIntervalls();
        this.versionId = this.getRandomInteger(1, 999);
        this.setRandomPolicy();
        this.setRandomConsentState();
        this.setRandomPatient();
        this.clearConcept();
        this.setRandomConcept();
        return this;
    }

    @Override
    public Consent build() {
        Consent c = new Consent();
        c.setId(this.id);

        if (this.hasMeta) {
            c.getMeta().setVersionId(Integer.toString(this.versionId));
            c.getMeta().setLastUpdated(new Date(this.getRandomDateTimeLong()));
            c.getMeta().setSource(this.source);
            c.getMeta().getProfile().add(new CanonicalType(ConsentConstant.MII_CONSENT_PROFILE));
        }

        c.setStatus(this.consentState);

        if (this.scope != null) {
            c.setScope(new CodeableConcept().setCoding(List.of(new Coding(this.scope.getSystem(),
                    this.scope.toCode(),
                    this.scope.getDisplay()))));
        }

        c.getCategory().addAll(this.category);

        c.setPatient(this.patientReference);
        c.getPatient().setIdentifier(this.identifier);

        Date timeTmp;
        if (this.isDifferentDateTime) {
            int randomInteger = this.getRandomInteger(0, 1000000000);
            if (this.getRandomBoolean()) {
                timeTmp = new Date(this.startDate.getTime() + randomInteger);
            } else {
                timeTmp = new Date(this.startDate.getTime() - randomInteger);
            }
        } else {
            timeTmp = this.startDate;
        }

        if (this.isDateTime) {
            if (timeTmp == null) timeTmp = new Date();
            c.setDateTime(timeTmp);
        } else {
            c.setDateTime(null);
        }

        c.getPolicy().add(this.policy);

        if (this.hasParentProvision) {
            Consent.provisionComponent provisionParentComponent = new Consent.provisionComponent()
                    .setType(Consent.ConsentProvisionType.DENY)
                    .setPeriod(new Period().setStart(this.startDate).setEnd(this.endDate));

            provisionParentComponent.addAction(this.parentAction);
            provisionParentComponent.addCode(this.parentConcept);

            for (Consent.provisionComponent childComponent : this.provisionChildComponents) {
                List<CodeableConcept> codeableConcepts = childComponent.getCode();
                for (CodeableConcept codeableConcept : codeableConcepts) {
                    List<Coding> codings = codeableConcept.getCoding();
                    for (Coding coding : codings) {
                        if (this.provisionChildComponentActionMap.containsKey(coding.getCode())) {
                            childComponent.addAction(this.provisionChildComponentActionMap.get(coding.getCode()));
                        }
                        if (this.provisionChildComponentMap.containsKey(coding.getCode())) {
                            childComponent.addProvision(this.provisionChildComponentMap.get(coding.getCode()));
                        }
                    }
                }
                provisionParentComponent.addProvision(childComponent);
            }
            c.setProvision(provisionParentComponent);
        } else {
            c.setProvision(null);
        }

        return c;
    }

    public ConsentResource hasMeta() {
        this.hasMeta = true;
        return this;
    }

    public ConsentResource hasNoMeta() {
        this.hasMeta = false;
        return this;
    }

    public ConsentResource setVersionId(int versionId) {
        this.versionId = versionId;
        return this;
    }

    public ConsentResource setSource(String organizationSource) {
        this.source = "mailto:medic@" + organizationSource + ".org";
        return this;
    }

    public ConsentResource setScope(ConsentScope scope) {
        this.scope = scope;
        return this;
    }

    public ConsentResource setState(Consent.ConsentState consentState) {
        this.consentState = consentState;
        return this;
    }

    public ConsentResource clearCategory() {
        this.category.clear();
        return this;
    }

    public ConsentResource addCategory(String system, String code, String display) {
        this.category.add(new CodeableConcept(new Coding(system, code, display)));
        return this;
    }

    public ConsentResource addCategory(CodeableConcept category) {
        this.category.add(category);
        return this;
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
        return this.setStartDate(startDate).setEndDate(endDate);
    }

    public ConsentResource setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public ConsentResource setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public ConsentResource setPolicy(Consent.ConsentPolicyComponent policy) {
        if (policy == null) {
            this.policy = new Consent.ConsentPolicyComponent();
        } else {
            this.policy = policy;
        }
        return this;
    }

    public ConsentResource setRandomPolicy() {
        this.policy = ConsentVersion.values()[this.getRandomInteger(0, ConsentVersion.values().length)].getPolicyComponent();
        return this;
    }

    public ConsentResource setRandomConsentState() {
        this.consentState = Consent.ConsentState.values()[this.getRandomInteger(0, Consent.ConsentState.values().length - 1)];
        return this;
    }

    public ConsentResource isDifferentDateTime() {
        this.isDifferentDateTime = true;
        return this;
    }

    public ConsentResource isSameDateTime() {
        this.isDifferentDateTime = false;
        return this;
    }

    public ConsentResource isDateTime() {
        this.isDateTime = true;
        return this;
    }

    public ConsentResource isNoDateTime() {
        this.isDateTime = false;
        return this;
    }

    public ConsentResource hasParentProvision() {
        this.hasParentProvision = true;
        return this;
    }

    public ConsentResource hasNoParentProvision() {
        this.hasParentProvision = false;
        return this;
    }

    private Consent.provisionComponent createProvisionComponent(Coding concept,
                                                                Consent.ConsentProvisionType type,
                                                                Date start,
                                                                Date end) {
        Consent.provisionComponent provisionChildComponent = new Consent.provisionComponent()
                .setType(type)
                .setPeriod(new Period().setStart(start).setEnd(end));
        provisionChildComponent.addCode(new CodeableConcept(concept));
        return provisionChildComponent;
    }

    public ConsentResource clearConcept() {
        this.provisionChildComponentActionMap.clear();
        this.provisionChildComponents.clear();
        return this;
    }

    public ConsentResource addConcept(Coding concept,
                                      Consent.ConsentProvisionType type,
                                      Date start,
                                      Date end) {
        Consent.provisionComponent provisionChildComponent = this.createProvisionComponent(concept, type, start, end);
        this.provisionChildComponents.add(provisionChildComponent);
        return this;
    }

    public ConsentResource addConcept(CodeableConcept concept,
                                      Consent.ConsentProvisionType type,
                                      Date start,
                                      Date end) {
        return this.addConcept(concept.getCodingFirstRep(), type, start, end);
    }

    public ConsentResource addConcept(ConsentCodeSystem concept,
                                      Consent.ConsentProvisionType type,
                                      Date start,
                                      Date end) {
        return this.addConcept(concept.getCodeSystem(), type, start, end);
    }

    public ConsentResource addConcept(ConsentCodeSystem concept,
                                      Consent.ConsentProvisionType type) {
        return this.addConcept(concept, type, this.startDate, this.endDate);
    }

    private CodeableConcept getNotSupportedCoding() {
        return new CodeableConcept(new Coding("not supported",
                "not supported",
                "not supported"));
    }

    public ConsentResource addParentAction() {
        this.parentAction = this.getNotSupportedCoding();
        return this;
    }

    public ConsentResource clearParentAction() {
        this.parentAction = null;
        return this;
    }

    public ConsentResource addChildAction(ConsentCodeSystem concept) {
        this.provisionChildComponentActionMap.put(concept.getCode(), this.getNotSupportedCoding());
        return this;
    }

    public ConsentResource clearChildAction(ConsentCodeSystem concept) {
        this.provisionChildComponentActionMap.remove(concept.getCode());
        return this;
    }

    public ConsentResource addParentCode() {
        this.parentConcept = this.getNotSupportedCoding();
        return this;
    }

    public ConsentResource clearParentCode() {
        this.parentConcept = null;
        return this;
    }

    public ConsentResource addChildConcept(ConsentCodeSystem provision, ConsentCodeSystem childProvision) {
        Consent.provisionComponent provisionChildChildComponent = this.createProvisionComponent(childProvision.getCodeSystem().getCodingFirstRep(),
                                                                                                Consent.ConsentProvisionType.PERMIT,
                                                                                                this.startDate,
                                                                                                this.endDate);
        this.provisionChildComponentMap.put(provision.getCode(), provisionChildChildComponent);
        return this;
    }

    public ConsentResource clearChildConcept(ConsentCodeSystem provision, ConsentCodeSystem childProvision) {
        this.provisionChildComponentMap.remove(provision.getCode());
        return this;
    }

    public ConsentResource setRandomConcept()  {
        int randomInteger = this.getRandomInteger(0, ConsentCodeSystem.values().length);
        for (int i = 0; i <= randomInteger; i++) {
            Consent.ConsentProvisionType type = this.endDate.after(new Date())
                    ? Consent.ConsentProvisionType.PERMIT : Consent.ConsentProvisionType.DENY;
            this.addConcept(ConsentCodeSystem.values()[i], type, this.startDate, this.endDate);
        }
        return this;
    }

    public ConsentResource setPatient(Patient patient) {
        if (patient == null) {
            this.patientReference = null;
        } else {
            this.patientReference = new Reference(new IdType("Patient", patient.getId()));
        }
        return this;
    }

    public ConsentResource setRandomPatient() {
        this.patientReference = new Reference(new IdType("Patient", this.getRandomId()));
        return this;
    }

    public ConsentResource addIdentifier(Identifier identifier) {
        this.identifier = identifier;
        return this;
    }

    public ConsentResource addIdentifier(String system, String value) {
        this.identifier = new Identifier().setSystem(system).setValue(value);
        return this;
    }

    public ConsentResource clearIdentifier() {
        this.identifier = null;
        return this;
    }

    public ConsentResource setEmpty() {
        this.id = "";
        this.startDate = new Date();
        this.endDate = new Date();
        this.versionId = 1;
        this.hasMeta = true;
        this.setSource("");
        this.scope = null;
        this.policy = new Consent.ConsentPolicyComponent();
        this.consentState = null;
        this.category.clear();
        this.patientReference = null;
        this.identifier = null;
        this.isDifferentDateTime = false;
        this.isDateTime = true;
        this.hasParentProvision = true;
        this.parentAction = null;
        this.parentConcept = null;
        this.provisionChildComponents.clear();
        this.provisionChildComponentActionMap.clear();
        this.provisionChildComponentMap.clear();
        return this;
    }
}
