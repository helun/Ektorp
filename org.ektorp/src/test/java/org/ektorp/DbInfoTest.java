package org.ektorp;

import static org.junit.Assert.*;

import org.junit.Test;

public class DbInfoTest {

	@Test
	public void given_updateSeq_is_numeric_then_isUpdateSeqNumeric_should_return_true() {
		DbInfo info = new DbInfo("test");
		info.updateSeq = "1234";
		assertTrue(info.isUpdateSeqNumeric());
	}
	
	@Test
	public void given_updateSeq_is_not_numeric_then_isUpdateSeqNumeric_should_return_false() {
		DbInfo info = new DbInfo("test");
		info.updateSeq = "dqwEFWEGRGQ34Q";
		assertFalse(info.isUpdateSeqNumeric());
	}

}
