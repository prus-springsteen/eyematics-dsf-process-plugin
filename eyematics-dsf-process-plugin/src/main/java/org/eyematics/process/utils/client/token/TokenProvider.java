package org.eyematics.process.utils.client.token;

public interface TokenProvider
{
	boolean isConfigured();

	String getInfo();

	String getToken();
}
