package org.eyematics.process.fhir.profile;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.eyematics.process.constant.*;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import org.eyematics.process.EyeMaticsProcessPluginDefinition;
import dev.dsf.bpe.v1.constants.CodeSystems;
import dev.dsf.bpe.v1.constants.NamingSystems;
import dev.dsf.fhir.validation.ResourceValidator;
import dev.dsf.fhir.validation.ResourceValidatorImpl;
import dev.dsf.fhir.validation.ValidationSupportRule;


public class TaskProfileTest {

	private static final Logger logger = LoggerFactory.getLogger(TaskProfileTest.class);
	private static final EyeMaticsProcessPluginDefinition def = new EyeMaticsProcessPluginDefinition();
	private static final String DIC_IDENTIFIER = "dic.a.test";

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(def.getResourceVersion(),
			def.getResourceReleaseDate(),
			List.of("dsf-task-base-1.0.0.xml", "eyematics-generic-process-data-set-status-error-extension.xml", "eyematics-initiate-process-structure-definition.xml",
					"eyematics-provide-process-acknowledgement-structure-definition.xml", "eyematics-provide-process-structure-definition.xml",
					"eyematics-receive-process-initiate-dic-identifier-extension.xml", "eyematics-receive-process-initiate-structure-definition.xml",
					"eyematics-receive-process-structure-definition.xml"),
			List.of("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "eyematics-generic-process-data-set-status-code-system.xml",
					"eyematics-generic-process-data-set-status-codes-code-system.xml", "eyematics-receive-process-code-system.xml",
					"eyematics-receive-process-initiate-code-system.xml"),
			List.of("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "eyematics-generic-process-data-set-status-codes-value-set.xml",
					"eyematics-generic-process-data-set-status-value-set.xml", "eyematics-receive-process-initiate-value-set.xml",
					"eyematics-receive-process-value-set.xml"));

	private final ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testValidTaskInitiateProcessOutput() {

		Task task = createValidTaskInitiateProcess();

		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_REQUEST_SUCCESS.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

	}

	@Test
	public void testValidTaskInitiateProcessOutputError() {

		Task task = createValidTaskInitiateProcess();

		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_REQUEST_FAILURE.getStatusCode(),
				EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

	}

	private Task createValidTaskInitiateProcess() {
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));
		task.getMeta().addProfile(InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_URI + "|" + def.getResourceVersion());
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.addInput().setValue(new StringType(InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_MESSAGE_NAME)).getType()
				.addCoding(CodeSystems.BpmnMessage.messageName());
		return task;
	}

    @Test
    public void testValidTaskInitiateReceiveProcess() {
        Task task = createValidTaskInitiateReceiveProcess();

        task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
                    EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                    EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskInitiateReceiveProcess() {
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));
		task.getMeta().addProfile(ReceiveConstants.PROFILE_TASK_INITIATE_EYEMATICS_RECEIVE_PROCESS);
		task.setInstantiatesCanonical(
				ReceiveConstants.PROFILE_TASK_EYEMATICS_RECEIVE_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.addInput().setValue(new StringType(ReceiveConstants.PROFILE_TASK_INITIATE_EYEMATICS_RECEIVE_PROCESS_MESSAGE_NAME))
				.getType().addCoding(CodeSystems.BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.businessKey());

        Task.ParameterComponent dic = task.addInput().setValue(new StringType(UUID.randomUUID().toString()));
        dic.getType().addCoding().setSystem(ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE)
                .setCode(ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_INITIATE_PROCESS_CORRELATION_KEY);
        dic.addExtension().setUrl(ReceiveConstants.EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER)
                .setValue(new Reference().setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER))
                        .setType(ResourceType.Organization.name()));

		return task;
	}

	@Test
	public void testValidTaskReceiveProcessOutput() {
		Task task = createValidTaskReceiveProcessResponse();

        task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testValidTaskReceiveProcessOutputError() {
		Task task = createValidTaskReceiveProcessResponse();

		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_DECRYPT_FAILURE.getStatusCode(),
				EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskReceiveProcessResponse() {
		Task task = new Task();
		task.getMeta().addProfile(ReceiveConstants.PROFILE_TASK_EYEMATICS_RECEIVE_PROCESS);
		task.setInstantiatesCanonical(
				ReceiveConstants.PROFILE_TASK_EYEMATICS_RECEIVE_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.addInput().setValue(new StringType(ReceiveConstants.PROFILE_TASK_EYEMATICS_RECEIVE_PROCESS_MESSAGE_NAME)).getType()
				.addCoding(CodeSystems.BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.businessKey());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.correlationKey());

		task.addInput()
				.setValue(new Reference().setReference("https://dic-a/fhir/Binary/" + UUID.randomUUID().toString()))
				.getType().addCoding().setSystem(ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS)
				.setCode(ReceiveConstants.CODE_SYSTEM_RECEIVE_PROCESS_DATASET_REFERENCE);

		return task;
	}

	@Test
	public void testValidTaskInitiateProvideProcessOutput() {
		Task task = createValidTaskInitiateProvideProcess();

		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testValidTaskInitiateProvideProcessOutputError() {
		Task task = createValidTaskInitiateProvideProcess();

        task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				EyeMaticsGenericStatus.DATA_DOWNLOAD_FAILURE.getStatusCode(),
				EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskInitiateProvideProcess() {
		Task task = new Task();
		task.getMeta().addProfile(ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));

		task.addInput().setValue(new StringType(ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_MESSAGE_NAME))
				.getType().addCoding(CodeSystems.BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.businessKey());

		return task;
	}

	@Test
	public void testValidTaskProvideProcessResponseInputOutput() {
		Task task = createValidTaskProvideProcessResponse();

		task.addInput(new DataSetStatusGenerator().createDataSetStatusInput(
				EyeMaticsGenericStatus.DATA_RECEIVE_SUCCESS.getStatusCode(), EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}


	@Test
	public void testValidTaskProvideProcessResponseInputError() {
		Task task = createValidTaskProvideProcessResponse();

		task.addInput(new DataSetStatusGenerator().createDataSetStatusInput(
				EyeMaticsGenericStatus.DATA_DOWNLOAD_FAILURE.getStatusCode(),
				EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
				EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}


	private Task createValidTaskProvideProcessResponse() {
		Task task = new Task();
		task.getMeta().addProfile(ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_ACKNOWLEDGEMENT + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));

		task.addInput().setValue(new StringType(ProvideConstants.PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_ACKNOWLEDGEMENT_MESSAGE_NAME))
				.getType().addCoding(CodeSystems.BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.businessKey());

		return task;
	}

}
