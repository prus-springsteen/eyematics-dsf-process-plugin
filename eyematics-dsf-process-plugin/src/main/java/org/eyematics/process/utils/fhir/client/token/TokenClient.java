package org.eyematics.process.utils.fhir.client.token;

public interface TokenClient
{
	boolean isConfigured();

	String getInfo();

	AccessToken requestToken();
}
