package org.ektorp.impl;

import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.*;
import org.ektorp.support.*;
import org.junit.*;

public class EmbeddedDocViewResponseHandlerTest {

	EmbeddedDocViewResponseHandler<TestDoc> handler;

	@Before
	public void setup() {
		handler = new EmbeddedDocViewResponseHandler<EmbeddedDocViewResponseHandlerTest.TestDoc>(TestDoc.class, new ObjectMapper());
	}

	@Test
	public void test_included_doc() throws Exception {
		List<TestDoc> result = handler.success(ResponseOnFileStub.newInstance(202, "view_result_with_included_docs.json"));
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test
	public void test_doc_in_value_field() throws Exception {
		List<TestDoc> result = handler.success(ResponseOnFileStub.newInstance(202, "view_result_with_embedded_docs.json"));
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test
	public void test_single_row_result() throws Exception {
		List<TestDoc> result = handler.success(ResponseOnFileStub.newInstance(202, "view_result_with_single_row.json"));
		assertEquals(1, result.size());
		assertEquals("doc_id1", result.get(0).getId());
	}

	@Test
	public void test_empty_result_from_reduced_view() throws Exception {
		List<TestDoc> result = handler.success(ResponseOnFileStub.newInstance(202, "empty_reduced_view_result.json"));
		assertTrue(result.isEmpty());
	}

	@Test
	public void given_both_value_and_docs_contains_objects_then_doc_should_be_used() throws Exception {
		List<TestDoc> result = handler.success(ResponseOnFileStub.newInstance(202, "all_docs_result.json"));
		assertEquals(2, result.size());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
	}

	@Test( expected = ViewResultException.class )
	public void given_view_result_contains_error_then_exception_should_be_thrown() throws Exception {
		handler.success(ResponseOnFileStub.newInstance(202, "view_result_with_error.json"));
	}

	@Test( expected = DbAccessException.class )
	public void given_view_result_contains_error_then_exception_should_be_thrown2() throws Exception {
		handler.success(ResponseOnFileStub.newInstance(202, "erroneous_cloudant_view_result.json"));
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
