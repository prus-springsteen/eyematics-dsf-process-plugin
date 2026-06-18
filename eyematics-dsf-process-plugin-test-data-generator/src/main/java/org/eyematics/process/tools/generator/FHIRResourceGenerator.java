package org.eyematics.process.tools.generator;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.text.StringSubstitutor;
import org.eyematics.process.tools.generator.resourcebuilder.BundleResource;
import org.eyematics.process.tools.generator.resourcebuilder.MeasureReportResource;
import org.eyematics.process.tools.generator.resourcebuilder.MedicationResource;
import org.eyematics.process.tools.generator.resourcebuilder.PatientResource;
import org.eyematics.process.tools.generator.util.BundleCompressor;
import org.eyematics.process.tools.generator.util.FHIRServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FHIRResourceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(FHIRResourceGenerator.class);
    private static final FhirContext FHIRCONTEXT = FhirContext.forR4();
    private static final List<FHIRServer> DIC = List.of(new FHIRServer("dic-a", "8070", "8071"),
                                                        new FHIRServer("dic-b", "8072", "8073"),
                                                        new FHIRServer("dic-c", "8074", "8075"),
                                                        new FHIRServer("dic-d", "8076", "8077"));
    private static final int AMOUNT_PATIENT_BUNDLES = 10;
    private static final int AMOUNT_VALID_PATIENT_PER_BUNDLE = 10;
    private static final int AMOUNT_INVALID_PATIENT_PER_BUNDLE = 10;
    private static final int AMOUNT_MEASURE_REPORTS = 3;
    private static final int AMOUNT_SHARED_MEDICATIONS = 5;
    private static final boolean READABLE = false;
    private static final Path dicFolder = Paths.get("../eyematics-dsf-process-plugin-test-setup");
    private static final String compressedFile = "eyematics_test_data.tar";
    private static final String resourceFile = compressedFile + ".gz";
    private static final Path scriptTemplateFile = Paths.get("./src/main/resources/bash-template/fhir_server_bash_template.sh");
    private static final String scriptName = "fhir_server_init.sh";

    public void generateFHIRResources()  {
        Date startDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.now().plusYears(5).atZone(ZoneId.systemDefault()).toInstant());
        logger.info("Creating one shared Patient...");
        Patient sharedPatient = new PatientResource()
                .randomize()
                .setSource("shared")
                .build();
        logger.info("DONE");
        logger.info("Creating shared Medications...");
        List<Medication> medicationList = new ArrayList<>();
        MedicationResource mr = new MedicationResource();
        for (int i = 0; i < AMOUNT_SHARED_MEDICATIONS; i++) {
            medicationList.add(mr.randomize().build());
        }
        logger.info("DONE");
        for (FHIRServer d : DIC) {
            logger.info("Creating and Preparing Bundle for " + d.getDic() + "...");
            boolean areAdditionalAdded = false;
            Bundle dicBundle = new BundleResource()
                    .setBundleType(Bundle.BundleType.TRANSACTION)
                    .setHttpVerb(Bundle.HTTPVerb.PUT)
                    .build();
            Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
            brc.setMethod(Bundle.HTTPVerb.PUT);
            logger.info("DONE");
            int bundleCounter = 1;
            for (int j = 0; j < AMOUNT_PATIENT_BUNDLES; j++) {
                dicBundle.getEntry().clear();
                if (!areAdditionalAdded) {
                    logger.info("Adding shared Medications into Bundle for " + d.getDic() + "...");
                    medicationList.forEach(m -> dicBundle.addEntry()
                            .setResource(m)
                            .setRequest(brc.copy().setUrl(m.getResourceType().name() + "/" + m.getId()))
                            .setFullUrl(m.getResourceType().name() + "/" + m.getId())
                    );
                    logger.info("DONE");
                    logger.info("Adding shared Patient and MDAT into Bundle for " + d.getDic() + "...");
                    Bundle sharedPatientResources = new BundleResource().randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPatient(sharedPatient)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(5)
                            .setAmountInvalidConsent(5)
                            .build();
                    sharedPatientResources.getEntry().forEach(dicBundle::addEntry);
                    logger.info("DONE");
                    logger.info("Creating MeasureReports for " + d.getDic() + "...");
                    int count = 0;
                    for (int i = 0; i < AMOUNT_MEASURE_REPORTS; i++) {
                        count = ThreadLocalRandom.current().nextInt(count, 1000);
                        MeasureReport measureReport = new MeasureReportResource()
                                .randomize()
                                .setPeriod(startDate, endDate)
                                .setCount(count)
                                .build();
                        String measureReportName = measureReport.getResourceType().name() + "/" + measureReport.getId();
                        dicBundle.addEntry()
                                .setResource(measureReport)
                                .setRequest(brc.copy().setUrl(measureReportName))
                                .setFullUrl(measureReportName);
                    }
                    logger.info("DONE");
                    areAdditionalAdded = true;
                }
                logger.info("Creating patients and mdat for " + d.getDic() + "...");
                for (int i = 0; i < AMOUNT_VALID_PATIENT_PER_BUNDLE; i++) {
                    BundleResource randomValidPatientBundle = new BundleResource();
                    Bundle ramdomValidPatient = randomValidPatientBundle.randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(5)
                            .setAmountInvalidConsent(5)
                            .setSource(d.getDic().toLowerCase())
                            .build();
                    ramdomValidPatient.getEntry().forEach(dicBundle::addEntry);
                }
                for (int i = 0; i < AMOUNT_INVALID_PATIENT_PER_BUNDLE; i++) {
                    BundleResource randomInvalidPatientBundle = new BundleResource();
                    Bundle ramdomInvalidPatient = randomInvalidPatientBundle.randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(0)
                            .setAmountInvalidConsent(5)
                            .setSource(d.getDic().toLowerCase())
                            .build();
                    ramdomInvalidPatient.getEntry().forEach(dicBundle::addEntry);
                }
                logger.info("DONE");
                logger.info("Writing Bundle #" + bundleCounter +" for " + d.getDic() + "...");
                try {
                    this.saveBundle(dicBundle,
                            this.getFileNumber(bundleCounter) + "_eyematics_test_data_bundle_" + d.getDic() + ".json",
                            d.getDic(),
                            d.getLocalPort());
                } catch (Exception e) {
                    logger.error("Generation of FHIR resources failed for {}: {}", d.getDic(), e.getMessage());
                }
                bundleCounter++;
                logger.info("DONE");
            }
            BundleCompressor.compress(dicFolder + "/" + d.getDic() + "/repository", compressedFile);
        }
    }

    private void saveBundle(Bundle bundle, String jsonFileName, String dic, String port) throws FileNotFoundException {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle must not be null.");
        }

        Path bundlePath = dicFolder.resolve(dic).resolve("repository");
        try {
            Files.createDirectories(bundlePath.toAbsolutePath());
        } catch (Exception e) {
            logger.info("Folder for FHIR resources already existing.");
        }

        bundlePath = bundlePath.resolve(jsonFileName);
        logger.info("Writing FHIR resources to {}", bundlePath.toAbsolutePath());

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
            String bundleJSON = FHIRCONTEXT.newJsonParser()
                    .setPrettyPrint(READABLE)
                    .encodeResourceToString(bundle);
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

        Map<String, String> fhirServer = Map.of("dic", dic, "port", port, "resource", resourceFile);
        StringSubstitutor SubStr = new StringSubstitutor(fhirServer);
        try (FileOutputStream os = new FileOutputStream(path.toFile())) {
            String bashScript = SubStr.replace(bashScriptTemplate).replaceAll("\r\n", "\n");;
            os.write(bashScript.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getFileNumber(int number) {
        StringBuilder numberString = new StringBuilder();
        int maxPlaces = String.valueOf(AMOUNT_PATIENT_BUNDLES).length();
        int places = maxPlaces - String.valueOf(number).length();
        numberString.append("0".repeat(Math.max(0, places)));
        return numberString.toString() + number;
    }

}
