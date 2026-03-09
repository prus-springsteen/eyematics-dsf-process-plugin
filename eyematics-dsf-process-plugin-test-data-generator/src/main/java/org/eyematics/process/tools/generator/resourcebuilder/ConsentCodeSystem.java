package org.eyematics.process.tools.generator.resourcebuilder;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public enum ConsentCodeSystem {

    MDAT_WISSENSCHAFTLICH_NUTZEN("8", "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU"),
    PROMDAT_PATIENTENBEFRAGUNG_ERHEBEN("64", "PROMDAT Patientenbefragung erheben");

    private final String code;
    private final String display;

    private ConsentCodeSystem(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public CodeableConcept getCodeSystem() {
        String system = "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3";
        String generalCode = "2.16.840.1.113883.3.1937.777.24.5.3.";
        return new CodeableConcept(new Coding(system, generalCode + this.code, this.display));
    }
}