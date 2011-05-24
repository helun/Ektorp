package org.ektorp;

import static org.junit.Assert.*;

import org.codehaus.jackson.map.*;
import org.junit.*;


public class ReplicationStatusTest {

	@Test
	public void deserialize() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = mapper.readValue(getClass().getResourceAsStream("replication_response.json"), ReplicationStatus.class);
		assertNotNull(rs.getSessionId());
		assertNotNull(rs.getSourceLastSequence());
		assertFalse(rs.getHistory().isEmpty());
		
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getSessionId());
		assertNotNull(h.getStartTime());
		assertNotNull(h.getEndTime());
		assertNotNull(h.getEndTime());
		assertTrue(h.getStartLastSeq() > 0);
		assertTrue(h.getEndLastSeq() > 0);
		assertTrue(h.getRecordedSeq() > 0);
		assertTrue(h.getMissingChecked() > 0);
		assertTrue(h.getMissingFound() > 0);
		assertTrue(h.getDocsRead() > 0);
		assertTrue(h.getDocsWritten() > 0);
		assertTrue(h.getDocWriteFailures() > 0);
	}
}
