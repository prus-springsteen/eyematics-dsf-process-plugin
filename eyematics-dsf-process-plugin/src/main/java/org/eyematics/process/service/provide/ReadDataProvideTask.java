package org.eyematics.process.service.provide;

import java.io.*;
import java.util.Objects;
import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.core.MediaType;
import org.eyematics.process.constant.EyeMaticsConstants;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.fhir.client.FhirClient;
import org.eyematics.process.utils.fhir.client.FhirClientFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.IdType;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;


// https://github.com/medizininformatik-initiative/dsf-plugin-numdashboard/blob/main/src/main/java/de/medizininformatik_initiative/process/report/service/CreateJson.java
// https://www.baeldung.com/java-9-http-client

// Download with Pause and Resume...
// https://www.baeldung.com/spring-resttemplate-download-large-file
public class ReadDataProvideTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadDataProvideTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadDataProvideTask(ProcessPluginApi api, FhirClientFactory fhirClientFactory) {
        super(api);
        this.fhirClientFactory = fhirClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(fhirClientFactory, "fhirClientFactory");
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("Reading Data from FHIR-Repository...");

        // Klappt am besten
        // Vorteil: 1.) Einfache Handhabung 2.) Funktioniert 3.) InputStream etc....
        URL url = new URL("https://blaze-dev.ukmuenster.de/fhir/StructureDefinition");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // String auth = username + ":" + password;
        // String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        // String authHeaderValue = "Basic " + encodedAuth;
        // con.setRequestProperty("Authorization", authHeaderValue);

        int status = con.getResponseCode();
        logger.info("Response Code -> {}", status);
        // if (status == HttpURLConnection.HTTP_OK)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, in);
            logger.info("Data -> {}", bundle);
            variables.setResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
        } catch (Exception e) {
            // go handle the exception
        }  finally {
            con.disconnect();
        }


        /*
         // Klappt nicht wegen ???...
        TimeUnit.SECONDS.sleep((int)(Math.random() * ((20 - 5) + 1)));
        FhirClient fhirClient = fhirClientFactory.getFhirClient();
        IdType idType = new IdType(fhirClientFactory.getFhirClient().getFhirBaseUrl(), null, "StructureDefinition", "");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fhirClient.readBinary(idType).getContent());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = new Bundle();// fhirContext.newJsonParser().parseResource(Bundle.class, in);
            logger.info("Data -> {}", in);
            variables.setResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
        } catch (Exception e) {
            // go handle the exception
        }  finally {

        }
        */

        /*
        // Klappt nicht wegen SSL...
        BasicFhirWebserviceClient client = api.getFhirWebserviceClientProvider().getWebserviceClient("https://blaze-dev.ukmuenster.de/").withRetry(6, 5);
        InputStream inputStream = client.readBinary("StructureDefinition", MediaType.APPLICATION_XML_TYPE);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, in);
            logger.info("Data -> {}", bundle);
            variables.setResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
        } catch (Exception e) {
            // go handle the exception
        }  finally {

        }

         */




        /*
        // Klappt nicht wegen Konvertierung ...
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<InputStream> response = restTemplate.exchange(
                "https://blaze-dev.ukmuenster.de/fhir/StructureDefinition",
                HttpMethod.GET,
                entity,
                InputStream .class
        );

        InputStream inputStream = response.getBody();

        try {

            assert inputStream != null;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                FhirContext fhirContext = FhirContext.forR4();
                Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, in);
                logger.info("Data -> {}", bundle);
                variables.setResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
