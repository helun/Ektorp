package org.ektorp;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;

import org.junit.Test;

public class DbInfoTest {

	@Test
	public void given_updateSeq_is_numeric_then_isUpdateSeqNumeric_should_return_true() throws Exception {
		DbInfo info = new DbInfo("test");
		String json = "{\"update_seq\":123456789123}";
		info.updateSeq = new ObjectMapper().readTree(json).get("update_seq");
		assertTrue(info.isUpdateSeqNumeric());
	}
	
	@Test
	public void given_updateSeq_is_not_numeric_then_isUpdateSeqNumeric_should_return_false() throws Exception {
		DbInfo info = new DbInfo("test");
		String json = "{\"update_seq\":\"dqwEFWEGRGQ34Q\"}";
		info.updateSeq = new ObjectMapper().readTree(json).get("update_seq");
		assertFalse(info.isUpdateSeqNumeric());
	}

	@Test
	public void given_updateSeq_is_array_then_isUpdateSeqNumeric_should_return_false() throws Exception {
		DbInfo info = new DbInfo("test");
		String json = "{\"update_seq\":[123,\"dqwEFWEGRGQ34Q\"]}";
		info.updateSeq = new ObjectMapper().readTree(json).get("update_seq");
		assertFalse(info.isUpdateSeqNumeric());
	}

}
