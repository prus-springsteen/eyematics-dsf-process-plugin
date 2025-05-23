package org.eyematics.process.spring.config;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.KeyProviderImpl;
import org.eyematics.process.utils.logger.DataLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CryptoConfig {

    @Autowired
    private ProcessPluginApi api;

    @ProcessDocumentation(required = true,
            processNames = "DEV_DMS_PRIVATE_KEY_FILE",
            description = "Location of the DMS private-key as 4096 Bit RSA PEM encoded, not encrypted file",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/dms_private_key.pem")
    @Value("${dev.dms.private.key.file:#{null}}")
    private String dmsPrivateKeyFile;

    @ProcessDocumentation(required = true,
            processNames = "DEV_DMS_PUBLIC_KEY_FILE",
            description = "Location of the DMS public-key as 4096 Bit RSA PEM encoded file",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/dms_public_key.pem")
    @Value("${dev.dms.public.key.file:#{null}}")
    private String dmsPublicKeyFile;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeyProvider keyProviderDms() {
        return KeyProviderImpl.fromFiles(api, dmsPrivateKeyFile, dmsPublicKeyFile, new DataLogger(false, null));
    }
}

