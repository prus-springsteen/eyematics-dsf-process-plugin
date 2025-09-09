/**
 * @author Reto Wettstein (https://github.com/wetret)
 */

package org.eyematics.process.utils.client.interceptor;

import java.util.Objects;

import org.eyematics.process.utils.client.token.TokenProvider;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;


public class OAuth2Interceptor implements IClientInterceptor, InitializingBean
{
	private final TokenProvider tokenProvider;

	public OAuth2Interceptor(TokenProvider tokenProvider)
	{
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(tokenProvider, "tokenProvider");
	}

	@Override
	public void interceptRequest(IHttpRequest theRequest)
	{
		theRequest.addHeader(Constants.HEADER_AUTHORIZATION,
				Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER + tokenProvider.getToken());
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse)
	{
		// do not intercept response
	}
}
