package org.ektorp.http;

import java.io.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.protocol.*;
import org.apache.http.impl.auth.*;
import org.apache.http.protocol.*;
/**
 * Interceptor that preemptively introduces an instance of BasicScheme to the execution context, if no authentication has been attempted yet. 
 *
 */
public class PreemptiveAuthRequestInterceptor implements HttpRequestInterceptor {

	public void process(
            final HttpRequest request, 
            final HttpContext context) throws HttpException, IOException {
        
        AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);
        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                ClientContext.CREDS_PROVIDER);
        HttpHost targetHost = (HttpHost) context.getAttribute(
                ExecutionContext.HTTP_TARGET_HOST);
        
        // If not auth scheme has been initialized yet
        if (authState.getAuthScheme() == null) {
            AuthScope authScope = new AuthScope(
                    targetHost.getHostName(), 
                    targetHost.getPort());
            // Obtain credentials matching the target host
            Credentials creds = credsProvider.getCredentials(authScope);
            // If found, generate BasicScheme preemptively
            if (creds != null) {
                authState.setAuthScheme(new BasicScheme());
                authState.setCredentials(creds);
            }
        }
    }

}
