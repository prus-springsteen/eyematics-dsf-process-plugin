package org.eyematics.process.fhir.profile;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.eyematics.process.constant.InitiateConstants;
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
			List.of("dsf-task-base-1.0.0.xml", "eyematics-generic-process-data-set-status-error-extension", "eyematics-initiate-process-structure-definition.xml",
					"eyematics-provide-process-receive-receipt-structure-definition.xml", "eyematics-provide-process-structure-definition.xml",
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

/*
	@Test
	public void testTaskStartDataInitiateValid() {

		Task task = createValidTaskDataSendStart();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}
*/

	/*
	@Test
	public void testTaskStartDataSendValidWithReportStatusErrorOutput()
	{
		Task task = createValidTaskDataSendStart();
		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				ConstantsBase.CODESYSTEM_DATA_SET_STATUS_VALUE_NOT_REACHABLE,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	 */

	private Task createValidTaskDataSendStart() {
		Task task = new Task();
		//task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));
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
	/*
	@Test
	public void testTaskDataSendValid()
	{
		Task task = createValidTaskDataSend();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskDataSendValidWithReportStatusOutput()
	{
		Task task = createValidTaskDataSend();
		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				ConstantsBase.CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIVE_OK,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskDataSendValidWithReportStatusErrorOutput()
	{
		Task task = createValidTaskDataSend();
		task.addOutput(new DataSetStatusGenerator().createDataSetStatusOutput(
				ConstantsBase.CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIVE_ERROR,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskDataSend()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsDataTransfer.PROFILE_TASK_DATA_SEND + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				ConstantsDataTransfer.PROFILE_TASK_DATA_SEND_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue("Test_DIC"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue("Test_DIC"));
		task.addInput().setValue(new StringType(ConstantsDataTransfer.PROFILE_TASK_DATA_SEND_MESSAGE_NAME)).getType()
				.addCoding(CodeSystems.BpmnMessage.messageName());

		task.addInput()
				.setValue(new Reference().setReference("https://dsf-dic.de/fhir/Binary/" + UUID.randomUUID().toString())
						.setType(ResourceType.Binary.name()))
				.getType().addCoding().setSystem(ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER)
				.setCode(ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_REFERENCE);
		task.addInput()
				.setValue(new Identifier().setSystem(ConstantsBase.NAMINGSYSTEM_MII_PROJECT_IDENTIFIER)
						.setValue("Test_PROJECT"))
				.getType().addCoding().setSystem(ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER)
				.setCode(ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_PROJECT_IDENTIFIER);
		return task;
	}

	@Test
	public void testTaskDataStatusValidWithResponseInput()
	{
		Task task = createValidTaskDataStatus();
		task.addInput(new DataSetStatusGenerator().createDataSetStatusInput(
				ConstantsBase.CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_OK,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskDataStatusValidWithResponseInputError()
	{
		Task task = createValidTaskDataStatus();
		task.addInput(new DataSetStatusGenerator().createDataSetStatusInput(
				ConstantsBase.CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_ERROR,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER,
				ConstantsDataTransfer.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskDataStatus()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsDataTransfer.PROFILE_TASK_DATA_STATUS + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				ConstantsDataTransfer.PROFILE_TASK_DATA_STATUS_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue("DIC"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(NamingSystems.OrganizationIdentifier.withValue("DIC"));

		task.addInput().setValue(new StringType(ConstantsDataTransfer.PROFILE_TASK_DATA_STATUS_MESSAGE_NAME)).getType()
				.addCoding(CodeSystems.BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(CodeSystems.BpmnMessage.businessKey());

		return task;
	}

	 */
}
