package org.eyematics.process.tools.generator.shared;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public enum ConsentCodeSystem {

    MDAT_WISSENSCHAFTLICH_NUTZEN("8", "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU"),
    MDAT_RETROSPEKTIV_WISSENSCHAFTLICH_NUTZEN("46", "MDAT retrospektiv wissenschaftlich nutzen EU DSGVO NIVEAU"),
    PROMDAT_PATIENTENBEFRAGUNG_ERHEBEN("64", "PROMDAT Patientenbefragung erheben"),
    PROMDAT_WISSENSCHAFTLICH_NUTZEN("65", "PROMDAT wissenschaftlich nutzen auf EU DSGVO NIVEAU");

    private final String code;
    private final String display;

    private ConsentCodeSystem(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public CodeableConcept getCodeSystem() {
        String code = ConsentConstant.MII_CONSENT_BASE_CODE + "." + this.code;
        String system = "urn:oid:" + ConsentConstant.MII_CONSENT_BASE_CODE;
        return new CodeableConcept(new Coding(system, code, this.display));
    }

    public String getCode() {
        return ConsentConstant.MII_CONSENT_BASE_CODE + "." + this.code;
    }

    public String getDisplay() {
        return this.display;
    }
}
