package org.ektorp.util;

import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ektorp.*;
import org.junit.*;

public class DocumentsTest {

	TestDocument testDoc_1 = new TestDocument();
	AnnotatedDoc annotatedDoc = new AnnotatedDoc();
	PrivateAnnotatedDoc privAnnotatedDoc = new PrivateAnnotatedDoc();
	ConstructorAnnotatedDoc constDoc = new ConstructorAnnotatedDoc("id_4");
	Map<String, Object> mapDoc = new HashMap<String, Object>();
	JsonNode nodeDoc;

	@Before
	public void setup() {
		testDoc_1.setId("id_1");
		testDoc_1.setRevision("rev_1");

		annotatedDoc.setIdentifikator("id_2");
		annotatedDoc.setRevisjon("rev_2");

		privAnnotatedDoc.setIdentifikator("id_3");
		privAnnotatedDoc.setRevisjon("id_3");

		mapDoc.put("_id", "id_4");
		mapDoc.put("_rev", "rev_4");

		ObjectMapper om = new ObjectMapper();
		ObjectNode onode = om.createObjectNode();
		onode.put("_id", "jsonNode_id");
		onode.put("_rev", "jsonNode_rev");
		onode.put("otherField", "otherValue");
		nodeDoc = onode;
	}

	@Test
	public void getId_should_return_id() {
		assertEquals("id_1", Documents.getId(testDoc_1));
	}

	@Test
	public void id_in_constructor_should_not_cause_error() {
		assertEquals("id_4", Documents.getId(constDoc));
	}

	@Test(expected = InvalidDocumentException.class)
	public void invalid_doc_should_thow_exception() {
		Documents.getId(new InvalidDoc());
	}

	@Test
	public void given_id_in_constructor_when_setting_id_should_fail_silent() {
		Documents.setId(constDoc, "new_id");
	}

	@Test
	public void annotdated_id_should_return_id() {
		assertEquals("id_2", Documents.getId(annotatedDoc));
	}

	@Test
	public void private_annotdated_id_should_return_id() {
		assertEquals("id_3", Documents.getId(privAnnotatedDoc));
	}

	@Test
	public void getId_on_subclass_should_return_id() {
		ExtendedDocument extDoc = new ExtendedDocument();
		extDoc.setId("ext_id");
		assertEquals("ext_id", Documents.getId(extDoc));
	}

	@Test
	public void should_handle_getId_on_map() {
		assertEquals(mapDoc.get("_id"), Documents.getId(mapDoc));
	}

	@Test
	public void should_handle_setId_on_map() {
		Documents.setId(mapDoc, "new_id");
		assertEquals("new_id", mapDoc.get("_id"));
	}

	@Test
	public void should_handle_getRev_on_map() {
		assertEquals(mapDoc.get("_rev"), Documents.getRevision(mapDoc));
	}

	@Test
	public void should_handle_isNew_on_map() {
		mapDoc.remove("_rev");
		assertTrue(Documents.isNew(mapDoc));
	}

	@Test
	public void should_handle_setRev_on_map() {
		Documents.setRevision(mapDoc, "new_rev");
		assertEquals("new_rev", mapDoc.get("_rev"));
	}

	@Test
	public void should_handle_setId_on_JsonNode() {
		Documents.setId(nodeDoc, "new_id");
		assertEquals("new_id", nodeDoc.get("_id").textValue());
	}

	@Test
	public void should_handle_setRev_on_JsonNode() {
		Documents.setRevision(nodeDoc, "new_rev");
		assertEquals("new_rev", nodeDoc.get("_rev").textValue());
	}

	@Test
	public void should_handle_getRev_on_JsonNode() {
		assertEquals("jsonNode_rev", Documents.getRevision(nodeDoc));
	}

