package org.ektorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ViewQueryCloneTest {


	@Test
	public void cloneShouldCopyStartRawKey() throws IOException {
		String inputRawKey = "\"hello\"";
		Object expectedResult = new ObjectMapper().readTree(inputRawKey);
		ViewQuery q = new ViewQuery();
		q.rawStartKey(inputRawKey);
		assertEquals(expectedResult, q.getStartKey());
		ViewQuery clone = q.clone();
		assertEquals(expectedResult, clone.getStartKey());
	}

	@Test
	public void cloneShouldCopyEndRawKey() throws IOException {
		String inputRawKey = "\"hello\"";
		Object expectedResult = new ObjectMapper().readTree(inputRawKey);
		ViewQuery q = new ViewQuery();
		q.rawEndKey(inputRawKey);
		assertEquals(expectedResult, q.getEndKey());
		ViewQuery clone = q.clone();
		assertEquals(expectedResult, clone.getEndKey());
	}

	@Test
	public void cloneShouldCopyRawKey() throws IOException {
		String inputRawKey = "\"hello\"";
		Object expectedResult = new ObjectMapper().readTree(inputRawKey);
		ViewQuery q = new ViewQuery();
		q.rawKey(inputRawKey);
		assertEquals(expectedResult, q.getKey());
		ViewQuery clone = q.clone();
		assertEquals(expectedResult, clone.getKey());
	}

	@Test
	public void cloneShouldCopyStartKey() {
		Object inputKeyObject = "hello";
		ViewQuery q = new ViewQuery();
		q.startKey(inputKeyObject);
		assertEquals(inputKeyObject, q.getStartKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getStartKey());
	}

	@Test
	public void cloneShouldCopyEndKey() {
		Object inputKeyObject = "hello";
		ViewQuery q = new ViewQuery();
		q.endKey(inputKeyObject);
		assertEquals(inputKeyObject, q.getEndKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getEndKey());
	}

	@Test
	public void cloneShouldCopyKey() {
		Object inputKeyObject = "hello";
		ViewQuery q = new ViewQuery();
		q.key(inputKeyObject);
		assertEquals(inputKeyObject, q.getKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getKey());
	}

	@Test
	public void cloneShouldCopyStartKeyWithSameInstance() {
		Object inputKeyObject = new Object();
		ViewQuery q = new ViewQuery();
		q.startKey(inputKeyObject);
		assertEquals(inputKeyObject, q.getStartKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getStartKey());
		assertSame(inputKeyObject, clone.getStartKey());
	}

	@Test
	public void cloneShouldCopyEndKeyWithSameInstance() {
		Object inputKeyObject = new Object();
		ViewQuery q = new ViewQuery();
		q.endKey(inputKeyObject);
		assertEquals(inputKeyObject, q.getEndKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getEndKey());
		assertSame(inputKeyObject, clone.getEndKey());
	}

	@Test
	public void cloneShouldCopyKeyWithSameInstance() {
		Object inputKeyObject = new Object();
		ViewQuery q = new ViewQuery();
		q.key(inputKeyObject);
		assertEquals(inputKeyObject, q.getKey());
		ViewQuery clone = q.clone();
		assertEquals(inputKeyObject, clone.getKey());
		assertSame(inputKeyObject, clone.getKey());
	}


}
