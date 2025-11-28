package org.eyematics.process.spring.config;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.utils.client.logging.DataLogger;
import org.eyematics.process.utils.crypto.KeyProvider;
import org.eyematics.process.utils.crypto.KeyProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CryptoConfig {

    @Autowired
    private ProcessPluginApi api;

    @ProcessDocumentation(
            required = true,
            processNames = "eyematicsorg_receiveProcess",
            description = "Location of the DIC private-key as 4096-Bit RSA PEM encoded, not encrypted file",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/dic_private_key.pem")
    @Value("${org.eyematics.dic.receive.private.key.file:#{null}}")
    private String dicReceivePrivateKeyFile;

    @ProcessDocumentation(
            required = true,
            processNames = "eyematicsorg_receiveProcess",
            description = "Location of the DIC public-key as 4096-Bit RSA PEM encoded file",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/dic_public_key.pem")
    @Value("${org.eyematics.dic.receive.public.key.file:#{null}}")
    private String dicReceivePublicKeyFile;

    public KeyProvider keyProviderDicReceive() {
        return KeyProviderImpl.fromFiles(api, dicReceivePrivateKeyFile, dicReceivePublicKeyFile, new DataLogger(false, null));
    }
}

