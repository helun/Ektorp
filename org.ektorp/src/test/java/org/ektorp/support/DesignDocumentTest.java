package org.ektorp.support;

import static org.junit.Assert.*;

import java.io.*;

import org.apache.commons.io.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.util.*;
import org.junit.*;

public class DesignDocumentTest {

	DesignDocument dd = new DesignDocument("_design/TestDoc");

	@Before
	public void setup() {
		dd.setRevision("12345");
		dd.addView("all", new DesignDocument.View("function(doc) { if (doc.Type == 'TestDoc')  emit(null, doc.id) }"));
		dd.addView("by_lastname", new DesignDocument.View("function(doc) { if (doc.Type == 'TestDoc')  emit(doc.LastName, doc) }"));
		DesignDocument.View view = new DesignDocument.View();
		view.setAnonymous("module", "exports.info = {artiest : {name : 'artiest', properties : {naam : {name : 'naam', type : 'string', required : true, searchable : true}}}}");
		dd.addView("lib", view);
		dd.setLanguage("javascript");
	}

	@Test
	public void should_deserialize_from_design_doc_json_from_db() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		DesignDocument dd = om.readValue(getClass().getResourceAsStream("design_doc.json"), DesignDocument.class);
		assertDeserialization(dd);
	}

	@Test
	public void should_deserialize_with_auto_detect_getters_disabled() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		om.getSerializationConfig().with(MapperFeature.AUTO_DETECT_GETTERS);
		DesignDocument dd = om.readValue(getClass().getResourceAsStream("design_doc.json"), DesignDocument.class);
		assertDeserialization(dd);
	}

	@Test
	public void test_merge() {
		DesignDocument mergeDoc = new DesignDocument();
		mergeDoc.addFilter("filter", "func");
		mergeDoc.addListFunction("list", "func");
		mergeDoc.addShowFunction("show", "func");
		mergeDoc.addView("view", new DesignDocument.View("map"));

		DesignDocument existingDoc = new DesignDocument();
		assertTrue(existingDoc.mergeWith(mergeDoc));
		assertNotNull(existingDoc.getLists().get("list"));
		assertNotNull(existingDoc.getShows().get("show"));
		assertNotNull(existingDoc.getFilters().get("filter"));
		assertNotNull(existingDoc.getViews().get("view"));
	}

	@Test
	public void given_update_on_diff_is_true_then_merge_sould_update_functions() {
		System.setProperty(DesignDocument.UPDATE_ON_DIFF, "true");
		DesignDocument mergeDoc = new DesignDocument();
		mergeDoc.addFilter("filter", "new");
		mergeDoc.addListFunction("list", "new");
		mergeDoc.addShowFunction("show", "new");
		mergeDoc.addView("view", new DesignDocument.View("new"));

		DesignDocument existingDoc = new DesignDocument();
		existingDoc.addFilter("filter", "org");
		existingDoc.addListFunction("list", "org");
		existingDoc.addShowFunction("show", "org");
		existingDoc.addView("view", new DesignDocument.View("org"));

		assertTrue(existingDoc.mergeWith(mergeDoc));
		assertEquals("new", existingDoc.getLists().get("list"));
		assertEquals("new", existingDoc.getShows().get("show"));
		assertEquals("new", existingDoc.getFilters().get("filter"));
		assertEquals("new", existingDoc.getViews().get("view").getMap());
	}

	@Test
	public void given_update_on_diff_is_false_then_merge_sould_not_update_functions() {
		System.setProperty(DesignDocument.UPDATE_ON_DIFF, "false");
		System.setProperty(DesignDocument.AUTO_UPDATE_VIEW_ON_CHANGE, "false");
		DesignDocument mergeDoc = new DesignDocument();
		mergeDoc.addFilter("filter", "new");
		mergeDoc.addListFunction("list", "new");
		mergeDoc.addShowFunction("show", "new");
		mergeDoc.addView("view", new DesignDocument.View("new"));

		DesignDocument existingDoc = new DesignDocument();
		existingDoc.addFilter("filter", "org");
		existingDoc.addListFunction("list", "org");
		existingDoc.addShowFunction("show", "org");
		existingDoc.addView("view", new DesignDocument.View("org"));

		assertFalse(existingDoc.mergeWith(mergeDoc));
		assertEquals("org", existingDoc.getLists().get("list"));
		assertEquals("org", existingDoc.getShows().get("show"));
		assertEquals("org", existingDoc.getFilters().get("filter"));
		assertEquals("org", existingDoc.getViews().get("view").getMap());
	}

	private void assertDeserialization(DesignDocument dd) {
		assertEquals("_design/TestDoc", dd.getId());
		assertTrue(dd.containsView("all"));
		assertTrue(dd.containsView("by_lastname"));
		assertEquals("javascript", dd.getLanguage());
	}

	@Test
	public void should_serialize_just_fine() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		assertSerialization(om);
	}

	@Test
	public void should_serialize_with_auto_detect_getters_disabled() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		om.getSerializationConfig().with(MapperFeature.AUTO_DETECT_GETTERS);
		assertSerialization(om);
	}

	private void assertSerialization(ObjectMapper om) throws IOException,
			JsonGenerationException, JsonMappingException {
		String json = om.writeValueAsString(dd);
		String expected = IOUtils.toString(getClass().getResourceAsStream("design_doc.json"));
		assertTrue(JSONComparator.areEqual(json, expected));
	}

}
