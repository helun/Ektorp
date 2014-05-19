package org.ektorp.http;

import org.apache.http.params.HttpParams;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StdHttpClientBuilderTest {

	/**
	 * This test documents that the configureHttpParams() should be protected
	 * so that it can be overridden by applications who need to configure the HttpParams instance
	 * before it is given to the DefaultHttpClient constructor.
	 */
	@Test
	public void shouldOverrideConfigureHttpParams() {
		final AtomicBoolean invoked = new AtomicBoolean(false);

		StdHttpClient.Builder builder = new StdHttpClient.Builder() {

			@Override
			protected HttpParams configureHttpParams() {
				HttpParams result = super.configureHttpParams();

				// application can set additional configuration like the following
				// HttpConnectionParams.setStaleCheckingEnabled(result, false);

				invoked.set(true);

				return result;
			}

		};

		HttpClient httpClient = builder.build();
		assertNotNull(httpClient);
		assertTrue(invoked.get());
	}

}
