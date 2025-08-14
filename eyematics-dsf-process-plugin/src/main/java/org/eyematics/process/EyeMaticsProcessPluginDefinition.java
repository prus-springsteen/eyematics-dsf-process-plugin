package org.eyematics.process;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import dev.dsf.bpe.v1.ProcessPluginDefinition;
import org.eyematics.process.spring.config.CryptoConfig;
import org.eyematics.process.spring.config.EyeMaticsConfig;
import org.eyematics.process.spring.config.initiate.InitiateConfig;
import org.eyematics.process.spring.config.provide.ProvideFhirClientConfig;
import org.eyematics.process.spring.config.provide.ProvideConfig;
import org.eyematics.process.spring.config.receive.ReceiveConfig;

import static org.eyematics.process.constant.InitiateConstants.PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS;
import static org.eyematics.process.constant.ProvideConstants.PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS;
import static org.eyematics.process.constant.ReceiveConstants.PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS;

public class EyeMaticsProcessPluginDefinition implements ProcessPluginDefinition {

	public static final String VERSION = "1.0.0.1";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2025, 8, 15);

	@Override
	public String getName() {
		return "eyematics-dsf-process-plugin";
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public LocalDate getReleaseDate() {
		return RELEASE_DATE;
	}

	@Override
	public List<String> getProcessModels() {
		return List.of("bpe/eyematics-initiate-process.bpmn", "bpe/eyematics-provide-process.bpmn",
				"bpe/eyematics-receive-process.bpmn");
	}

	@Override
	public Map<String, List<String>> getFhirResourcesByProcessId() {

		// EYEMATICS PROCESS (v1.0)

		// GENERIC
		String cGenericProcess = "fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system.xml";
		String cGenericProcessCodes = "fhir/CodeSystem/eyematics-generic-process-data-set-status-codes-code-system.xml";
		String sGenericProcess = "fhir/StructureDefinition/eyematics-generic-process-data-set-status-error-extension.xml";
		String vGenericProcess = "fhir/ValueSet/eyematics-generic-process-data-set-status-value-set.xml";
		String vGenericProcessCodes = "fhir/ValueSet/eyematics-generic-process-data-set-status-codes-value-set.xml";

		// INITIATE
		String aInitiateProcess = "fhir/ActivityDefinition/eyematics-initiate-process-activity-definition.xml";
		String sInitiateProcess = "fhir/StructureDefinition/eyematics-initiate-process-structure-definition.xml";
		String tInitiateProcess = "fhir/Task/eyematics-initiate-process-task.xml";

		// PROVIDE
		String aProvideProcess = "fhir/ActivityDefinition/eyematics-provide-process-activity-definition.xml";
		String sProvideProcess = "fhir/StructureDefinition/eyematics-provide-process-structure-definition.xml";
		String sProvideProcessReceiveReceipt = "fhir/StructureDefinition/eyematics-provide-process-receive-receipt-structure-definition.xml";

		// RECEIVE
		String aReceiveProcess = "fhir/ActivityDefinition/eyematics-receive-process-activity-definition.xml";
		String sInitiateReceiveProcess = "fhir/StructureDefinition/eyematics-receive-process-initiate-structure-defintion.xml";
		String sInitiateReceiveProcessExtension = "fhir/StructureDefinition/eyematics-receive-process-initiate-dic-identifier-extension.xml";
		String sStartReceiveProcess = "fhir/StructureDefinition/eyematics-receive-process-structure-definition.xml";
		String cReceiveInitiateProcess = "fhir/CodeSystem/eyematics-receive-process-initiate-code-system.xml";
		String cReceiveProcess = "fhir/CodeSystem/eyematics-receive-process-code-system.xml";
		String vReceiveInitiateProcess = "fhir/ValueSet/eyematics-receive-process-initiate-value-set.xml";
		String vReceiveProcess = "fhir/ValueSet/eyematics-receive-process-value-set.xml";

		return Map.of(PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS,
				List.of(aInitiateProcess, sInitiateProcess, tInitiateProcess),
				      PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS,
				List.of(aProvideProcess, sProvideProcess, sProvideProcessReceiveReceipt, sGenericProcess, cGenericProcess, cGenericProcessCodes, vGenericProcess, vGenericProcessCodes),
					  PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS,
				List.of(aReceiveProcess, sInitiateReceiveProcess, sInitiateReceiveProcessExtension, sStartReceiveProcess, cReceiveProcess, cReceiveInitiateProcess, vReceiveProcess, vReceiveInitiateProcess, sGenericProcess, cGenericProcess, cGenericProcessCodes, vGenericProcess, vGenericProcessCodes));
	}

	@Override
	public List<Class<?>> getSpringConfigurations() {
		return List.of(EyeMaticsConfig.class, CryptoConfig.class, ProvideFhirClientConfig.class, InitiateConfig.class,
				ProvideConfig.class, ReceiveConfig.class);
	}
}
