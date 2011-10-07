package org.ektorp;

import static org.junit.Assert.*;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.junit.*;

public class ViewResultTest {

	ObjectMapper om = new ObjectMapper();
	
	@Test
	public void fromJson() throws Exception {
		ViewResult result = readResult("impl/view_result.json");
		assertEquals(2, result.getSize());
		assertEquals(1, result.getOffset());
		List<ViewResult.Row> rows = result.getRows(); 
		assertEquals("doc_id1", rows.get(0).getId());
		assertEquals("key_value", rows.get(0).getKey());
		assertEquals("doc_value1", rows.get(0).getValue());
		
		assertEquals("doc_id2", rows.get(1).getId());
		assertEquals("key_value", rows.get(1).getKey());
		assertEquals("doc_value2", rows.get(1).getValue());
		
		assertNull(rows.get(0).getDoc());
		assertTrue(rows.get(0).getDocAsNode().isMissingNode());
	}
	
	@Test
	public void test_read_reduce_result() throws Exception {
		ViewResult result = readResult("impl/reduce_view_result.json");
		assertEquals(3, result.getSize());
		List<ViewResult.Row> rows = result.getRows();
		assertEquals(4, rows.get(0).getValueAsInt());
		assertEquals(5, rows.get(1).getValueAsInt());
		assertEquals(6, rows.get(2).getValueAsInt());
		
	}
	
	@Test
	public void int_view_result() throws Exception {
		
		ViewResult result = readResult("impl/int_view_result.json");
		assertEquals(2, result.getSize());
		List<ViewResult.Row> rows = result.getRows();
		assertEquals(123, rows.get(0).getValueAsInt());
		assertEquals(321, rows.get(1).getValueAsInt());
	}
	
	@Test
	public void array_and_object_view_result() throws Exception {
		ViewResult result = readResult("impl/array_and_object_view_result.json");
		assertEquals(2, result.getSize());
		List<ViewResult.Row> rows = result.getRows();
		assertEquals("[\"foo\",\"bar\"]", rows.get(0).getValue());
		assertEquals("{\"foo\":\"bar\"}", rows.get(1).getValue());
	}

	@Test
	public void view_result_with_doc_field() throws Exception {
		ViewResult result = readResult("impl/view_result_with_included_docs.json");
		assertEquals(2, result.getSize());
		List<ViewResult.Row> rows = result.getRows();
		assertEquals("doc_id1", rows.get(0).getDocAsNode().get("_id").getTextValue());
		assertEquals("doc_id2", rows.get(1).getDocAsNode().get("_id").getTextValue());
	}
	
	@Test
	public void complex_key_view_result() throws Exception {
		ViewResult result = readResult("impl/complex_key_view_result.json");
		assertEquals(2, result.getSize());
		List<ViewResult.Row> rows = result.getRows();
		String expectedKey = "[1337,\"key\"]";
		assertEquals(expectedKey, rows.get(0).getKey());
		assertEquals(expectedKey, rows.get(1).getKey());
	}
	
	@Test( expected = ViewResultException.class )
	public void view_result_with_error_row() throws Exception {
		readResult("impl/view_result_with_error.json");
	}

        @Test
        public void int_update_seq_view_result() throws Exception {

                ViewResult result = readResult("impl/view_result_with_int_update_seq.json");
		assertTrue(result.isUpdateSeqNumeric());
                assertEquals(1234, result.getUpdateSeq());
		assertEquals("1234", result.getUpdateSeqAsString());
	}

        @Test
        public void string_update_seq_view_result() throws Exception {

                ViewResult result = readResult("impl/view_result_with_string_update_seq.json");
                assertFalse(result.isUpdateSeqNumeric());
                assertEquals("1234-abc", result.getUpdateSeqAsString());
        }
	
	private ViewResult readResult(String path) throws Exception {
		return new ViewResult(om.readTree(getClass().getResourceAsStream(path)), false);
	}
}
