package org.eyematics.process.service.provide;

import java.io.*;
import java.util.Objects;
import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.client.BasicFhirWebserviceClient;
import jakarta.ws.rs.core.MediaType;
import org.eyematics.process.constant.EyeMaticsConstants;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.utils.client.BinaryStreamFhirClient;
import org.eyematics.process.utils.client.FhirClient;
import org.eyematics.process.utils.client.FhirClientFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;


// https://github.com/medizininformatik-initiative/dsf-plugin-numdashboard/blob/main/src/main/java/de/medizininformatik_initiative/process/report/service/CreateJson.java
// https://www.baeldung.com/java-9-http-client

// Download with Pause and Resume...
// https://www.baeldung.com/spring-resttemplate-download-large-file
public class ReadProvideDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ReadProvideDataTask.class);
    private final FhirClientFactory fhirClientFactory;

    public ReadProvideDataTask(ProcessPluginApi api, FhirClientFactory fhirClientFactory) {
        super(api);
        this.fhirClientFactory = fhirClientFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Objects.requireNonNull(this.fhirClientFactory, "fhirClientFactory");
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
        try (InputStream in = con.getInputStream()) {
            logger.info("Stream -> {}", in); // sun.net.www.protocol.http.HttpURLConnection$HttpInputStream@3ac8963d
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, in);
            logger.info("Data -> {}", bundle);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
        } catch (Exception e) {
            // go handle the exception
        }  finally {
            con.disconnect();
        }


        /*
        // Klappt ...
        // https://github.com/medizininformatik-initiative/mii-processes-common/tree/develop/src/main/java/de/medizininformatik_initiative/processes/common/fhir/client
        // https://github.com/medizininformatik-initiative/mii-process-data-transfer/blob/issues/36_multiple_attachments/src/main/java/de/medizininformatik_initiative/process/data_transfer/service/EncryptAndStoreData.java#L352
        // TimeUnit.SECONDS.sleep((int)(Math.random() * ((20 - 5) + 1)));
        BinaryStreamFhirClient fhirClient = fhirClientFactory.getBinaryStreamFhirClient();
        IdType idType = new IdType("https://blaze-dev.ukmuenster.de/fhir", "Bundle", "StructureDefinition", "");
        try (InputStream  in = fhirClient.readBundle(idType, "application/fhir+xml")) {
            logger.info("Stream -> {}", in); // sun.net.www.protocol.http.HttpURLConnection$HttpInputStream@3ac8963d
            FhirContext fhirContext = FhirContext.forR4();
            Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, in); // jdk.internal.net.http.ResponseSubscribers$HttpResponseInputStream@9082505
            logger.info("Data -> {}", bundle);
            variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, bundle);
        } catch (Exception e) {
            logger.info("Exception -> {}", e.getMessage());
        }
        */


    }
}
