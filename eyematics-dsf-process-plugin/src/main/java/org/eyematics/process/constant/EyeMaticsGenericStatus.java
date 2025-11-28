package org.eyematics.process.constant;

import org.apache.commons.text.CaseUtils;


public enum EyeMaticsGenericStatus {
    DATA_INITIATE_FAILURE,
    DATA_INITIATE_FORBIDDEN,
    DATA_REQUEST_SUCCESS,
    DATA_REQUEST_FAILURE,
    DATA_REQUEST_FORBIDDEN,
    DATA_READ_FAILURE,
    CONSENT_CHECK_FAILURE,
    PATIENT_READ_FAILURE,
    PSEUDONYM_PROCESS_FAILURE,
    DATA_BUNDLE_FAILURE,
    DATA_ENCRYPT_FAILURE,
    DATA_STORE_FAILURE,
    DATA_PROVIDE_FAILURE,
    DATA_PROVIDE_FORBIDDEN,
    DATA_ACKNOWLEDGE_MISSING,
    DATA_DELETE_FAILURE,
    DATA_DOWNLOAD_FAILURE,
    DATA_DECRYPT_FAILURE,
    DATA_INSERT_FAILURE,
    DATA_RECEIVE_SUCCESS,
    DATA_ACKNOWLEDGE_FAILURE,
    DATA_ACKNOWLEDGE_FORBIDDEN,
    DATA_PROVIDE_MISSING;

    private static final String TYPE_SYSTEM = "https://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system";
    private static final String TYPE_CODE = "data-set-status";

    public static String getTypeSystem() {
        return TYPE_SYSTEM;
    }

    public static String getTypeCode() {
        return TYPE_CODE;
    }

    public String getStatusCode() {
        return this.name().toLowerCase().replace('_', '-');
    }

    public String getErrorCode() {
        return switch (this) {
            case DATA_INITIATE_FAILURE, DATA_INITIATE_FORBIDDEN -> "dataInitiateFailure";
            case DATA_REQUEST_FAILURE, DATA_REQUEST_FORBIDDEN -> "dataRequestFailure";
            case DATA_PROVIDE_FAILURE, DATA_PROVIDE_FORBIDDEN -> "dataProvideFailure";
            case DATA_ACKNOWLEDGE_FAILURE, DATA_ACKNOWLEDGE_FORBIDDEN -> "dataAcknowledgeFailure";
            default -> CaseUtils.toCamelCase(this.name(), false, '_');
        };
    }

    public String getErrorMessage() {
        return this.getErrorCode() + "Message";
    }
}
