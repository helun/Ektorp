package org.ektorp.impl;

import static org.junit.Assert.*;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.junit.*;

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
		
		assertEquals("new_rev1", objects.get(1).getRevision());
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
