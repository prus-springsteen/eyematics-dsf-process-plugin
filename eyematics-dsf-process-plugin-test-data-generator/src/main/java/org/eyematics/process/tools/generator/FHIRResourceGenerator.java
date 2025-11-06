package org.eyematics.process.tools.generator;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.text.StringSubstitutor;
import org.eyematics.process.tools.generator.resourcebuilder.BundleResource;
import org.eyematics.process.tools.generator.util.FHIRServer;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class FHIRResourceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(FHIRResourceGenerator.class);
    private static final List<FHIRServer> DIC = List.of(new FHIRServer("dic-a", "8070", "8071"),
                                                        new FHIRServer("dic-b", "8072", "8073"),
                                                        new FHIRServer("dic-c", "8074", "8075"),
                                                        new FHIRServer("dic-d", "8076", "8077"));
    private static final Path dicFolder = Paths.get("../eyematics-dsf-process-plugin-test-setup");
    private static final String resourceJSON = "eyematics_cds.json";
    private static final Path scriptTemplateFile = Paths.get("./src/main/resources/bash-template/fhir_server_bash_template.sh");
    private static final String scriptName = "fhir_server_init.sh";

    public void generateFHIRResources()  {
        for (int i = 0; i < DIC.size(); i++) {
            FHIRServer fhirServer = DIC.get(i);
            Bundle b = new BundleResource().randomize()
                    .setBundleType(Bundle.BundleType.TRANSACTION)
                    .setHttpVerb(Bundle.HTTPVerb.PUT)
                    .setPatientId(i + 1)
                    .setSource(fhirServer.getDic())
                    .build();
            try {
                this.saveBundle(b, fhirServer.getDic(), fhirServer.getLocalPort());
            } catch (Exception e) {
                logger.error("Generation of FHIR resources failed for {}: {}", fhirServer.getDic(), e.getMessage());
            }

        }
    }

    private void saveBundle(Bundle bundle, String dic, String port) throws FileNotFoundException {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle must not be null.");
        }

        Path bundlePath = dicFolder.resolve(dic).resolve("repository");
        try {
            Files.createDirectories(bundlePath.toAbsolutePath());
        } catch (Exception e) {
            logger.info("Folder for FHIR resources already existing.");
        }

        bundlePath = bundlePath.resolve(resourceJSON);

        if (!Files.exists(bundlePath)) {
            this.writeFHIRResourceFile(bundle, bundlePath);
        }

        Path scriptFile = dicFolder.resolve(dic).resolve("repository").resolve(scriptName);
        if (!Files.exists(scriptFile)) {
            this.createBashScript(scriptFile, dic, port);
        }
    }

    private void writeFHIRResourceFile(Bundle bundle, Path path) {
        try (FileOutputStream os = new FileOutputStream(path.toFile())) {
            String bundleJSON = FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
            os.write(bundleJSON.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void createBashScript(Path path, String dic, String port) {
        StringBuilder bashScriptTemplate = new StringBuilder();
        try (FileInputStream os = new FileInputStream(scriptTemplateFile.toFile())) {
            int i = 0;
            while((i = os.read()) !=-1 )  bashScriptTemplate.append((char)i);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        Map<String, String> fhirServer = Map.of("dic", dic, "port", port, "resource", resourceJSON);
        StringSubstitutor SubStr = new StringSubstitutor(fhirServer);
        try (FileOutputStream os = new FileOutputStream(path.toFile())) {
            String bashScript = SubStr.replace(bashScriptTemplate).replaceAll("\r\n", "\n");;
            os.write(bashScript.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
