package org.eyematics.process.utils.bpe;

import ca.uhn.fhir.context.FhirContext;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.client.EyeMaticsFhirClient;
import org.hl7.fhir.r4.model.Bundle;

public class EyeMaticsDataBundleRetriever {

    public static Bundle getEyeMaticsDataBundle(EyeMaticsFhirClient fhirClient, String resource, String searchQuery) throws Exception {
        String data = fhirClient.read(resource, searchQuery, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
        Bundle dataBundle = FhirContext.forR4().newJsonParser().parseResource(org.hl7.fhir.r4.model.Bundle.class, data);
        String nextLink = getNextLink(dataBundle);
        while (nextLink != null) {
            String nextData = fhirClient.read(resource, searchQuery, EyeMaticsConstants.MEDIA_TYPE_APPLICATION_FHIR_JSON);
            org.hl7.fhir.r4.model.Bundle nextBundle = FhirContext.forR4().newJsonParser().parseResource(Bundle.class, nextData);
            dataBundle.getEntry().addAll(nextBundle.getEntry());
            nextLink = getNextLink(nextBundle);
        }
        return FhirContext.forR4().newJsonParser().parseResource(org.hl7.fhir.r4.model.Bundle.class, data);
    }

    private static String getNextLink(Bundle bundle) {
        return bundle.getLink(Bundle.LINK_NEXT) != null ? bundle.getLink(Bundle.LINK_NEXT).getUrl() : null;
    }
}
