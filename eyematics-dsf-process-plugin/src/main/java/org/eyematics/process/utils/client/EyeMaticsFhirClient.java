package org.eyematics.process.utils.client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.IdType;

import java.io.InputStream;

public interface EyeMaticsFhirClient extends FhirClient {
    String read(IdType idType, String mimeType) throws Exception;
    MethodOutcome create(IdType idType, String fhirResource, String mimeType) throws Exception;
}
