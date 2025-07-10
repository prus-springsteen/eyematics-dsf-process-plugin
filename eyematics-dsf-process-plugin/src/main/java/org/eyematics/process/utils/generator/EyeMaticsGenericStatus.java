package org.eyematics.process.utils.generator;

import org.apache.commons.text.CaseUtils;


public enum EyeMaticsGenericStatus {
    DATA_READ_FAILED,
    DATA_BUNDLE_FAILED,
    DATA_ENCRYPT_FAILED,
    DATA_STORE_FAILED,
    DATA_PROVIDE_FAILED,
    DATA_PROVIDE_FORBIDDEN,
    DATA_RECEIPT_MISSING,
    DATA_DOWNLOAD_FAILED,
    DATA_DECRYPT_FAILED,
    DATA_RECEIVE_SUCCESSFUL,
    DATA_RECEIPT_FAILED,
    DATA_RECEIPT_FORBIDDEN;

    private final String typeSystem = "http://eyematics.org/fhir/CodeSystem/eyematics-generic-process-data-set-status-code-system";
    private final String typeCode = "data-set-status";
;
    public String getTypeSystem() {
        return this.typeSystem;
    }

    public String getTypeCode() {
        return this.typeCode;
    }

    public String getStatusCode() {
        return this.name().toLowerCase().replace('_', '-');
    }

    public String getErrorCode() {
        return CaseUtils.toCamelCase(this.name(), false, '_');
    }

    public String getErrorMessage() {
        return this.getErrorCode() + "Message";
    }
}
