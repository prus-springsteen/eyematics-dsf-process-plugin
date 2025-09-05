package org.eyematics.process.utils.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.eyematics.process.utils.client.logging.DataLogger;
import org.eyematics.process.utils.client.token.TokenProvider;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.util.Map;
import java.util.Optional;

public class EyeMaticsFhirClientImpl extends AbstractHttpFhirClient implements EyeMaticsFhirClient {

    private static final Logger logger = LoggerFactory.getLogger(EyeMaticsFhirClientImpl.class);

    public EyeMaticsFhirClientImpl(KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword, int connectTimeout, int socketTimeout, String fhirServerBasicAuthUsername, String fhirServerBasicAuthPassword, String fhirServerBearerToken, TokenProvider fhirServerOAuth2TokenProvider, String fhirServerBase, String proxyUrl, String proxyUsername, String proxyPassword, FhirContext fhirContext, String localIdentifierValue, DataLogger dataLogger) {
        super(trustStore, keyStore, keyStorePassword, connectTimeout, socketTimeout, fhirServerBasicAuthUsername, fhirServerBasicAuthPassword, fhirServerBearerToken, fhirServerOAuth2TokenProvider, fhirServerBase, proxyUrl, proxyUsername, proxyPassword, fhirContext, localIdentifierValue, dataLogger);
    }

    @Override
    public String read(IdType idType, String mimeType) throws Exception {
        String url = idType.toUnqualified().getValue();
        try {
            HttpClient client = this.createClient();
            HttpRequest request = this.createBaseRequest(url, Map.of("Accept", mimeType)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return response.body();
            } else {
                throw new Exception("Reading  from '" + url + "' failed - status code: " + response.statusCode());
            }
        } catch (Exception exception) {
            throw new Exception("Reading from '" + url + "' failed - " + exception.getMessage(), exception);
        }
    }

    @Override
    public MethodOutcome create(IdType idType, String fhirResource, String mimeType) throws Exception {
        String url = idType.toUnqualified().getValue();
        HttpClient client = this.createClient();
        HttpRequest request = this.createBaseRequest(url, Map.of("Content-Type", mimeType))
                                  .POST(HttpRequest.BodyPublishers.ofString(fhirResource))
                                  .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
                Optional<String> location = response.headers().firstValue("Location");
                return new MethodOutcome(new IdType(location.orElseThrow(() -> new Exception("FHIR-Resource could be created, the location has not been provided."))), true);
            }
            throw new Exception("Creating FHIR-Resource failed - " + response.body() + "(Status Code: " + response.statusCode() + ")");
        } catch (Exception exception) {
            throw new Exception("Saving FHIR-Resource failed", exception);
        }
    }
}

