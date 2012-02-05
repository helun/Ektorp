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
		assertNotNull(h.getStartLastSeq());
		assertNotNull(h.getEndLastSeq());
		assertTrue(h.getRecordedSeq() > 0);
		assertTrue(h.getMissingChecked() > 0);
		assertTrue(h.getMissingFound() > 0);
		assertTrue(h.getDocsRead() > 0);
		assertTrue(h.getDocsWritten() > 0);
		assertTrue(h.getDocWriteFailures() > 0);
	}
	
	@Test
	public void sequences_as_strings() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = mapper.readValue(getClass().getResourceAsStream("replication_response_string_seqs.json"), ReplicationStatus.class);
		assertNotNull(rs.getSourceLastSequence());
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getStartLastSeq());
		assertNotNull(h.getEndLastSeq());
	}
	
	@Test
	public void sequences_as_arrays() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = mapper.readValue(getClass().getResourceAsStream("replication_response_array_seqs.json"), ReplicationStatus.class);
		assertNotNull(rs.getSourceLastSequenceAsNode());
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getStartLastSeqAsNode());
		assertNotNull(h.getEndLastSeqAsNode());
	}
}
