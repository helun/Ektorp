package org.ektorp.impl.changes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.ektorp.StreamingChangesResult;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpResponse;
import org.ektorp.impl.ResponseOnFileStub;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItems;

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
    public void getRevision_should_return_the_first_revision_when_there_are_multiple_changes() throws IOException
    {
        StdDocumentChange objectUnderTest = new StdDocumentChange(load("change_message_w_multiple_revs.json"));
        assertThat(objectUnderTest.getId(), is("doc_id"));
        assertThat(objectUnderTest.getRevision(), is("rev-first"));
        assertNull(objectUnderTest.getDoc());
        assertTrue(objectUnderTest.getDocAsNode().isMissingNode());
        assertFalse(objectUnderTest.isDeleted());
    }

    @Test
    public void getRevisions_should_return_a_List_of_all_the_changes() throws IOException
    {
        StdDocumentChange objectUnderTest = new StdDocumentChange(load("change_message_w_multiple_revs.json"));
        assertThat(objectUnderTest.getId(), is("doc_id"));
        assertThat(objectUnderTest.getRevision(), is("rev-first"));
        assertThat(objectUnderTest.getRevisions(), notNullValue());
        assertThat(objectUnderTest.getRevisions(), hasItems("rev-first", "rev-second", "rev-third"));
        assertThat(objectUnderTest.getRevisions().size(), is(3));
        assertNull(objectUnderTest.getDoc());
        assertTrue(objectUnderTest.getDocAsNode().isMissingNode());
        assertFalse(objectUnderTest.isDeleted());
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
