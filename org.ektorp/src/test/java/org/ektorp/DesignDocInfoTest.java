package org.ektorp;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.*;

import java.io.InputStream;


public class DesignDocInfoTest {


	@Test
	public void test_from_json() throws Exception {
		ObjectMapper om = new ObjectMapper();
        DesignDocInfo info;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getResourceAsStream("design_doc_info.json");
            info = om.readValue(resourceAsStream, DesignDocInfo.class);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
		assertEquals("exampleDesignDoc", info.getName());

		DesignDocInfo.ViewIndex idx = info.getViewIndex();
		assertEquals("javascript", idx.getLanguage());
		assertEquals(12398, idx.getDiskSize());
		assertTrue(idx.isUpdaterRunning());
		assertTrue(idx.isCompactRunning());
		assertTrue(idx.isWaitingCommit());
		assertEquals(2, idx.getWaitingClients());
		assertEquals(14, idx.getUpdateSeq());
		assertEquals(1, idx.getPurgeSeq());
		assertEquals("a5867518b6853fbfdf01dfe72b3b343d", idx.getSignature());
	}

}
