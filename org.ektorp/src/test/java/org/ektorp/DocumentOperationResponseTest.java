package org.ektorp;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.*;

public class DocumentOperationResponseTest {

	@Test
	public void read_from_json() throws Exception {
		ObjectMapper om = new ObjectMapper();
		TypeReference<List<DocumentOperationResult>> tr = new TypeReference<List<DocumentOperationResult>>(){};

        List<DocumentOperationResult> result;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getResourceAsStream("document_operation_response.json");
            result = om.readValue(resourceAsStream, tr);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        assertEquals(3, result.size());

		assertTrue(result.get(0).isErroneous());
		assertEquals("0", result.get(0).getId());
		assertEquals("conflict", result.get(0).getError());
		assertEquals("Document update conflict.", result.get(0).getReason());

		assertEquals("1", result.get(1).getId());
		assertEquals("2-1579510027", result.get(1).getRevision());

		assertEquals("2", result.get(2).getId());
		assertEquals("2-3978456339", result.get(2).getRevision());
	}

}
