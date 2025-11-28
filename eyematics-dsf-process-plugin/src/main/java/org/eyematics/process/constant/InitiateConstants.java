package org.eyematics.process.constant;


public interface InitiateConstants {
    String PROCESS_NAME_EXECUTE_INITIATE_EYEMATICS_PROCESS = "eyematicsInitiateProcess";
    String PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS =  EyeMaticsConstants.PROCESS_EYEMATICS_NAME_BASE + PROCESS_NAME_EXECUTE_INITIATE_EYEMATICS_PROCESS;

    String PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START = "https://eyematics.org/fhir/StructureDefinition/eyematics-initiate-process-structure-definition";
    String PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_URI = EyeMaticsConstants.PROCESS_EYEMATICS_URI_BASE + PROCESS_NAME_EXECUTE_INITIATE_EYEMATICS_PROCESS;
    String PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_MESSAGE_NAME= "initiateProcessStartEventMessage";
}
