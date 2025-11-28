package org.eyematics.process.constant;


public interface ProvideConstants {
    String PROCESS_NAME_EXECUTE_PROVIDE_EYEMATICS_PROCESS = "eyematicsProvideProcess";
    String PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS =  EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE + PROCESS_NAME_EXECUTE_PROVIDE_EYEMATICS_PROCESS;

    String BPMN_EXECUTION_VARIABLE_EYEMATICS_DATA_SET_ADMIN_APPROVAL_REQUIRED = "eyematicsDataSetAdminApprovalRequired";
    String BPMN_EXECUTION_VARIABLE_EYEMATICS_DATA_SET_ADMIN_APPROVED = "eyematicsDataSetAdminApproved";

    String BPMN_PROVIDE_EXECUTION_VARIABLE_PATIENT_DATA_SET = "patientDataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_OBSERVATION_DATA_SET = "observationDataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_DATA_SET = "medicationDataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_ADMINISTRATION_DATA_SET = "medicationAdministrationDataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_MEDICATION_REQUEST_DATA_SET = "medicationRequestDataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET = "dataSet";
    String BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED = "dataSetEncrypted";

    String BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET_REFERENCE = "data-reference";

    String PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_URI = EyeMaticsConstants.PROCESS_EYEMATICS_URI_BASE + PROCESS_NAME_EXECUTE_PROVIDE_EYEMATICS_PROCESS;
    String PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS = "https://eyematics.org/fhir/StructureDefinition/eyematics-provide-process-structure-definition";
    String PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_MESSAGE_NAME = "provideProcessStartEventMessage";
    String PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_ACKNOWLEDGEMENT = "https://eyematics.org/fhir/StructureDefinition/eyematics-provide-process-acknowledgement-structure-definition";
    String PROFILE_TASK_EYEMATICS_PROVIDE_PROCESS_ACKNOWLEDGEMENT_MESSAGE_NAME = "provideProcessAcknowledgementEventMessage";
}
