package org.ektorp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.ektorp.ActiveTask;
import org.ektorp.ReplicationTask;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StdReplicationTaskTest {

	@Test
	public void shouldParseValidStdReplicationTaskWithCheckpointInterval() throws IOException {
		final String resourceName = "replication_task_sample_with_checkpointinterval.json";
		ActiveTask activeTask = readResourceAsActiveTask(resourceName);
		assertNotNull(activeTask);
		ReplicationTask replicationTask = (ReplicationTask) activeTask;
		assertEquals("0.992.0>", replicationTask.getPid());
		assertEquals(Long.valueOf(5000l), replicationTask.getCheckpointInterval());
		assertEquals(95948, replicationTask.getCheckpointedSourceSequenceId());
		assertEquals(true, replicationTask.isContinuous());
		assertEquals(null, replicationTask.getReplicationDocumentId());
		assertEquals(0, replicationTask.getWriteFailures());
		assertEquals(8, replicationTask.getTotalReads());
		assertEquals(8, replicationTask.getTotalWrites());
		assertEquals(8, replicationTask.getTotalMissingRevisions());
		assertEquals(100, replicationTask.getProgress());
		assertEquals("cc86d412959b7c453ab661ce23312db7+continuous", replicationTask.getReplicationId());
		assertEquals(777, replicationTask.getTotalRevisionsChecked());
		assertEquals("sourceDB", replicationTask.getSourceDatabaseName());
		assertEquals(373378, replicationTask.getSourceSequenceId());
		assertEquals(1422566612, replicationTask.getStartedOn().getTime());
		assertEquals("targetDB", replicationTask.getTargetDatabaseName());
		assertEquals(1422581543, replicationTask.getUpdatedOn().getTime());
	}

	@Test
	public void shouldParseValidStdReplicationTaskWithoutCheckpointInterval() throws IOException {
		final String resourceName = "replication_task_sample_without_checkpointinterval.json";
		ActiveTask activeTask = readResourceAsActiveTask(resourceName);
		assertNotNull(activeTask);
		ReplicationTask replicationTask = (ReplicationTask) activeTask;
		assertEquals("0.992.0>", replicationTask.getPid());
		assertEquals(null, replicationTask.getCheckpointInterval());
		assertEquals(95948, replicationTask.getCheckpointedSourceSequenceId());
		assertEquals(true, replicationTask.isContinuous());
		assertEquals(null, replicationTask.getReplicationDocumentId());
		assertEquals(0, replicationTask.getWriteFailures());
		assertEquals(8, replicationTask.getTotalReads());
		assertEquals(8, replicationTask.getTotalWrites());
		assertEquals(8, replicationTask.getTotalMissingRevisions());
		assertEquals(100, replicationTask.getProgress());
		assertEquals("cc86d412959b7c453ab661ce23312db7+continuous", replicationTask.getReplicationId());
		assertEquals(777, replicationTask.getTotalRevisionsChecked());
		assertEquals("sourceDB", replicationTask.getSourceDatabaseName());
		assertEquals(373378, replicationTask.getSourceSequenceId());
		assertEquals(1422566612, replicationTask.getStartedOn().getTime());
		assertEquals("targetDB", replicationTask.getTargetDatabaseName());
		assertEquals(1422581543, replicationTask.getUpdatedOn().getTime());
	}

	private ActiveTask readResourceAsActiveTask(String resourceName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ActiveTask activeTask;
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = getClass().getResourceAsStream(resourceName);
			activeTask = objectMapper.readValue(resourceAsStream, StdActiveTask.class);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
		}
		return activeTask;
	}


}
