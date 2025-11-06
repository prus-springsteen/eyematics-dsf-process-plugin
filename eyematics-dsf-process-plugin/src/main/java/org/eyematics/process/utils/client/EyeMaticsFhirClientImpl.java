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
    public String read(String url, String mimeType) throws Exception {
        return readImplementation(url, mimeType);
    }

    @Override
    public String read(IdType idType, String mimeType) throws Exception {
        String url = idType.toUnqualified().getValue();
        return readImplementation(url, mimeType);
    }

    @Override
    public String read(String resourceType, String searchQuery, String mimeType) throws Exception {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return readImplementation(resourceType, mimeType);
        }
        String url = resourceType + "?" + searchQuery;
        return readImplementation(url, mimeType);
    }

    private String readImplementation(String url, String mimeType) throws Exception {
        try {
            HttpClient client = this.createClient();
            HttpRequest request = this.createBaseRequest(url, Map.of("Accept", mimeType))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == HttpURLConnection.HTTP_OK) {
                return response.body();
            }
            throw new Exception("Reading from '" + url + "' failed - status code: " + status);
        } catch (Exception e) {
            throw new Exception("Reading from '" + url + "' failed - " + e.getMessage(), e);
        }
    }

    @Override
    public String create(String fhirResource, String mimeType) throws Exception {
        return createImplementation(null, fhirResource, mimeType);
    }

    @Override
    public String create(String resourceType, String fhirResource, String mimeType) throws Exception {
        return createImplementation(resourceType, fhirResource, mimeType);
    }

    private String createImplementation(String resourceType, String fhirResource, String mimeType) throws Exception {
        try {
            HttpClient client = this.createClient();
            HttpRequest.Builder reqBuilder = (resourceType == null)
                    ? this.createBaseRequest(Map.of("Content-Type", mimeType))
                    : this.createBaseRequest(resourceType, Map.of("Content-Type", mimeType));

            HttpRequest request = reqBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(fhirResource))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == HttpURLConnection.HTTP_CREATED || status == HttpURLConnection.HTTP_OK) {
                return response.body();
            }
            throw new Exception("Creating FHIR-Resource failed - " + response.body() + " (Status Code: " + status + ")");
        } catch (Exception e) {
            throw new Exception("Saving FHIR-Resource failed", e);
        }
    }
}

