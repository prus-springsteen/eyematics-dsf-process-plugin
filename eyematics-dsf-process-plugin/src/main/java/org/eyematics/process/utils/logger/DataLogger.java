/**
 * @author Reto Wettstein (https://github.com/medizininformatik-initiative/mii-processes-common/commits?author=wetret)
 * @see    https://github.com/medizininformatik-initiative/mii-processes-common/blob/main/src/main/java/de/medizininformatik_initiative/processes/common/fhir/client/logging/DataLogger.java
 */
package org.eyematics.process.utils.logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;

public class DataLogger
{
    private static final Logger logger = LoggerFactory.getLogger(DataLogger.class);

    private final boolean enabled;
    private final FhirContext fhirContext;

    public DataLogger(boolean enabled, FhirContext fhirContext)
    {
        this.enabled = enabled;
        this.fhirContext = fhirContext;
    }

    public void logResource(String logMessage, IBaseResource resource)
    {
        if (enabled)
            logger.debug("{}: {}", logMessage, asString(resource));
    }

    public void logMethodOutcome(String logMessage, MethodOutcome outcome)
    {
        if (enabled)
            logger.debug("{}: {}", logMessage, asString(outcome));
    }

    private String asString(IBaseResource resource)
    {
        return fhirContext.newJsonParser().encodeResourceToString(resource);
    }

    private String asString(MethodOutcome outcome)
    {
        return "[created: " + outcome.getCreated() + ", id: " + outcome.getId() + ", operation-outcome: "
                + (outcome.getOperationOutcome() != null ? asString(outcome.getOperationOutcome()) : "")
                + ", resource: " + (outcome.getResource() != null ? asString(outcome.getResource()) : "null") + "]";
    }
}