	@Test
	public void should_handle_isNew_onJsonNode() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode onode = om.createObjectNode();
		onode.put("_id", "jsonNode_id");
		onode.put("otherField", "otherValue");
		assertTrue(Documents.isNew(onode));
	}

	@Test
	public void should_handle_getId_on_JsonNode() {
		assertEquals("jsonNode_id", Documents.getId(nodeDoc));
	}

	@Test
	public void setId_should_set_id() {
		Documents.setId(testDoc_1, "new_id");
		assertEquals("new_id",testDoc_1.getId());
	}

	@Test
	public void getRevision_should_return_rev() {
		assertEquals("rev_1", Documents.getRevision(testDoc_1));
		assertFalse(Documents.isNew(testDoc_1));
	}

	@Test
	public void annotated_revision_should_return_rev() {
		assertEquals("rev_2", Documents.getRevision(annotatedDoc));
		assertFalse(Documents.isNew(annotatedDoc));
	}

	@Test
	public void null_revision_should_be_considered_new() {
		testDoc_1.setRevision(null);
		assertTrue(Documents.isNew(testDoc_1));
	}

	@Test
	public void setRevision_should_set_rev() {
		Documents.setRevision(testDoc_1, "new_rev");
		assertEquals("new_rev",testDoc_1.getRevision());
	}

	@Test
	public void test_annotated_fields() {
		FieldAnnotatedDoc fd = new FieldAnnotatedDoc();
		assertTrue(Documents.isNew(fd));
		Documents.setId(fd, "myid");
		assertEquals("myid", fd.id);
		Documents.setRevision(fd, "myrev");
		assertEquals("myrev", fd.rev);
		assertEquals(fd.id, Documents.getId(fd));
		assertEquals(fd.rev, Documents.getRevision(fd));
	}

	public static class TestDocument {

		private String id;
		private String revision;
		private String name;

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getRevision() {
			return revision;
		}
		public void setRevision(String revision) {
			this.revision = revision;
		}
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class AnnotatedDoc {

		private String identifikator;
		private String revisjon;

		@JsonProperty("_id")
		public String getIdentifikator() {
			return identifikator;
		}

		@JsonProperty("_id")
		public void setIdentifikator(String identifikator) {
			this.identifikator = identifikator;
		}

		@JsonProperty("_rev")
		public String getRevisjon() {
			return revisjon;
		}

		@JsonProperty("_rev")
		public void setRevisjon(String revisjon) {
			this.revisjon = revisjon;
		}

	}

	public static class PrivateAnnotatedDoc {

		private String identifikator;
		private String revisjon;

		@JsonProperty("_id")
		public String getIdentifikator() {
			return identifikator;
		}

		@JsonProperty("_id")
		private void setIdentifikator(String identifikator) {
			this.identifikator = identifikator;
		}

		@JsonProperty("_rev")
		public String getRevisjon() {
			return revisjon;
		}

		@JsonProperty("_rev")
		private void setRevisjon(String revisjon) {
			this.revisjon = revisjon;
		}

	}

	public static class ConstructorAnnotatedDoc {

		private String identifikator;
		private String revisjon;

		@JsonCreator
		public ConstructorAnnotatedDoc(@JsonProperty("_id") String id) {
			identifikator = id;
		}

		@JsonProperty("_id")
		public String getIdentifikator() {
			return identifikator;
		}

		@JsonProperty("_rev")
		public String getRevisjon() {
			return revisjon;
		}

		@SuppressWarnings("unused")
		@JsonProperty("_rev")
		private void setRevisjon(String revisjon) {
			this.revisjon = revisjon;
		}

	}

	public static class FieldAnnotatedDoc {
		@JsonProperty("_id")
		private String id;
		@JsonProperty("_rev")
		private String rev;

		public String getId() {
			return id;
		}

		public String getRev() {
			return rev;
		}
	}

	public static class InvalidDoc {

		private String identifikator;
		private String revisjon;

		public String getIdentifikator() {
			return identifikator;
		}

		public void setIdentifikator(String identifikator) {
			this.identifikator = identifikator;
		}

		public String getRevisjon() {
			return revisjon;
		}

		public void setRevisjon(String revisjon) {
			this.revisjon = revisjon;
		}

	}

	public static class ExtendedDocument extends TestDocument {
		public String id;

		public String getId(String s) {
			return id;
		}
	}

}
