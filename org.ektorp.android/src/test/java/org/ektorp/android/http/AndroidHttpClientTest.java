package org.ektorp.android.http;

import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.params.HttpParams;
import org.ektorp.http.HttpResponse;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AndroidHttpClientTest {

    @Test
    public void shouldInvokeClientWhenGetWithStringParam() throws IOException {
        org.apache.http.HttpResponse expectedResponse = mock(org.apache.http.HttpResponse.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn("mock expectedResponse").when(expectedResponse).toString();
        doReturn(null).when(expectedResponse).getEntity();
        doReturn(new FakeStatusLine()).when(expectedResponse).getStatusLine();
        doReturn(null).when(expectedResponse).getFirstHeader("ETag");

        final HttpHost httpHost = new HttpHost("whatever", 1234);

        org.apache.http.client.HttpClient client = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));
        org.apache.http.client.HttpClient backend = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));

        HttpParams httpParams = mock(HttpParams.class);
        doReturn(httpHost).when(httpParams).getParameter(ClientPNames.DEFAULT_HOST);

        doReturn(httpParams).when(client).getParams();

        doReturn(expectedResponse).when(client).execute(eq(httpHost), any(HttpRequest.class));

        AndroidHttpClient service = new AndroidHttpClient(client, backend);

        HttpResponse androidHttpResponse = service.get("");
        assertNotNull(androidHttpResponse);
    }

    @Test
    public void shouldInvokeClientWhenPutWithStringParamAndHttpEntityParam() throws IOException {
        org.apache.http.HttpResponse expectedResponse = mock(org.apache.http.HttpResponse.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn("mock expectedResponse").when(expectedResponse).toString();
        doReturn(null).when(expectedResponse).getEntity();
        doReturn(new FakeStatusLine()).when(expectedResponse).getStatusLine();
        doReturn(null).when(expectedResponse).getFirstHeader("ETag");

        final HttpHost httpHost = new HttpHost("whatever", 1234);

        org.apache.http.client.HttpClient client = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));
        org.apache.http.client.HttpClient backend = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));

        HttpParams httpParams = mock(HttpParams.class);
        doReturn(httpHost).when(httpParams).getParameter(ClientPNames.DEFAULT_HOST);

        doReturn(httpParams).when(client).getParams();

        doReturn(expectedResponse).when(client).execute(eq(httpHost), any(HttpRequest.class));

        AndroidHttpClient service = new AndroidHttpClient(client, backend);

        HttpEntity httpEntity = mock(HttpEntity.class, new ThrowsException(new UnsupportedOperationException()));

        HttpResponse androidHttpResponse = service.put("", httpEntity);
        assertNotNull(androidHttpResponse);
    }

    @Test
    public void shouldInvokeBackendWhenPostWithStringParamAndInputStreamParam() throws IOException {
        org.apache.http.HttpResponse expectedResponse = mock(org.apache.http.HttpResponse.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn("mock expectedResponse").when(expectedResponse).toString();
        doReturn(null).when(expectedResponse).getEntity();
        doReturn(new FakeStatusLine()).when(expectedResponse).getStatusLine();
        doReturn(null).when(expectedResponse).getFirstHeader("ETag");

        org.apache.http.client.HttpClient client = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));
        org.apache.http.client.HttpClient backend = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn(expectedResponse).when(backend).execute(any(HttpUriRequest.class));

        AndroidHttpClient service = new AndroidHttpClient(client, backend);

        HttpResponse androidHttpResponse = service.post("", new ByteArrayInputStream("".getBytes()));
        assertNotNull(androidHttpResponse);
    }

    @Test
    public void shouldInvokeBackendWhenPostWithStringParamAndHttpEntityParam() throws IOException {
        org.apache.http.HttpResponse expectedResponse = mock(org.apache.http.HttpResponse.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn("mock expectedResponse").when(expectedResponse).toString();
        doReturn(null).when(expectedResponse).getEntity();
        doReturn(new FakeStatusLine()).when(expectedResponse).getStatusLine();
        doReturn(null).when(expectedResponse).getFirstHeader("ETag");

        org.apache.http.client.HttpClient client = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));
        org.apache.http.client.HttpClient backend = mock(org.apache.http.client.HttpClient.class, new ThrowsException(new UnsupportedOperationException()));

        doReturn(expectedResponse).when(backend).execute(any(HttpUriRequest.class));

        AndroidHttpClient service = new AndroidHttpClient(client, backend);

        HttpEntity httpEntity = mock(HttpEntity.class, new ThrowsException(new UnsupportedOperationException()));

        HttpResponse androidHttpResponse = service.post("", httpEntity);
        assertNotNull(androidHttpResponse);
    }

    public static class FakeStatusLine implements StatusLine {

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }
    }

}
