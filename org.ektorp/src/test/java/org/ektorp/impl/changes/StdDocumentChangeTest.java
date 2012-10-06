package org.ektorp.impl.changes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import junit.framework.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.StreamingChangesResult;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpResponse;
import org.ektorp.impl.ResponseOnFileStub;
import org.junit.Test;

public class StdDocumentChangeTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void test_normal_message() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message.json"));
		assertMandatoryFields(m);
		assertNull(m.getDoc());
		assertTrue(m.getDocAsNode().isMissingNode());
		assertFalse(m.isDeleted());
	}

	private void assertMandatoryFields(StdDocumentChange m) {
		assertEquals(21, m.getSequence());
		assertEquals("doc_id", m.getId());
		assertEquals("doc_rev", m.getRevision());
	}

	@Test
	public void test_deleted_doc_message() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message_w_deleted_doc.json"));
		assertMandatoryFields(m);
		assertTrue(m.isDeleted());
	}

	@Test
	public void test_message_with_included_doc() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message_w_included_doc.json"));
		assertMandatoryFields(m);
		assertNotNull(m.getDoc());
		assertFalse(m.getDocAsNode().isMissingNode());
		assertNotNull(m.getDocAsNode().findValue("_id"));
		assertNotNull(m.getDocAsNode().findValue("_rev"));
	}
	
	
	@Test
    public void test_streaming_changes() throws IOException {
	    HttpResponse httpResponse = ResponseOnFileStub.newInstance(200, "changes/changes_full.json");
	    
	    StreamingChangesResult changes = new StreamingChangesResult(new ObjectMapper(),
                httpResponse);
	    int i = 0;
        for (DocumentChange documentChange : changes) {
            Assert.assertEquals(++i, documentChange.getSequence());
        }
        Assert.assertEquals(5, changes.getLastSeq());
    }
	
	private JsonNode load(String id) throws IOException {
		return mapper.readTree(getClass().getResourceAsStream(id));
	}

}
