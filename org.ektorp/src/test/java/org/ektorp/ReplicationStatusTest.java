package org.ektorp;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.*;

import java.io.InputStream;

public class ReplicationStatusTest {

	@Test
	public void deserialize() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = loadReplicationStatusFromResource(mapper, "replication_response.json");
		assertNotNull(rs.getSessionId());
		assertNotNull(rs.getId());
		assertNotNull(rs.getSourceLastSequence());
		assertFalse(rs.getHistory().isEmpty());
		
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getSessionId());
		assertNotNull(h.getStartTime());
		assertNotNull(h.getEndTime());
		assertNotNull(h.getEndTime());
		assertNotNull(h.getStartLastSeq());
		assertNotNull(h.getEndLastSeq());
		assertNotNull(h.getRecordedSeq());
		assertTrue(h.getMissingChecked() > 0);
		assertTrue(h.getMissingFound() > 0);
		assertTrue(h.getDocsRead() > 0);
		assertTrue(h.getDocsWritten() > 0);
		assertTrue(h.getDocWriteFailures() > 0);
	}

    @Test
	public void sequences_as_strings() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = loadReplicationStatusFromResource(mapper, "replication_response_string_seqs.json");
		assertNotNull(rs.getSourceLastSequence());
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getStartLastSeq());
		assertNotNull(h.getEndLastSeq());
	}

	@Test
	public void sequences_as_arrays() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ReplicationStatus rs = loadReplicationStatusFromResource(mapper, "replication_response_array_seqs.json");
		assertNotNull(rs.getSourceLastSequenceAsNode());
		ReplicationStatus.History h = rs.getHistory().get(0);
		assertNotNull(h.getStartLastSeqAsNode());
		assertNotNull(h.getEndLastSeqAsNode());
	}

    @Test
    public void recordedSeq_as_number() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ReplicationStatus rs = loadReplicationStatusFromResource(mapper, "replication_response_string_recorded_seq.json");
        Assert.assertNotNull(rs.getHistory());
        Assert.assertNotNull(rs.getHistory().get(0));
        Assert.assertTrue(rs.getHistory().get(0).getRecordedSeq().length() > 10);
    }

    @Test
    public void recordedSeq_as_string() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ReplicationStatus rs = loadReplicationStatusFromResource(mapper, "replication_response_number_recorded_seq.json");
        Assert.assertNotNull(rs.getHistory());
        Assert.assertNotNull(rs.getHistory().get(0));
        String recordedSeq = rs.getHistory().get(0).getRecordedSeq();
        Assert.assertEquals("4", recordedSeq);
    }

    private ReplicationStatus loadReplicationStatusFromResource(ObjectMapper mapper, String name) throws java.io.IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getResourceAsStream(name);
            return mapper.readValue(resourceAsStream, ReplicationStatus.class);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

}
