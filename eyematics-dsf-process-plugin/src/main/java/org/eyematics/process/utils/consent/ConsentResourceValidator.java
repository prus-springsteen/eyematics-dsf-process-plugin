package org.eyematics.process.utils.consent;

import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/*
 * This class is used to check if one or more consents are valid
 * @see https://simplifier.net/guide/mii-ig-modul-consent-2025/MII-IG-Modul-Consent/TechnischeImplementierung/FHIRProfile/Consent?version=2025.0.4
 */
public class ConsentResourceValidator {

    private final HashSet<String> consentVersion;

    public ConsentResourceValidator(List<String> consentVersion) {
        this.consentVersion = new HashSet<>();
        this.consentVersion.addAll(consentVersion);
    }

    public ConsentResourceValidator(String... consentVersion) {
        this(List.of(consentVersion));
    }

    public void addBCVersion(String consentVersion) {
        this.consentVersion.add(consentVersion);
    }

    public void removeBCVersion(String consentVersion) {
        this.consentVersion.remove(consentVersion);
    }

    // 1.) Check if consent is active
    public boolean isConsentStatusValid(Consent consent) {
        return consent.getStatus() != null && consent.getStatus().equals(Consent.ConsentState.ACTIVE);
    }

    // 2.) Check if consent has valid consent scope
    public boolean isConsentScopeValid(Consent consent) {
        ConsentScope consentScope = ConsentScope.fromCode(ConsentScope.RESEARCH.toCode());
        return consent.hasScope() && consent.getScope()
                .hasCoding(consentScope.getSystem(), consentScope.toCode());
    }

    // 3.0) Check if consent has a valid category
    private boolean isConsentCategoryValid(Consent consent) {
        return consent.hasCategory() && consent.getCategory().size() >= 2;
    }

    // 3.1) Check if consent has a valid category (LOINC)
    public boolean isConsentValuesetCategoryValid(Consent consent) {
        return this.isConsentCategoryValid(consent)
                && consent.getCategory()
                .stream()
                .anyMatch(c -> c.hasCoding("http://loinc.org", "57016-8"));
    }

    // 3.2) Check if consent has a valid category (MII)
    public boolean isMIIBroadConsentCategoryValid(Consent consent) {
        return this.isConsentCategoryValid(consent)
                && consent.getCategory()
                .stream()
                .anyMatch(c ->
                        c.hasCoding("https://www.medizininformatik-initiative.de/fhir/modul-consent/CodeSystem/mii-cs-consent-consent_category",
                                "2.16.840.1.113883.3.1937.777.24.2.184"));
    }

    // 4) Check if consent has a (valid) patient identifier
    public boolean isConsentPatientIdentifierValid(Consent consent) {
        if (consent.getPatient().getIdentifier().isEmpty()) return true;
        return consent.getPatient().getIdentifier().hasSystem() && consent.getPatient().getIdentifier().hasValue();
    }

    // 5) Check if consent has time of consent signature / creation
    public boolean hasTimeOfConsentCreation(Consent consent) {
        return consent.hasDateTimeElement();
    }

    // 6) Check if consent has a valid policy
    public boolean isConfiguredConsentPolicyValid(Consent consent) {
        return consent.getPolicy() != null
                && consent.getPolicy().size() == 1
                && consent.getPolicy().get(0).getUri() != null
                && (this.consentVersion.contains(consent.getPolicy().get(0).getUri())
                || this.consentVersion.contains(consent.getPolicy().get(0).getUri().replace("urn:oid:", "")));
    }

    // 7) Check for valid Provision(s)
    private boolean isProvisionValid(Consent.provisionComponent provision) {
        return provision.hasType() && provision.hasPeriod();
    }

    private boolean isProvisionTypeDeny(Consent.provisionComponent provision) {
        return provision.getType().equals(Consent.ConsentProvisionType.DENY);
    }

    private boolean isProvisionTypePermit(Consent.provisionComponent provision) {
        return provision.getType().equals(Consent.ConsentProvisionType.PERMIT);
    }

    private boolean isProvisionTypeValid(Consent.provisionComponent provision) {
        return this.isProvisionTypeDeny(provision) || this.isProvisionTypePermit(provision);
    }

    private boolean isProvisionPeriodValid(Consent.provisionComponent provision) {
        if (!provision.getPeriod().hasStart() || !provision.getPeriod().hasEnd()) return false;
        return provision.getPeriod().getStart().before(provision.getPeriod().getEnd());
    }

    private boolean isProvisionActionValid(Consent.provisionComponent provision) {
        return provision.getAction().isEmpty();
    }

    private boolean isProvisionCodeValid(Consent.provisionComponent provision) {
        return provision.getCode().isEmpty();
    }

    private boolean hasParentProvision(Consent consent) {
        return consent.hasProvision();
    }

    private boolean hasParentProvisionChildProvision(Consent.provisionComponent provision) {
        return provision.hasProvision();
    }

    private boolean isChildProvisionChildProvisionEmpty(Consent.provisionComponent provision) {
        return provision.getProvision().isEmpty();
    }

    private boolean hasChildProvisionCode(Consent.provisionComponent provision) {
        return !provision.getCode().isEmpty();
    }

    private boolean isValidChildProvisionCodingSystem(Consent.provisionComponent provision) {
        return provision.getCode()
                .stream()
                .anyMatch(c -> c.getCoding().get(0)
                        .getSystem()
                        .equals("urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"));
    }

