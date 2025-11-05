package org.eyematics.process.utils.client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.IdType;


public interface EyeMaticsFhirClient extends FhirClient {
    String read(IdType idType, String mimeType) throws Exception;
    String read(String resourceType, String searchQuery, String mimeType) throws Exception;
    String create(String resourceType, String fhirResource, String mimeType) throws Exception;
    String create(String fhirResource, String mimeType) throws Exception;
}
