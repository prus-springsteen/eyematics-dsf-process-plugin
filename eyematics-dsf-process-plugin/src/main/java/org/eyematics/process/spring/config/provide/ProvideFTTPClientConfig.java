package org.eyematics.process.spring.config.provide;

import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import org.eyematics.process.utils.client.FTTPClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
public class ProvideFTTPClientConfig {

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "The base address of the fTTP server",
            recommendation = "Specify if you are using the process to request pseudonyms from the fTTP. Caution: The fTTP client is unable to follow redirects, specify the final url if the server redirects requests",
            example = "http://foo.bar"
    )
    @Value("${org.eyematics.provide.fttp.server.base.url:#{null}}")
    private String fttpServerBase;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "P12 encoded file with client-certificate used to authenticate against fTTP server",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/fttp_server_client_certificate.p12"
    )
    @Value("${org.eyematics.provide.fttp.client.server.certificate:#{null}}")
    private String fttpCertificate;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Password for the client-certificate defined via `org.eyematics.provide.fttp.certificate`",
            recommendation = "Use docker secret file to configure",
            example = "/run/secrets/fttp_client_server.password"
    )
    @Value("${org.eyematics.provide.fttp.client.server.certificate.password:#{null}}")
    private String fttpCertificatePassword;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Basic authentication username to authenticate against the fTTP server, set if the server requests authentication using basic authentication"
    )
    @Value("${org.eyematics.provide.fttp.client.basicauth.username:#{null}}")
    private String fttpBasicAuthUsername;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Basic authentication password to authenticate against the fTTP server, set if the server requests authentication using basic authentication",
            recommendation = "Use docker secret file to configure by using `${env_variable}_FILE`",
            example = "/run/secrets/fttp_server_basicauth.password"
    )
    @Value("${org.eyematics.provide.fttp.client.basicauth.password:#{null}}")
    private String fttpBasicAuthPassword;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Your organization API key provided by the fTTP, the fTTP API key cannot be defined via docker secret file and needs to be defined directly via the environment variable",
            recommendation = "Specify if you are using the process to request pseudonyms from the fTTP"
    )
    @Value("${org.eyematics.provide.fttp.api.key:#{null}}")
    private String fttpApiKey;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Study identifier specified by the fTTP"
    )
    @Value("${org.eyematics.provide.fttp.study:eyematics}")
    private String fttpStudy;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Pseudonymization domain target identifier specified by the fTTP",
            example = "dic_muenster",
            recommendation = "Specify if you are using the process to request pseudonyms from the fTTP"
    )
    @Value("${org.eyematics.provide.fttp.target:eyematics}")
    private String fttpTarget;

    @ProcessDocumentation(
            processNames = {"eyematicsorg_provideProcess"},
            description = "Connection timeout in milliseconds used when accessing the fTTP server, time until a connection needs to be established before aborting"
    )
    @Value("${org.eyematics.provide.fttp.client.timeout.connect:10000}")
    private int fttpConnectTimeout;


    public FTTPClientFactory getFTTPClientFactory() {
        Path certificatePath = checkExists(fttpCertificate);
        char[] fttpClientCertificatePassword = fttpCertificatePassword == null ? null : fttpCertificatePassword.toCharArray();
        return new FTTPClientFactory(fttpServerBase, certificatePath, fttpClientCertificatePassword,
                fttpBasicAuthUsername, fttpBasicAuthPassword, fttpStudy, fttpTarget, fttpApiKey, fttpConnectTimeout);
    }

    private Path checkExists(String file) {
        if (file == null) {
            return null;
        } else {
            Path path = Paths.get(file);
            if (!Files.isReadable(path)) throw new RuntimeException(path.toString() + " not readable");
            return path;
        }
    }
}
