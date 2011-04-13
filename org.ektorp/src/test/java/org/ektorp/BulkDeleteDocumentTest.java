package org.ektorp;

import static org.junit.Assert.*;

import org.codehaus.jackson.map.*;
import org.junit.*;

public class BulkDeleteDocumentTest {

	@Test
	public void testToJson() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		BulkDeleteDocument bd = new BulkDeleteDocument("0", "r0");
		String json = mapper.writeValueAsString(bd);
		assertEquals("{\"_id\":\"0\",\"_rev\":\"r0\",\"_deleted\":true}", json);
	}

}