    private boolean isValidChildProvision(Consent.provisionComponent provision) {
        if (!(this.isProvisionTypeDeny(provision) || this.isProvisionTypePermit(provision))) return false;
        if (!this.isProvisionPeriodValid(provision)) return false;
        if (!this.isProvisionActionValid(provision)) return false;
        if (!this.isChildProvisionChildProvisionEmpty(provision)) return false;
        if (!this.hasChildProvisionCode(provision)) return false;
        if (!this.isValidChildProvisionCodingSystem(provision)) return false;
        return true;
    }

    public boolean isValidConsentStructure(Consent consent) {
        if (!this.isConsentStatusValid(consent)) return false;
        if (!this.isConsentScopeValid(consent)) return false;
        if (!this.isConsentValuesetCategoryValid(consent)) return false;
        if (!this.isMIIBroadConsentCategoryValid(consent)) return false;
        if (!this.isConsentPatientIdentifierValid(consent)) return false;
        if (!this.hasTimeOfConsentCreation(consent)) return false;
        if (!this.isConfiguredConsentPolicyValid(consent)) return false;
        if (!this.hasParentProvision(consent)) return false;
        if (!this.isProvisionTypeDeny(consent.getProvision())) return false;
        if (!this.isProvisionPeriodValid(consent.getProvision())) return false;
        if (!this.isProvisionActionValid(consent.getProvision())) return false;
        if (!this.isProvisionCodeValid(consent.getProvision())) return false;
        if (this.hasParentProvisionChildProvision(consent.getProvision())) {
            return consent.getProvision()
                    .getProvision()
                    .stream()
                    .allMatch(this::isValidChildProvision);
        }
        return false;
    }

    public boolean isValidConsent(Consent consent, String... codes) {
        return this.isValidConsent(consent, List.of(codes));
    }

    public boolean isValidConsent(Consent consent, List<String> codes) {
        return this.isValidConsentStructure(consent) && this.isProvisionPermitted(consent.getProvision(), codes);
    }

    private List<Consent.provisionComponent> getChildProvision(Consent.provisionComponent provision, String code) {
        return provision.getProvision()
                .stream()
                .filter(p -> code.equals(p.getCode().get(0).getCoding().get(0).getCode()))
                .toList();
    }

    public boolean isProvisionPermitted(Consent.provisionComponent provisionComponent, String... codes) {
        return this.isProvisionPermitted(provisionComponent, List.of(codes));
    }

    public boolean isProvisionPermitted(Consent.provisionComponent provisionComponent, List<String> codes) {
        for (String code : codes) {
            List<Consent.provisionComponent> provision = this.getChildProvision(provisionComponent, code);
            if (provision.size() > 1) return false;
        }
        return true;
    }

    public Optional<Consent> getPermittedTimeIntervalls(Consent consent, List<String> codes) {
        if (!this.isValidConsentStructure(consent)) return Optional.empty();

        Consent consentOutput = new Consent();
        consentOutput.setId(consent.getId());
        consentOutput.setStatus(Consent.ConsentState.ACTIVE);
        consentOutput.getPatient().setReference(consent.getPatient().getReference());
        consentOutput.getPatient().setIdentifier(consent.getPatient().getIdentifier());
        consentOutput.setDateTime(consent.getDateTimeElement().getValue());

        for (String code : codes) {
            List<Consent.provisionComponent> provision = this.getChildProvision(consent.getProvision(), code);
            if (provision.size() == 1 && provision.get(0).getType().equals(Consent.ConsentProvisionType.PERMIT)) {
                consentOutput.getProvision().addProvision(provision.get(0));
            }
        }

        return consentOutput.getProvision().isEmpty() ? Optional.empty() : Optional.of(consentOutput);
    }

    private boolean isSameIdentifier(Consent consent, Consent consentOutput) {
        if (!consent.getPatient().hasIdentifier() && !consentOutput.getPatient().hasIdentifier()) return true;
        if (consent.getPatient().hasIdentifier() && consentOutput.getPatient().hasIdentifier()) {
            return consent.getPatient().getIdentifier().getValue().equals(consentOutput.getPatient().getIdentifier().getValue())
                    && consent.getPatient().getIdentifier().getSystem().equals(consentOutput.getPatient().getIdentifier().getSystem());
        }
        return false;
    }

    private boolean isSameReference(Consent consent, Consent consentOutput) {
        if (!consent.getPatient().hasReference() && !consentOutput.getPatient().hasReference()) return true;
        if (consent.getPatient().hasReference() && consentOutput.getPatient().hasReference()) {
            return consent.getPatient().getReference().equals(consentOutput.getPatient().getReference());
        }
        return false;
    }

    private boolean isSamePatient(Consent consent, Consent consentOutput) {
        return (this.isSameIdentifier(consent, consentOutput) && this.isSameReference(consent, consentOutput));
    }

    public Optional<Consent> getPermittedTimeIntervalls(Consent consent, String... codes) {
        return this.getPermittedTimeIntervalls(consent, List.of(codes));
    }

    public Optional<Consent> getPermittedTimeIntervalls(List<Consent> consent, List<String> codes) {
        Consent consentOutput = null;
        for (Consent c : consent) {
            Optional<Consent> consentOptional = this.getPermittedTimeIntervalls(c, codes);
            if (consentOptional.isPresent()) {
                if (consentOutput == null) {
                    consentOutput = consentOptional.get();
                } else {
                    if (this.isSamePatient(consentOptional.get(), consentOutput)) {
                        consentOutput.getProvision()
                                .getProvision()
                                .addAll(consentOptional.get().getProvision().getProvision());
                    }
                }
            }
        }
        return Optional.ofNullable(consentOutput);
    }
}
