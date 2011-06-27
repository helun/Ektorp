package org.ektorp;

import static org.junit.Assert.*;

import java.util.*;

import org.ektorp.util.*;
import org.junit.*;

public class ViewQueryTest {

	// 2011-02-18T22%3A13%3A03.738%2B0000
	static final String URL_ENCODED_ISO_8601_DATE_FORMAT_REGEX = ".*\\d{4}-\\d{2}-\\d{2}T\\d{2}%3A\\d{2}%3A\\d{2}.\\d{3}\\%2B\\d{4}.*";
	
	ViewQuery query = new ViewQuery()
							.dbPath("/somedb/")
							.designDocId("_design/doc")
							.viewName("viewname");
										
	@Test
	public void when_allDocs_is_set_no_design_doc_should_be_appended_to_uri() {
		assertEquals("/somedb/_all_docs", query.allDocs().buildQuery());
	}
	
	@Test
	public void default_to_no_parameters() {
		assertEquals("/somedb/_design/doc/_view/viewname", query.buildQuery());
	}
	
	@Test
	public void string_key() throws Exception {
		String url = query.key("value").buildQuery();
		assertTrue(contains(url, "?key=%22value%22"));
	}
	
	@Test
	public void int_key() throws Exception {
		String url = query.key(123).buildQuery();
		assertTrue(contains(url, "?key=123"));
	}
	
	@Test
	public void float_key() throws Exception {
		String url = query.key(123.5).buildQuery();
		assertTrue(contains(url, "?key=123.5"));
	}
	
	@Test
	public void double_key() throws Exception {
		String url = query.key(123.5d).buildQuery();
		assertTrue(contains(url, "?key=123.5"));
	}
	
	@Test
	public void long_key() throws Exception {
		String url = query.key(Long.MAX_VALUE).buildQuery();
		assertTrue(contains(url, "?key=9223372036854775807"));
	}
	
	@Test
	public void boolean_key() throws Exception {
		String url = query.key(true).buildQuery();
		assertTrue(contains(url, "?key=true"));
	}
	
	@Test
	public void complex_key() throws Exception {
		String url = query.key(ComplexKey.of(2010, 8, 3)).buildQuery();
		assertTrue(contains(url, "?key=%5B2010%2C8%2C3%5D"));
	}
	
	@Test
	public void complex_date_key() throws Exception {
		String url = query.key(ComplexKey.of(new Date(), 3)).buildQuery();
		assertThat(url, RegexMatcher.matches(URL_ENCODED_ISO_8601_DATE_FORMAT_REGEX));
	}
	
	@Test
	public void multiple_keys() {
		assertFalse(query.hasMultipleKeys());
		List<Object> keys = new ArrayList<Object>();
		keys.add("key1");
		keys.add("key2");
		keys.add("key3");
		
		query.keys(keys);
		assertTrue(query.hasMultipleKeys());
		assertEquals("{\"keys\":[\"key1\",\"key2\",\"key3\"]}", query.getKeysAsJson());
	}
	
	@Test
	public void keys_should_encode_strings_properly() {
		ViewQuery.Keys keys = ViewQuery.Keys.of("key1", "key2", "key3");
		assertEquals("{\"keys\":[\"key1\",\"key2\",\"key3\"]}", keys.toJson());
	}
	
