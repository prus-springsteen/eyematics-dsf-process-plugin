package org.eyematics.process.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import dev.dsf.fhir.service.ReferenceCleaner;
import dev.dsf.fhir.service.ReferenceCleanerImpl;
import dev.dsf.fhir.service.ReferenceExtractor;
import dev.dsf.fhir.service.ReferenceExtractorImpl;
import org.eyematics.process.tools.generator.CertificateGenerator.CertificateFiles;

public class BundleGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(BundleGenerator.class);

	private final FhirContext fhirContext = FhirContext.forR4();
	private final ReferenceExtractor extractor = new ReferenceExtractorImpl();
	private final ReferenceCleaner cleaner = new ReferenceCleanerImpl(extractor);

	private Bundle bundle;

	private Bundle readAndCleanBundle(Path bundleTemplateFile)
	{
		try (InputStream in = Files.newInputStream(bundleTemplateFile))
		{
			Bundle bundle = newXmlParser().parseResource(Bundle.class, in);

			// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
			return cleaner.cleanReferenceResourcesIfBundle(bundle);
		}
		catch (IOException e)
		{
			logger.error("Error while reading bundle from " + bundleTemplateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private void writeBundle(Path bundleFile, Bundle bundle)
	{
		try (OutputStream out = Files.newOutputStream(bundleFile);
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
		catch (IOException e)
		{
			logger.error("Error while writing bundle to " + bundleFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private IParser newXmlParser()
	{
		IParser parser = fhirContext.newXmlParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	public void createDockerTestBundles(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		createDockerTestBundle(clientCertificateFilesByCommonName);
	}

	private void createDockerTestBundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Path bundleTemplateFile = Paths.get("src/main/resources/bundle-templates/bundle.xml");

		bundle = readAndCleanBundle(bundleTemplateFile);

		Organization organizationDicB = (Organization) bundle.getEntry().get(0).getResource();
		Extension organizationDicBThumbprintExtension = organizationDicB
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDicBThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic-b-client").getCertificateSha512ThumbprintHex()));

		Organization organizationDicA = (Organization) bundle.getEntry().get(1).getResource();
		Extension organizationDicAThumbprintExtension = organizationDicA
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDicAThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic-a-client").getCertificateSha512ThumbprintHex()));

		Organization organizationDicC = (Organization) bundle.getEntry().get(2).getResource();
		Extension organizationDicCThumbprintExtension = organizationDicC
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDicCThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic-c-client").getCertificateSha512ThumbprintHex()));


		Organization organizationDicD = (Organization) bundle.getEntry().get(3).getResource();
		Extension organizationDicDThumbprintExtension = organizationDicD
				.getExtensionByUrl("http://dsf.dev/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationDicDThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("dic-d-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle/bundle.xml"), bundle);
	}

	public void copyDockerTestBundles()
	{
		Path dicBBundleFile = Paths.get("../eyematics-dsf-process-plugin-test-setup/dic-b/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dicBBundleFile);
		writeBundle(dicBBundleFile, bundle);

		Path dicABundleFile = Paths.get("../eyematics-dsf-process-plugin-test-setup/dic-a/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dicABundleFile);
		writeBundle(dicABundleFile, bundle);

		Path dicCBundleFile = Paths.get("../eyematics-dsf-process-plugin-test-setup/dic-c/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dicCBundleFile);
		writeBundle(dicCBundleFile, bundle);

		Path dicDBundleFile = Paths.get("../eyematics-dsf-process-plugin-test-setup/dic-d/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dicDBundleFile);
		writeBundle(dicDBundleFile, bundle);
	}
}
