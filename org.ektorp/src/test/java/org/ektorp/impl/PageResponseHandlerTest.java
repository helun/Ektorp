package org.ektorp.impl;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.Page;
import org.ektorp.PageRequest;
import org.ektorp.support.CouchDbDocument;
import org.junit.Test;

public class PageResponseHandlerTest {

	PageResponseHandler<TestDoc> handler;

	@Test
	public void given_view_result_size_is_smaller_than_page_size_plus_one_then_next_link_should_not_exist() throws Exception {
		handler = new PageResponseHandler<TestDoc>(PageRequest.firstPage(5), TestDoc.class, new ObjectMapper());
		Page<TestDoc> page = handler.success(ResponseOnFileStub.newInstance(200, "offset_view_result.json"));
		assertEquals(5, page.size());
		assertFalse(page.isHasNext());
	}

	@Test
	public void given_view_result_size_is_equal_to_page_size_plus_one_then_next_link_should_exist() throws Exception {
		handler = new PageResponseHandler<TestDoc>(PageRequest.firstPage(4), TestDoc.class, new ObjectMapper());
		Page<TestDoc> page = handler.success(ResponseOnFileStub.newInstance(200, "offset_view_result.json"));
		assertEquals(4, page.size());
		assertTrue(page.isHasNext());
		assertEquals("dcdaf08242a4be7da1a36e25f4f0b022", page.getNextPageRequest().getStartKeyDocId());
	}

	@Test
	public void given_previous_page_request_has_not_been_set_then_hasPrevious_should_be_false() throws Exception {
		handler = new PageResponseHandler<TestDoc>(PageRequest.firstPage(5), TestDoc.class, new ObjectMapper());
		Page<TestDoc> page = handler.success(ResponseOnFileStub.newInstance(200, "offset_view_result.json"));
		assertFalse(page.isHasPrevious());
	}

	@Test
	public void given_previous_page_request_has_been_set_then_hasPrevious_should_be_true() throws Exception {
		PageRequest pr = PageRequest.firstPage(5).getNextPageRequest("key", "docId");
		handler = new PageResponseHandler<TestDoc>(pr, TestDoc.class, new ObjectMapper());

		Page<TestDoc> page = handler.success(ResponseOnFileStub.newInstance(200, "offset_view_result.json"));
		assertTrue(page.isHasPrevious());
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