	@Test
	public void keys_should_encode_ints_properly() {
		ViewQuery.Keys keys = ViewQuery.Keys.of(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
		assertEquals("{\"keys\":[1,2,3]}", keys.toJson());
	}
	
	@Test
	public void keys_should_encode_booleans_properly() {
		ViewQuery.Keys keys = ViewQuery.Keys.of(Boolean.TRUE, Boolean.FALSE);
		assertEquals("{\"keys\":[true,false]}", keys.toJson());
	}
	
	@Test
	public void keys_should_encode_objects_properly() {
		List<String> key1 = Arrays.asList("foo1","bar1");
		List<String> key2 = Arrays.asList("foo2","bar2");
		ViewQuery.Keys keys = ViewQuery.Keys.of(key1, key2);
		assertEquals("{\"keys\":[[\"foo1\",\"bar1\"],[\"foo2\",\"bar2\"]]}", keys.toJson());
	}
	
	@Test
	public void given_key_is_json_then_key_parameter_should_be_unchanged() throws Exception {
		String url = query.key("\"value\"").buildQuery();
		assertTrue(contains(url, "?key=%22value%22"));
	}
	
	@Test
	public void startKey_parameter_added() {
		String url = query.startKey("value").buildQuery();
		assertTrue(contains(url, "?startkey=%22value%22"));
	}
	
	@Test
	public void startKey_and_endKey_parameter_added() {
		String url = query
			.startKey("start")
			.endKey("end")
			.buildQuery();
		assertTrue(contains(url, "?startkey=%22start%22"));
		assertTrue(contains(url, "&endkey=%22end%22"));
	}
	
	@Test
	public void int_start_end_key() throws Exception {
		String url = query.startKey(123).endKey(321).buildQuery();
		assertTrue(contains(url, "?startkey=123&endkey=321"));
	}
	
	@Test
	public void float_start_end_key() throws Exception {
		String url = query.startKey(0.0).endKey(321.9).buildQuery();
		assertTrue(contains(url, "?startkey=0.0&endkey=321.9"));
	}
	
	@Test
	public void double_start_end_key() throws Exception {
		String url = query.startKey(0.0d).endKey(321.9d).buildQuery();
		assertTrue(contains(url, "?startkey=0.0&endkey=321.9"));
	}
	
	@Test
	public void long_start_end_key() throws Exception {
		String url = query.startKey(0).endKey(Long.MAX_VALUE).buildQuery();
		assertTrue(contains(url, "?startkey=0&endkey=9223372036854775807"));
	}
	
	@Test
	public void boolean_start_end_key() throws Exception {
		String url = query.startKey(true).endKey(false).buildQuery();
		assertTrue(contains(url, "?startkey=true&endkey=false"));
	}
	
	@Test
	public void complex_start_end_key() throws Exception {
		String url = query.startKey(ComplexKey.of(2010, 8, 3)).endKey(ComplexKey.of(2010, 12, 24)).buildQuery();
		assertTrue(contains(url, "?startkey=%5B2010%2C8%2C3%5D&endkey=%5B2010%2C12%2C24%5D"));
	}
	
	@Test
	public void startDoc_parameter_added() {
		String url = query
			.startDocId("start_dic_id")
			.buildQuery();
		assertTrue(contains(url, "?startkey_docid=start"));
	}
	
	@Test
	public void include_docs_parameter_added() {
		String url = query
			.includeDocs(true)
			.buildQuery();
		assertTrue(contains(url, "?include_docs=true"));
	}
	
	@Test
	public void stale_ok_parameter_added() {
		String url = query
			.staleOk(true)
			.buildQuery();
		assertTrue(contains(url, "?stale=ok"));
	}
	
	@Test
	public void stale_ok_update_after() {
		String url = query
			.staleOkUpdateAfter()
			.buildQuery();
		assertTrue(contains(url, "?stale=update_after"));
	}
	
	@Test
	public void reduce_parameter_added() {
		String url = query
			.reduce(false)
			.buildQuery();
		assertTrue(contains(url, "?reduce=false"));
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_dbName_is_missing() {
		new ViewQuery()
//			.dbPath("/somedb/")
			.designDocId("_design/doc")
			.viewName("viewname")
			.buildQuery();
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_designDocId_is_missing() {
		new ViewQuery()
			.dbPath("/somedb/")
//			.designDocId("_design/doc")
			.viewName("viewname")
			.buildQuery();
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_viewName_is_missing() {
		new ViewQuery()
			.dbPath("/somedb/")
			.designDocId("_design/doc")
//			.viewName("viewname")
			.buildQuery();
	}

	@Test
	public void viewQuery_used_to_access_list_function() {
		String url = new ViewQuery()
						.dbPath("/db/")
						.designDocId("_design/examples")
						.listName("index-posts")
						.viewName("posts-by-tag")
						.buildQuery();
		
		assertEquals("/db/_design/examples/_list/index-posts/posts-by-tag", url);
	}
	
	@Test
	public void call_list_function_with_params() {
		String url = new ViewQuery()
						.dbPath("/db/")
						.designDocId("_design/examples")
						.listName("index-posts")
						.viewName("posts-by-tag")
						.queryParam("param1", "value1")
						.queryParam("param2", "val/ue")
						.buildQuery();
		
		assertEquals("/db/_design/examples/_list/index-posts/posts-by-tag?param1=value1&param2=val%2Fue", url);
	}
	
	private boolean contains(String subject, String s) {
		return subject.indexOf(s) > -1;
	}
	
}
