/**
 * @author Reto Wettstein (https://github.com/wetret)
 */

package org.eyematics.process.utils.client;

import ca.uhn.fhir.context.FhirContext;
import org.eyematics.process.utils.client.logging.DataLogger;


public interface FhirClient
{
	/**
	 * @return not <code>null</code> or empty
	 */
	String getLocalIdentifierValue();

	/**
	 * @return not <code>null</code>
	 */
	FhirContext getFhirContext();

	/**
	 * Base URL of the FHIR server this client connects to.
	 *
	 * @return not <code>null</code> or empty
	 */
	String getFhirBaseUrl();

	/**
	 * @return not <code>null</code>
	 */
	DataLogger getDataLogger();

	/**
	 * Testing connection to the FHIR server CapabilityStatement using the base URL defined by
	 * {@link #getFhirBaseUrl()}.
	 */
	void testConnection();
}
