package org.eyematics.process.utils.client;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.v1.ProcessPluginApi;
import org.eyematics.process.utils.client.logging.DataLogger;
import org.eyematics.process.utils.client.token.OAuth2TokenClient;
import org.eyematics.process.utils.client.token.OAuth2TokenProvider;
import org.eyematics.process.utils.client.token.TokenClient;
import org.eyematics.process.utils.client.token.TokenProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FhirClientFactoryProviderImpl implements FhirClientFactoryProvider {

    private final FhirContext fhirContext;
    private final TokenProvider tokenProvider;
    private final DataLogger dataLogger;
    private final Path trustStorePath;
    private final Path certificatePath;
    private final Path privateKeyPath;
    private final String proxyUrl;
    private final String proxyUsername;
    private final String proxyPassword;
    private final char[] fhirStorePrivateKeyPassword;
    private final int fhirStoreConnectTimeout;
    private final int fhirStoreSocketTimeout;
    private final int fhirStoreConnectionRequestTimeout;
    private final String fhirStoreBaseUrl;
    private final String fhirStoreUsername;
    private final String fhirStorePassword;
    private final String fhirStoreBearerToken;
    private final boolean fhirStoreHapiClientVerbose;
    private final String localIdentifierValue;
    private final boolean fhirClientConnectionTestEnabled;

    public FhirClientFactoryProviderImpl(FhirContext fhirContext, ProcessPluginApi api, String fhirStoreOAuth2TrustStore, String fhirStoreOAuth2ProxyUrl,
                                         String fhirStoreOAuth2ProxyUsername, String fhirStoreOAuth2ProxyPassword, String fhirStoreOAuth2IssuerUrl,
                                         String fhirStoreOAuth2DiscoveryPath, String fhirStoreOAuth2ClientId, String fhirStoreOAuth2ClientSecret,
                                         int fhirStoreOAuth2ConnectTimeout, int fhirStoreOAuth2SocketTimeout, boolean fhirStoreOAuth2DiscoveryValidationLenient,
                                         boolean fhirDataLoggingEnabled, String fhirStoreTrustStore, String fhirStoreCertificate, String fhirStorePrivateKey,
                                         String fhirStoreProxyUrl, String fhirStoreProxyUsername, String fhirStoreProxyPassword, char[] fhirStorePrivateKeyPassword,
                                         int fhirStoreConnectTimeout, int fhirStoreSocketTimeout, int fhirStoreConnectionRequestTimeout, String fhirStoreBaseUrl,
                                         String fhirStoreUsername, String fhirStorePassword, String fhirStoreBearerToken, boolean fhirStoreHapiClientVerbose,
                                         String localIdentifierValue, boolean fhirClientConnectionTestEnabled) {
        this.fhirContext = fhirContext;
        this.trustStorePath = this.checkExists(fhirStoreTrustStore);
        this.certificatePath = this.checkExists(fhirStoreCertificate);
        this.privateKeyPath = this.checkExists(fhirStorePrivateKey);

        if (fhirStoreProxyUrl == null && api.getProxyConfig().isEnabled() && !api.getProxyConfig().isNoProxyUrl(fhirStoreBaseUrl)) {
            this.proxyUrl = api.getProxyConfig().getUrl();
            this.proxyUsername = api.getProxyConfig().getUsername();
            this.proxyPassword = api.getProxyConfig().getPassword() == null ? null : new String(api.getProxyConfig().getPassword());
        } else {
            this.proxyUrl = fhirStoreProxyUrl;
            this.proxyUsername = fhirStoreProxyUsername;
            this.proxyPassword = fhirStoreProxyPassword;
        }

        TokenClient tokenClient = this.tokenClient(api, fhirStoreOAuth2TrustStore, fhirStoreOAuth2ProxyUrl, fhirStoreOAuth2ProxyUsername, fhirStoreOAuth2ProxyPassword, fhirStoreOAuth2IssuerUrl,
                fhirStoreOAuth2DiscoveryPath, fhirStoreOAuth2ClientId, fhirStoreOAuth2ClientSecret, fhirStoreOAuth2ConnectTimeout, fhirStoreOAuth2SocketTimeout,
                fhirStoreOAuth2DiscoveryValidationLenient);
        this.tokenProvider = this.tokenProvider(tokenClient);
        this.dataLogger = new DataLogger(fhirDataLoggingEnabled, fhirContext);

        this.fhirStorePrivateKeyPassword = fhirStorePrivateKeyPassword;
        this.fhirStoreConnectTimeout = fhirStoreConnectTimeout;
        this.fhirStoreSocketTimeout = fhirStoreSocketTimeout;
        this.fhirStoreConnectionRequestTimeout = fhirStoreConnectionRequestTimeout;
        this.fhirStoreBaseUrl = fhirStoreBaseUrl;
        this.fhirStoreUsername = fhirStoreUsername;
        this.fhirStorePassword = fhirStorePassword;
        this.fhirStoreBearerToken = fhirStoreBearerToken;
        this.fhirStoreHapiClientVerbose = fhirStoreHapiClientVerbose;
        this.localIdentifierValue = localIdentifierValue;
        this.fhirClientConnectionTestEnabled = fhirClientConnectionTestEnabled;
    }

    @Override
    public FhirClientFactory create() {
        return new FhirClientFactory(this.trustStorePath, this.certificatePath, this.privateKeyPath, this.fhirStorePrivateKeyPassword,
                this.fhirStoreConnectTimeout, this.fhirStoreSocketTimeout, this.fhirStoreConnectionRequestTimeout, this.fhirStoreBaseUrl,
                this.fhirStoreUsername, this.fhirStorePassword, this.fhirStoreBearerToken, this.tokenProvider, this.proxyUrl, this.proxyUsername,
                this.proxyPassword, this.fhirStoreHapiClientVerbose,
                FhirClientFactory.DEFAULT_INITIAL_POLLING_INTERVAL_MILLISECONDS, this.fhirContext, this.localIdentifierValue,
                this.dataLogger, false, this.fhirClientConnectionTestEnabled);
    }

    private TokenProvider tokenProvider(TokenClient tokenClient) {
        return new OAuth2TokenProvider(tokenClient);
    }

    private TokenClient tokenClient(ProcessPluginApi api, String fhirStoreOAuth2TrustStore, String fhirStoreOAuth2ProxyUrl,
                                    String fhirStoreOAuth2ProxyUsername, String fhirStoreOAuth2ProxyPassword, String fhirStoreOAuth2IssuerUrl,
                                    String fhirStoreOAuth2DiscoveryPath, String fhirStoreOAuth2ClientId, String fhirStoreOAuth2ClientSecret,
                                    int fhirStoreOAuth2ConnectTimeout, int fhirStoreOAuth2SocketTimeout, boolean fhirStoreOAuth2DiscoveryValidationLenient) {
        Path trustStoreOAuth2Path = checkExists(fhirStoreOAuth2TrustStore);

        String proxyUrl = fhirStoreOAuth2ProxyUrl, proxyUsername = fhirStoreOAuth2ProxyUsername,
                proxyPassword = fhirStoreOAuth2ProxyPassword;
        if (proxyUrl == null && api.getProxyConfig().isEnabled() && !api.getProxyConfig().isNoProxyUrl(fhirStoreOAuth2IssuerUrl)) {
            proxyUrl = api.getProxyConfig().getUrl();
            proxyUsername = api.getProxyConfig().getUsername();
            proxyPassword = api.getProxyConfig().getPassword() == null ? null : new String(api.getProxyConfig().getPassword());
        }

        return new OAuth2TokenClient(fhirStoreOAuth2IssuerUrl, fhirStoreOAuth2DiscoveryPath, fhirStoreOAuth2ClientId,
                fhirStoreOAuth2ClientSecret, fhirStoreOAuth2ConnectTimeout, fhirStoreOAuth2SocketTimeout,
                trustStoreOAuth2Path, proxyUrl, proxyUsername, proxyPassword,
                fhirStoreOAuth2DiscoveryValidationLenient);
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
