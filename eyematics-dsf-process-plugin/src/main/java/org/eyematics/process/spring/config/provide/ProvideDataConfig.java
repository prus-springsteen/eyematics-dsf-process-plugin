package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.constant.ProvideConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvideDataConfig {

    @ProcessDocumentation(
            required = true,
            processNames = {"eyematicsorg_eyematicsProvideProcess" },
            description = "Define the waiting time (in seconds) for acknowledgment according to the amount of patients (and their corresponding data) per provision Bundle. [min = 5; max = 60]"
    )
    @Value("${org.eyematics.provide.wait.duration.patients.per.bundle:10}")
    private long waitingDurationPatientsPerBundle;

    public long getWaitingDurationPatientsPerBundle() {
        if (waitingDurationPatientsPerBundle < ProvideConstants.MINIMUM_ACKNOWLEDGEMENT_WAITING_DURATION_PER_PATIENT) {
            return ProvideConstants.MINIMUM_ACKNOWLEDGEMENT_WAITING_DURATION_PER_PATIENT;
        }
        if (waitingDurationPatientsPerBundle > ProvideConstants.MAXIMUM_ACKNOWLEDGEMENT_WAITING_DURATION_PER_PATIENT) {
            return ProvideConstants.MAXIMUM_ACKNOWLEDGEMENT_WAITING_DURATION_PER_PATIENT;
        }
        return waitingDurationPatientsPerBundle;
    }
}
