package org.eyematics.process.spring.config.provide;

import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.consent.ConsentResourceValidator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvideConsentConfig {

    public ConsentResourceValidator getConsentResourceValidator() {
        return new ConsentResourceValidator(EyeMaticsConstants.MII_IG_MODUL_CONSENT_VERSIONS);
    }
}
