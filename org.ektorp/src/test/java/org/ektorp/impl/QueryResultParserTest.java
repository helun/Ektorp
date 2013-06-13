package org.ektorp.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.DbAccessException;
import org.ektorp.ViewResultException;
import org.ektorp.support.CouchDbDocument;
import org.junit.Test;

public class QueryResultParserTest {

	QueryResultParser<TestDoc> parser = new QueryResultParser<QueryResultParserTest.TestDoc>(TestDoc.class, new ObjectMapper());

	@Test
	public void test_included_doc() throws Exception {
		parser.parseResult(loadData("view_result_with_included_docs.json"));
		List<TestDoc> result = parser.getRows();
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test
	public void test_doc_in_value_field() throws Exception {
		parser.parseResult(loadData("view_result_with_embedded_docs.json"));
		List<TestDoc> result = parser.getRows();
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test
	public void test_single_row_result() throws Exception {
		parser.parseResult(loadData("view_result_with_single_row.json"));
		List<TestDoc> result = parser.getRows();
		assertEquals(1, result.size());
		assertEquals("doc_id1", result.get(0).getId());
	}

	@Test
	public void test_empty_result_from_reduced_view() throws Exception {
		parser.parseResult(loadData("empty_reduced_view_result.json"));
		List<TestDoc> result = parser.getRows();
		assertTrue(result.isEmpty());
	}

	@Test
	public void test_updateSeq() throws Exception {
		parser.parseResult(loadData("update_seq_view_result.json"));
		List<TestDoc> result = parser.getRows();
		assertEquals(5, result.size());
	}
	
	@Test
	public void given_both_value_and_docs_contains_objects_then_doc_should_be_used() throws Exception {
		parser.parseResult(loadData("all_docs_result.json"));
		List<TestDoc> result = parser.getRows();
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test
	public void first_and_last_keys_and_ids_in_result_should_be_available() throws Exception {
		parser.parseResult(loadData("offset_view_result.json"));
		assertEquals("a2f31cfa68118a6ae9d35444fcb1a3cf", parser.getFirstId());
		assertEquals("Nirvana", parser.getFirstKey().textValue());
		assertEquals("dcdaf08242a4be7da1a36e25f4f0b022", parser.getLastId());
		assertEquals("Silverchair", parser.getLastKey().textValue());
	}

	@Test( expected = ViewResultException.class )
	public void given_view_result_contains_error_then_exception_should_be_thrown() throws Exception {
		parser.parseResult(loadData("view_result_with_error.json"));
	}

	@Test( expected = DbAccessException.class )
	public void given_view_result_contains_error_then_exception_should_be_thrown2() throws Exception {
		parser.parseResult(loadData("erroneous_cloudant_view_result.json"));
	}

	private InputStream loadData(String name) throws JsonParseException, IOException {
		return getClass().getResourceAsStream(name);
	}

	@SuppressWarnings("serial")
	public static class TestDoc extends CouchDbDocument {

		private String name;
		private int age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public int getAge() {
			return age;
		}
	}

}
