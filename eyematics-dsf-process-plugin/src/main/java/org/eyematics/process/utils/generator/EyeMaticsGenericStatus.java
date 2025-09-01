package org.eyematics.process.utils.generator;

import org.apache.commons.text.CaseUtils;


public enum EyeMaticsGenericStatus {
    DATA_INITIATION_FAILURE,
    DATA_INITIATION_FORBIDDEN,
    DATA_REQUEST_SUCCESS,
    DATA_REQUEST_FAILURE,
    DATA_REQUEST_FORBIDDEN,
    DATA_READ_FAILURE,
    DATA_BUNDLE_FAILURE,
    DATA_ENCRYPTION_FAILURE,
    DATA_STORE_FAILURE,
    DATA_PROVISION_FAILURE,
    DATA_PROVISION_FORBIDDEN,
    DATA_RECEIPT_MISSING,
    DATA_DOWNLOAD_FAILURE,
    DATA_DECRYPTION_FAILURE,
    DATA_RECEIPT_SUCCESS,
    DATA_RECEIPT_FAILURE,
    DATA_RECEIPT_FORBIDDEN,
    DATA_PROVISION_MISSING;

    private static final String TYPE_SYSTEM = "http://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system";
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
            case DATA_INITIATION_FAILURE, DATA_INITIATION_FORBIDDEN -> "dataInitiateFailure";
            case DATA_REQUEST_FAILURE, DATA_REQUEST_FORBIDDEN -> "dataRequestFailure";
            case DATA_PROVISION_FAILURE, DATA_PROVISION_FORBIDDEN -> "dataProvideFailure";
            case DATA_RECEIPT_FAILURE, DATA_RECEIPT_FORBIDDEN -> "dataAcknowledgeFailure";
            default -> CaseUtils.toCamelCase(this.name(), false, '_');
        };
    }

    public String getErrorMessage() {
        return this.getErrorCode() + "Message";
    }
}
