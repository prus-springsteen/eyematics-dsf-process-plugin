package org.eyematics.process.tools.generator.shared;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public interface ConsentConstant {
    /*
    * MII Consent
    * @see https://simplifier.net/guide/mii-ig-modul-consent-2025/MII-IG-Modul-Consent?version=2025.0.4
     */
    String MII_CONSENT_BASE_CODE = "2.16.840.1.113883.3.1937.777.24.5.3";
    String MII_CONSENT_PROFILE = "https://www.medizininformatik-initiative.de/fhir/modul-consent/StructureDefinition/mii-pr-consent-einwilligung";
    String MII_CONSENT_CATEGORY = "https://www.medizininformatik-initiative.de/fhir/modul-consent/CodeSystem/mii-cs-consent-consent_category";
    CodeableConcept MII_DATA_PRIVACY_CODEABLE_CONCEPT = new CodeableConcept().addCoding(new Coding("http://loinc.org", "57016-8", "Privacy policy acknowledgement Document"));
    CodeableConcept MII_CONSENT_CATEGORY_CODEABLE_CONCEPT = new CodeableConcept().addCoding(new Coding(ConsentConstant.MII_CONSENT_CATEGORY, "2.16.840.1.113883.3.1937.777.24.2.184", null));

    String CHARACTERISTIC_TO_DELETE = "https://eyematics.org/fhir/StructureDefinition/remove-before-flight";
}
