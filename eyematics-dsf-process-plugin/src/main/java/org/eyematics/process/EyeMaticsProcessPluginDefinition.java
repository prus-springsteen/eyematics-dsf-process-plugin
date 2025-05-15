package org.eyematics.process;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import dev.dsf.bpe.v1.ProcessPluginDefinition;
import org.eyematics.process.spring.config.CryptoConfig;
import org.eyematics.process.spring.config.initiate.InitiateConfig;
import org.eyematics.process.spring.config.provide.DicFhirClientConfig;
import org.eyematics.process.spring.config.provide.ProvideConfig;
import org.eyematics.process.spring.config.receive.ReceiveConfig;

import static org.eyematics.process.constant.InitiateConstants.PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS;
import static org.eyematics.process.constant.ProvideConstants.PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS;

public class EyeMaticsProcessPluginDefinition implements ProcessPluginDefinition {
	public static final String VERSION = "1.3.0.1";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2025, 8, 15);

	@Override
	public String getName()
	{
		return "eyematics-dsf-process-plugin";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public LocalDate getReleaseDate()
	{
		return RELEASE_DATE;
	}

	@Override
	public List<String> getProcessModels() {
		return List.of("bpe/eyematics-initiate-process.bpmn",
				       "bpe/eyematics-provide-process.bpmn");
	}

	@Override
	public Map<String, List<String>> getFhirResourcesByProcessId() {

		// EYEMATICS PROCESS (v1.0)

		// INITIATE
		String aEyematicsInitiateProcess = "fhir/ActivityDefinition/eyematics-initiate-process-activity-definition.xml";
		String sEyematicsInitiateProcess = "fhir/StructureDefinition/eyematics-initiate-process-structure-definition.xml";
		String tEyematicsInitiateProcess = "fhir/Task/eyematics-initiate-process-task.xml";

		// RECEIVE
		String sEyematicsReceiveProcess = "fhir/StructureDefinition/eyematics-receive-process-structure-definition.xml";
		String cEyematicsReceiveProcess = "fhir/CodeSystem/eyematics-receive-process-code-system.xml";
		String vEyematicsReceiveProcess = "fhir/ValueSet/eyematics-receive-process-value-set.xml";

		// PROVIDE
		String aEyematicsProvideProcess = "fhir/ActivityDefinition/eyematics-provide-process-activity-definition.xml";
		String sEyematicsProvideProcess = "fhir/StructureDefinition/eyematics-provide-process-structure-definition.xml";
		String tEyematicsProvideProcess = "fhir/Task/eyematics-provide-process-task.xml";

		return Map.of(PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS, List.of(aEyematicsInitiateProcess,
																				    sEyematicsInitiateProcess,
																				    tEyematicsInitiateProcess,
																				    sEyematicsReceiveProcess,
																				    cEyematicsReceiveProcess,
																				    vEyematicsReceiveProcess),
				      PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS, List.of(aEyematicsProvideProcess,
						                                                           sEyematicsProvideProcess,
						                                                           tEyematicsProvideProcess));
	}


	@Override
	public List<Class<?>> getSpringConfigurations() {
		return List.of(CryptoConfig.class,
					   DicFhirClientConfig.class,
				       InitiateConfig.class,
				       ProvideConfig.class,
				       ReceiveConfig.class);
	}
}
