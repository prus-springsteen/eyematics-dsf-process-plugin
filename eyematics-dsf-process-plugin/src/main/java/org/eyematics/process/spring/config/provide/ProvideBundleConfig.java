package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.constant.ProvideConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ProvideBundleConfig {

    @ProcessDocumentation(
            required = true,
            processNames = {"eyematicsorg_eyematicsProvideProcess" },
            description = "Define the amount of patients (and their corresponding data) per provision (transfer) Bundle. [min = 1; max = 1000]"
    )
    @Value("${org.eyematics.provide.max.patients.per.bundle:100}")
    private int maximumPatientsPerBundle;

    public int getMaximumPatientsPerBundle() {
        if (maximumPatientsPerBundle < ProvideConstants.MINIMUM_NUMBER_OF_PATIENTS_PER_BUNDLE) {
            return ProvideConstants.MINIMUM_NUMBER_OF_PATIENTS_PER_BUNDLE;
        }
        if (maximumPatientsPerBundle > ProvideConstants.MAXIMUM_NUMBER_OF_PATIENTS_PER_BUNDLE) {
            return ProvideConstants.MAXIMUM_NUMBER_OF_PATIENTS_PER_BUNDLE;
        }
        return maximumPatientsPerBundle;
    }
}
