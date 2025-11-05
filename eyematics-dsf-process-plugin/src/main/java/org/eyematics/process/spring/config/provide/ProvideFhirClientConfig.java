package org.eyematics.process.spring.config.provide;

import org.eyematics.process.utils.client.FhirClientFactory;
import org.eyematics.process.utils.client.FhirClientFactoryProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;


@Configuration
public class ProvideFhirClientConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(required = true, processNames = {
			"eyematicsorg_provideProcess" }, description = "The base address of the DIC FHIR server to read/store FHIR resources", example = "http://foo.bar/fhir")
	@Value("${org.eyematics.provide.fhir.server.base.url:#{null}}")
	private String fhirStoreBaseUrl;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "PEM encoded file with one or more trusted root certificate to validate the DIC FHIR server certificate when connecting via https", recommendation = "Use docker secret file to configure", example = "/run/secrets/hospital_ca.pem")
	@Value("${org.eyematics.provide.fhir.server.trust.certificates:#{null}}")
	private String fhirStoreTrustStore;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "PEM encoded file with client-certificate, if DIC FHIR server requires mutual TLS authentication", recommendation = "Use docker secret file to configure", example = "/run/secrets/fhir_server_client_certificate.pem")
	@Value("${org.eyematics.provide.fhir.server.certificate:#{null}}")
	private String fhirStoreCertificate;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Private key corresponding to the DIC FHIR server client-certificate as PEM encoded file. Use *${env_variable}_PASSWORD* or *${env_variable}_PASSWORD_FILE* if private key is encrypted", recommendation = "Use docker secret file to configure", example = "/run/secrets/fhir_server_private_key.pem")
	@Value("${org.eyematics.provide.fhir.server.private.key:#{null}}")
	private String fhirStorePrivateKey;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Password to decrypt the DIC FHIR server client-certificate encrypted private key", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/fhir_server_private_key.pem.password")
	@Value("${org.eyematics.provide.fhir.server.private.key.password:#{null}}")
	private char[] fhirStorePrivateKeyPassword;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Basic authentication username, set if the server containing the FHIR data requests authentication using basic auth")
	@Value("${org.eyematics.provide.fhir.server.basicauth.username:#{null}}")
	private String fhirStoreUsername;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Basic authentication password, set if the server containing the FHIR data requests authentication using basic auth", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/fhir_server_basicauth.password")
	@Value("${org.eyematics.provide.fhir.server.basicauth.password:#{null}}")
	private String fhirStorePassword;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Bearer token for authentication, set if the server containing the FHIR data requests authentication using a bearer token, cannot be set using docker secrets")
	@Value("${org.eyematics.provide.fhir.server.bearer.token:#{null}}")
	private String fhirStoreBearerToken;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The timeout in milliseconds until a connection is established between the client and the DIC FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.timeout.connect:20000}")
	private int fhirStoreConnectTimeout;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The timeout in milliseconds used when requesting a connection from the connection manager between the client and the DIC FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.timeout.connection.request:20000}")
	private int fhirStoreConnectionRequestTimeout;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Maximum period of inactivity in milliseconds between two consecutive data packets of the client and the DIC FHIR server", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.timeout.socket:60000}")
	private int fhirStoreSocketTimeout;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The client will log additional debug output", recommendation = "Change default value only if exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.client.verbose:false}")
	private boolean fhirStoreHapiClientVerbose;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy location, set if the server containing the FHIR data can only be reached through a proxy, uses value from DEV_DSF_PROXY_URL if not set", example = "http://proxy.foo:8080")
	@Value("${org.eyematics.provide.fhir.server.proxy.url:#{null}}")
	private String fhirStoreProxyUrl;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy username, set if the server containing the FHIR data can only be reached through a proxy which requests authentication, uses value from DEV_DSF_PROXY_USERNAME if not set")
	@Value("${org.eyematics.provide.fhir.server.proxy.username:#{null}}")
	private String fhirStoreProxyUsername;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy password, set if the server containing the FHIR data can only be reached through a proxy which requests authentication, uses value from DEV_DSF_PROXY_PASSWORD if not set", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.eyematics.provide.fhir.server.proxy.password:#{null}}")
	private String fhirStoreProxyPassword;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The url of the oidc provider to request access tokens (token endpoint)", example = "http://foo.baz/realms/fhir-realm/protocol/openid-connect/token")
	@Value("${org.eyematics.provide.fhir.server.oauth2.issuer.url:#{null}}")
	private String fhirStoreOAuth2IssuerUrl;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The path for oidc discovery protocol", recommendation = "Change default value only if path differs from the oidc specification")
	@Value("${org.eyematics.provide.fhir.server.oauth2.discovery.path:/.well-known/openid-configuration}")
	private String fhirStoreOAuth2DiscoveryPath;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Identifier of the client (username) used for authentication when accessing the oidc provider token endpoint")
	@Value("${org.eyematics.provide.fhir.server.oauth2.client.id:#{null}}")
	private String fhirStoreOAuth2ClientId;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Secret of the client (password) used for authentication when accessing the oidc provider token endpoint", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.eyematics.provide.fhir.server.oauth2.client.password:#{null}}")
	private String fhirStoreOAuth2ClientSecret;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "The timeout in milliseconds until a connection is established between the client and the oidc provider", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.oauth2.timeout.connect:20000}")
	private int fhirStoreOAuth2ConnectTimeout;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Maximum period of inactivity in milliseconds between two consecutive data packets of the client and the oidc provider", recommendation = "Change default value only if timeout exceptions occur")
	@Value("${org.eyematics.provide.fhir.server.oauth2.timeout.socket:60000}")
	private int fhirStoreOAuth2SocketTimeout;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "PEM encoded file with one or more trusted root certificate to validate the oidc provider server certificate when connecting via https", recommendation = "Use docker secret file to configure", example = "/run/secrets/hospital_ca.pem")
	@Value("${org.eyematics.provide.fhir.server.oauth2.trust.certificates:#{null}}")
	private String fhirStoreOAuth2TrustStore;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy location, set if the oidc provider can only be reached through a proxy, uses value from DEV_DSF_PROXY_URL if not set", example = "http://proxy.foo:8080")
	@Value("${org.eyematics.provide.fhir.server.oauth2.proxy.url:#{null}}")
	private String fhirStoreOAuth2ProxyUrl;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy username, set if the oidc provider can only be reached through a proxy which requests authentication, uses value from DEV_DSF_PROXY_USERNAME if not set")
	@Value("${org.eyematics.provide.fhir.server.oauth2.proxy.username:#{null}}")
	private String fhirStoreOAuth2ProxyUsername;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "Proxy password, set if the oidc provider can only be reached through a proxy which requests authentication, uses value from DEV_DSF_PROXY_PASSWORD if not set", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*")
	@Value("${org.eyematics.provide.fhir.server.oauth2.proxy.password:#{null}}")
	private String fhirStoreOAuth2ProxyPassword;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "If set to true, OIDC validation will only log a warning and not throw an illegal state exception")
	@Value("${org.eyematics.provide.fhir.server.oauth2.discovery.validation.lenient:false}")
	private boolean fhirStoreOAuth2DiscoveryValidationLenient;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "To enable debug logging of FHIR resources set to `true`")
	@Value("${org.eyematics.provide.fhir.dataLoggingEnabled:false}")
	private boolean fhirDataLoggingEnabled;

	@ProcessDocumentation(processNames = {
			"eyematicsorg_provideProcess" }, description = "To enable an additional connection test on startup of the client reading Binary resources as stream, set to `true`")
	@Value("${org.eyematics.provide.fhir.server.binary.stream.client.connection.test.enabled:false}")
	private boolean fhirProvideClientConnectionTestEnabled;

	@Value("${org.eyematics.provide.fhir.server.organization.identifier.value}")
	private String localIdentifierValue;

	public FhirClientFactory getFhirClientFactory() {
		return new FhirClientFactoryProviderImpl(fhirContext, api, fhirStoreOAuth2TrustStore, fhirStoreOAuth2ProxyUrl, fhirStoreOAuth2ProxyUsername, fhirStoreOAuth2ProxyPassword, fhirStoreOAuth2IssuerUrl,
				fhirStoreOAuth2DiscoveryPath, fhirStoreOAuth2ClientId, fhirStoreOAuth2ClientSecret, fhirStoreOAuth2ConnectTimeout,fhirStoreOAuth2SocketTimeout, fhirStoreOAuth2DiscoveryValidationLenient,
				fhirDataLoggingEnabled, fhirStoreTrustStore, fhirStoreCertificate, fhirStorePrivateKey, fhirStoreProxyUrl, fhirStoreProxyUsername, fhirStoreProxyPassword, fhirStorePrivateKeyPassword,
				fhirStoreConnectTimeout, fhirStoreSocketTimeout, fhirStoreConnectionRequestTimeout, fhirStoreBaseUrl, fhirStoreUsername,fhirStorePassword, fhirStoreBearerToken, fhirStoreHapiClientVerbose,
				localIdentifierValue, fhirProvideClientConnectionTestEnabled).create();
	}

}
