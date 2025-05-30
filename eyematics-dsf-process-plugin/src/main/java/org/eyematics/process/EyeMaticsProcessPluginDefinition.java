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
	public static final String VERSION = "2.1.0.1";
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
		String sGenericProcess = "fhir/StructureDefinition/eyematics-generic-data-set-status-error-extension.xml";
		String vGenericProcess = "fhir/ValueSet/eyematics-generic-data-set-status-value-set.xml";

		// INITIATE
		String aInitiateProcess = "fhir/ActivityDefinition/eyematics-initiate-process-activity-definition.xml";
		String sInitiateProcess = "fhir/StructureDefinition/eyematics-initiate-process-structure-definition.xml";
		String tInitiateProcess = "fhir/Task/eyematics-initiate-process-task.xml";

		// PROVIDE
		String aProvideProcess = "fhir/ActivityDefinition/eyematics-provide-process-activity-definition.xml";
		String sProvideProcess = "fhir/StructureDefinition/eyematics-provide-process-structure-definition.xml";
		String sProvideProcessReceiveReceipt = "fhir/StructureDefinition/eyematics-provide-process-receive-receipt-structure-definition.xml";
		String tProvideProcess = "fhir/Task/eyematics-provide-process-task.xml";

		// RECEIVE
		String aReceiveProcess = "fhir/ActivityDefinition/eyematics-receive-process-activity-definition.xml";
		String sReceiveProcess = "fhir/StructureDefinition/eyematics-receive-process-structure-definition.xml";
		String cReceiveProcess = "fhir/CodeSystem/eyematics-receive-process-code-system.xml";
		String vReceiveProcess = "fhir/ValueSet/eyematics-receive-process-value-set.xml";

		return Map.of(PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS,
				List.of(aInitiateProcess, sInitiateProcess, cGenericProcess, vGenericProcess, tInitiateProcess),
				      PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS,
				List.of(aProvideProcess, sProvideProcess, sProvideProcessReceiveReceipt, sGenericProcess, cGenericProcess, vGenericProcess),
					  PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS,
				List.of(aReceiveProcess, sReceiveProcess, cReceiveProcess, vReceiveProcess, cGenericProcess, vGenericProcess));
	}

	@Override
	public List<Class<?>> getSpringConfigurations() {
		return List.of(EyeMaticsConfig.class, CryptoConfig.class, ProvideFhirClientConfig.class, InitiateConfig.class,
				ProvideConfig.class, ReceiveConfig.class);
	}
}
