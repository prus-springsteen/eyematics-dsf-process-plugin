package org.eyematics.process.utils.client;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;


public class FTTPClientImpl implements FTTPClient {
    private static final Logger logger = LoggerFactory.getLogger(FTTPClientImpl.class);
    private final FhirContext fhirContext = FhirContext.forR4();
    private final String fttpServerBase;
    private final SSLContext sslContext;
    private final String fttpBasicAuthUsername;
    private final String fttpBasicAuthPassword;
    private final String fttpStudy;
    private final String fttpTarget;
    private final String fttpApiKey;
    private final int fttpConnectTimeout;

    public FTTPClientImpl(String fttpServerBase, SSLContext sslContext, String fttpBasicAuthUsername,
                          String fttpBasicAuthPassword, String fttpStudy, String fttpTarget, String fttpApiKey,
                          int fttpConnectTimeout) {
        this.fttpServerBase = fttpServerBase;
        this.sslContext = sslContext;
        this.fttpBasicAuthUsername = fttpBasicAuthUsername;
        this.fttpBasicAuthPassword = fttpBasicAuthPassword;
        this.fttpStudy = fttpStudy;
        this.fttpTarget = fttpTarget;
        this.fttpApiKey = fttpApiKey;
        this.fttpConnectTimeout = fttpConnectTimeout;
    }

    @Override
    public Optional<HashMap<String, String>> getGlobalPseudonym(HashSet<String> patientBloomFilter) throws Exception {
        try {
            Parameters parameters = this.createParametersForBfWorkflow();
            patientBloomFilter.forEach(pbf -> parameters.addParameter("bloomfilter", new Base64BinaryType(pbf)));
            HttpResponse<String> response = this.createPOSTRequest(parameters).orElse(null);
            Optional<Parameters> optParameters = this.processResponse(response);
            if (optParameters.isPresent()) {
                HashMap<String, String> globalPseudonyms = new HashMap<>();
                for (Parameters.ParametersParameterComponent pc : optParameters.get().getParameter()) {
                    String bf = null;
                    String psn = null;
                    for (Parameters.ParametersParameterComponent pcc : pc.getPart()) {
                        if (pcc.getName().equals("bloomfilter") && pcc.getValue() instanceof Base64BinaryType bbt) {
                            bf = bbt.getValueAsString();
                        }
                        if (pcc.getName().equals("pseudonym") && pcc.getValue() instanceof Identifier id
                                && id.getSystem().equals("https://ths-greifswald.de/gpas")) {
                            psn = id.getValue();
                        }
                    }
                    if (bf != null && psn != null) {
                        globalPseudonyms.put(bf, psn);
                    }
                }
                return Optional.of(globalPseudonyms);
            }
        } catch (Exception e) {
            logger.error("Error while getting global pseudonyms: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getGlobalPseudonym(String patientBloomFilter) throws Exception {
        try {
            Parameters parameters = this.createParametersForBfWorkflow();
            parameters.addParameter("bloomfilter", new Base64BinaryType(patientBloomFilter));
            HttpResponse<String> response = this.createPOSTRequest(parameters).orElse(null);
            Optional<Parameters> optParameters = this.processResponse(response);
            if (optParameters.isPresent()) {
                for (Parameters.ParametersParameterComponent pc : optParameters.get().getParameterFirstRep().getPart()) {
                    if (pc.getName().equals("pseudonym") && pc.getValue() instanceof Identifier id
                            && id.getSystem().equals("https://ths-greifswald.de/gpas")) {
                        return Optional.of(id.getValue());
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error while getting global pseudonym: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private Parameters createParametersForBfWorkflow() {
        Parameters p = new Parameters();
        p.addParameter("study", fttpStudy);
        p.addParameter("target", fttpTarget);
        p.addParameter("apikey", fttpApiKey);
        return p;
    }

    private HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .sslContext(this.sslContext)
                .connectTimeout(Duration.ofSeconds(this.fttpConnectTimeout))
                .build();
    }

    private Optional<HttpResponse<String>> createPOSTRequest(Parameters parameters) throws Exception {
        String url = this.fttpServerBase + "/ttp-fhir/fhir/dispatcher/$requestPsnFromBfWorkflow";
        String basicAuth = Base64.getEncoder()
                .encodeToString((this.fttpBasicAuthUsername + ":" + this.fttpBasicAuthPassword)
                        .getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + basicAuth;
        String body = this.fhirContext.newXmlParser().encodeResourceToString(parameters);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/fhir+xml")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            return Optional.of(this.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            logger.error("Error while sending request to FTTP server: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private Optional<Parameters> processResponse(HttpResponse<String> response) throws Exception {
        if (response == null || response.statusCode() != 200) {
            logger.error("Error while getting adequate response from FTTP server: {}", response);
            throw new Exception("Response: " + response);
        }
        try {
            Parameters parameters = this.fhirContext.newXmlParser().parseResource(Parameters.class, response.body());
            return Optional.of(parameters);
        } catch (Exception e) {
            logger.error("Error while parsing response from FTTP server: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
