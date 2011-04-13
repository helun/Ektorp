package org.ektorp.http;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.apache.commons.io.*;
import org.ektorp.*;
import org.junit.*;

/**
 * 
 * @author Henrik Lundgren
 * created 1 nov 2009
 *
 */
public class RestTemplateTest {

	HttpClient client;
	
	@Before
	public void setUp() throws Exception {
		client = mock(HttpClient.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void successful_get2_should_call_success_on_callback_interface_and_release_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.get("some_path", callback);
		
		verify(callback).success(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void connection_should_remain_open_when_calling_raw_get() throws Exception {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		assertNotNull(template.get("some_path"));		
		verify(rsp, never()).releaseConnection();
	}

	@Test
	public void exceptions_in_get_should_be_rethrown_and_connection_released() throws Exception {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenThrow(new RuntimeException("test"));
		try {
			template.get("some_path");
			fail("RT expected");
		} catch (RuntimeException e) {
			// expected
		}
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void unsucessful_get_should_release_connection() {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
		when(rsp.getCode()).thenReturn(404);
		when(rsp.getContent()).thenReturn(IOUtils.toInputStream("{\"error\":\"not_found\",\"reason\":\"missing\"}"));
		try {
		template.get("some_path");
		} catch (DocumentNotFoundException e) {
			// expected
		}
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void exceptions_in_get2_should_be_rethrown_and_connection_released() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get("some_path")).thenReturn(rsp);
		when(rsp.isSuccessful()).thenThrow(new RuntimeException("test"));
		
		try {
			template.get("some_path", callback);
			fail("exception expected");
		} catch (RuntimeException e) {
			// expected
		}
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void failed_get_should_call_error_method_in_callback_and_relase_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.get("some_path")).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
		
		template.get("some_path", callback);
		
		verify(callback).error(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void put_should_release_connection() {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put("/some/path")).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.put("/some/path");
		
		verify(client).put("/some/path");
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void exceptions_in_put_should_be_rethrown_and_connection_released() {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
		
		try {
			template.put("/some/path");
			fail("exception expected");
		} catch (RuntimeException e) {
			// expected
		}
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void exceptions_in_put2_should_be_rethrown_and_connection_released() {
		RestTemplate template = new RestTemplate(client);
		
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.put("/some/path", "content");
		
		verify(client).put("/some/path", "content");
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void exceptions_in_put4_should_be_rethrown_and_connection_released() {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString(), any(InputStream.class), anyString(), anyInt())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.put("/some/path", IOUtils.toInputStream("content"), "text/html", 12l);
		
		verify(client).put(eq("/some/path"), any(InputStream.class), eq("text/html"), eq(12l));
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void put3_should_call_success_in_callback_and_release_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.put("some_path", "content", callback);
		
		verify(callback).success(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void failed_put3_should_call_error_and_release_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
		
		template.put("some_path", "content", callback);
		
		verify(callback).error(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void exceptions_in_put3_should_be_rethrown_error_not_called_and_connection_released() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.put(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		when(callback.success(rsp)).thenThrow(new Exception("test"));
		
		try {
			template.put("some_path", "content", callback);
			fail("RT expected");
		} catch (RuntimeException e) {
			
		}
		
		verify(callback, never()).error(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@Test
	public void delete_should_release_connection() {
		RestTemplate template = new RestTemplate(client);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.delete(anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.delete("/some/path");
		
		verify(client).delete("/some/path");
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void successful_post3_should_call_success_and_release_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.post(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		
		template.post("some_path", "content", callback);
		
		verify(callback).success(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void failed_post3_should_call_error_and_release_connection() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.post(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
		
		template.post("some_path", "content", callback);
		
		verify(callback).error(any(HttpResponse.class));
		verify(rsp).releaseConnection();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void exceptions_in_callback_should_be_rethrown_and_connection_released() throws Exception {
		RestTemplate template = new RestTemplate(client);
		ResponseCallback<String> callback = mock(ResponseCallback.class);
		HttpResponse rsp = mock(HttpResponse.class);
		
		when(client.post(anyString(), anyString())).thenReturn(rsp);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		when(callback.success(rsp)).thenThrow(new Exception("test"));
		
		try {
			template.post("some_path", "content", callback);
			fail("RT expected");
		} catch (RuntimeException e) {
			
		}
		
		verify(rsp).releaseConnection();
	}
}
