package org.ektorp.http;

import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;

import java.net.*;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class StdHttpResponseTest
{
	private HttpUriRequest uriRequest;
	private HttpResponse apacheResponse;

	@Before
	public void setUp() throws URISyntaxException
	{
		apacheResponse = mock(HttpResponse.class);

		uriRequest = mock(HttpUriRequest.class);
		java.net.URI requestURI = new java.net.URI("http://tempuri.org/");
		stub(uriRequest.getURI()).toReturn(requestURI);
	}

	@Test
	public void of_will_cause_getETag_to_return_the_ETag_value_extracted_from_quotes()
	{
		String revision = UUID.randomUUID().toString();
		String eTag = "\"" + revision + "\"";
		Header eTagHeader = mock(Header.class);
		stub(eTagHeader.getValue()).toReturn(eTag);
		stub(apacheResponse.getFirstHeader("ETag")).toReturn(eTagHeader);

		assertThat(StdHttpResponse.of(apacheResponse, uriRequest).getETag(), is(revision));
	}

	@Test
	public void of_will_cause_getETag_to_return_null_if_ETag_is_not_present()
	{
		assertThat(StdHttpResponse.of(apacheResponse, uriRequest).getETag(), nullValue());
	}
}
