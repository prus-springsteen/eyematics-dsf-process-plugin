package org.eyematics.process.constant;

public interface ReceiveConstants {
    String PROCESS_NAME_EXECUTE_RECEIVE_EYEMATICS_PROCESS = "eyematicsReceiveProcess";
    String PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS =  EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE + PROCESS_NAME_EXECUTE_RECEIVE_EYEMATICS_PROCESS;
    String CODE_SYSTEM_RECEIVE_PROCESS_INITIATE = "http://eyematics.org/fhir/CodeSystem/eyematics-receive-process-initiate-code-system";
    String CODE_SYSTEM_RECEIVE_PROCESS_INITIATE_PROCESS_CORRELATION_KEY= "dic-correlation-key";
    String EXTENSION_RECEIVE_PROCESS_INITIATE_URL_DIC_IDENTIFIER = "http://eyematics.org/fhir/Extension/dic-identifier";
    String CODE_SYSTEM_RECEIVE_PROCESS = "http://eyematics.org/fhir/CodeSystem/eyematics-receive-process-code-system";
    String CODE_SYSTEM_RECEIVE_PROCESS_DATASET_REFERENCE = "data-set-reference-input";
    String BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET_ENCRYPTED = "dataSetEncrypted";
    String BPMN_RECEIVE_EXECUTION_VARIABLE_DATA_SET = "dataSet";
    String BPMN_RECEIVE_EXECUTION_VARIABLE_ERROR_RESOURCE = "errorResource";
}
