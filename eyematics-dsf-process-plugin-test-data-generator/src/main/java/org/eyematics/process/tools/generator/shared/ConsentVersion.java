package org.eyematics.process.tools.generator.shared;

import org.hl7.fhir.r4.model.Consent;

public enum ConsentVersion {
    VERSION_1_6d("2.16.840.1.113883.3.1937.777.24.2.1790"),
    VERSION_1_6f("2.16.840.1.113883.3.1937.777.24.2.1791"),
    VERSION_1_7_2("2.16.840.1.113883.3.1937.777.24.2.2079");

    private final String policyURI;

    private ConsentVersion(String policyURI){
        this.policyURI = policyURI;
    }

    public Consent.ConsentPolicyComponent getPolicyComponent() {
        return new Consent.ConsentPolicyComponent().setUri(this.policyURI);
    }

    public String getPolicyURI() {
        return this.policyURI;
    }
}
