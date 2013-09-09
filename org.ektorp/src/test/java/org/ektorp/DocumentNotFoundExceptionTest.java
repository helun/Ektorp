package org.ektorp;

import org.apache.commons.io.IOUtils;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.RestTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DocumentNotFoundExceptionTest {

	HttpClient client;

	@Before
	public void setUp() throws Exception {
		client = mock(HttpClient.class);
	}

	@Test
	public void give_resp_body_hasnt_been_set_then_isDocumentDeleted_and_isDatabaseDeleted_should_return_false() {
		DocumentNotFoundException e = new DocumentNotFoundException("some_path");
		assertFalse(e.isDocumentDeleted());
		assertFalse(e.isDatabaseDeleted());
	}

	@Test
	public void test_isDocAndDbDeleted_should_return_true() {
		String[] reasons = { "missing", "deleted", "no_db_file" };
		boolean[] isDocDeletedExpect = { false, true, false };
		boolean[] isDbDeletedExpect = { false, false, true };

		for (int i = 0; i < reasons.length; i++) {
			RestTemplate template = new RestTemplate(client);
			HttpResponse rsp = mock(HttpResponse.class);

			when(client.get(anyString())).thenReturn(rsp);
			when(rsp.isSuccessful()).thenReturn(Boolean.FALSE);
			when(rsp.getCode()).thenReturn(404);
			when(rsp.getContent()).thenReturn(IOUtils.toInputStream(
					String.format("{\"error\":\"not_found\",\"reason\":\"%s\"}", reasons[i])
			));
			try {
				template.get("some_path");
			}
			catch (DocumentNotFoundException e) {
				assertEquals(isDocDeletedExpect[i], e.isDocumentDeleted());
				assertEquals(isDbDeletedExpect[i], e.isDatabaseDeleted());
			}
			verify(rsp).releaseConnection();
		}
	}
}
