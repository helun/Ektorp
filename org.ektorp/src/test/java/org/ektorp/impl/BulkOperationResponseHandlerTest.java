package org.ektorp.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.DocumentOperationResult;
import org.junit.Test;

public class BulkOperationResponseHandlerTest {

	@Test
	public void testSuccessHttpResponse() throws Exception {
		List<TestDoc> objects = Arrays.asList(new TestDoc("0", "rev0"), new TestDoc("1", "rev1"), new TestDoc("2", "rev2"));
		BulkOperationResponseHandler h = new BulkOperationResponseHandler(objects, new ObjectMapper());

		List<DocumentOperationResult> errors = h.success(ResponseOnFileStub.newInstance(202, "bulk_response.json"));
		assertEquals(1, errors.size());
		assertEquals("0", errors.get(0).getId());
		assertEquals("conflict", errors.get(0).getError());
		assertEquals("Document update conflict.", errors.get(0).getReason());

        assertEquals("1", objects.get(1).getId());
		assertEquals("new_rev1", objects.get(1).getRevision());
        assertEquals("2", objects.get(2).getId());
		assertEquals("new_rev2", objects.get(2).getRevision());
	}

	public static class TestDoc {
		String id;
		String revision;

		public TestDoc(String id, String rev) {
			this.id = id;
			this.revision = rev;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public String getId() {
			return id;
		}

		public String getRevision() {
			return revision;
		}
	}

}
