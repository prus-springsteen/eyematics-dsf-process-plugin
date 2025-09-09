/**
 * @author Reto Wettstein (https://github.com/wetret)
 */

package org.eyematics.process.utils.client.token;

public interface TokenClient
{
	boolean isConfigured();

	String getInfo();

	AccessToken requestToken();
}
