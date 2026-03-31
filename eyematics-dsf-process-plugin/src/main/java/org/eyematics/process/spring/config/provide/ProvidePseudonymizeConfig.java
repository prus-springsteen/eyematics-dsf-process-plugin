package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.utils.pseudonymize.EyeMaticsMdatPseudonymizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvidePseudonymizeConfig {

    @ProcessDocumentation(
            processNames = {"eyematicsorg_eyematicsProvideProcess"},
            description = "",
            example = ""
    )
    @Value("${org.eyematics.provide.fhir.resource.pseudonymize.salt:EyeMatics")
    private String fhirResourcePseudonymizeSalt;


    public EyeMaticsMdatPseudonymizer getFhirResourcePseudonymizer() {
        return new EyeMaticsMdatPseudonymizer(fhirResourcePseudonymizeSalt);
    }
}
