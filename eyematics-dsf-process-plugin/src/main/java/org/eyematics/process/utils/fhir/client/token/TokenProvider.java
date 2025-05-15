package org.eyematics.process.utils.fhir.client.token;

public interface TokenProvider
{
	boolean isConfigured();

	String getInfo();

	String getToken();
}
