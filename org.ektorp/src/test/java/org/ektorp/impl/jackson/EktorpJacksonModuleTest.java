package org.ektorp.impl.jackson;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.CouchDbConnector;
import org.ektorp.docref.CascadeType;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.support.CouchDbDocument;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("serial")
public class EktorpJacksonModuleTest {

	EktorpJacksonModule module;
	CouchDbConnector db = mock(CouchDbConnector.class);
	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setup() {
		module = new EktorpJacksonModule(db, mapper);
		mapper.registerModule(module);
	}

	@Test
	public void property_annotated_with_DocRef_should_not_be_serialized() throws Exception {
		ParentDoc p = new ParentDoc();
		p.setId("id");
		p.setRevision("rev");

		p.addChild(new ChildDoc("cid","crev"));

		String json = mapper.writeValueAsString(p);
		assertFalse("children field should be absent from json", json.matches(".*children.*"));
	}

	@Test
	public void cascading_property_should_be_saved_separately() throws Exception {
		ParentDocWithCascade p = new ParentDocWithCascade();
		p.setId("id");
		p.setRevision("rev");

		p.addChild(new ChildDoc("cid","crev"));

		String json = mapper.writeValueAsString(p);
		assertFalse("children field should be absent from json", json.matches(".*children.*"));
		verify(db).executeBulk(any(Collection.class));
	}

	public static class ParentDoc extends CouchDbDocument {

		@DocumentReferences(backReference = "parentId")
		Set<ChildDoc> children;

		public Set<ChildDoc> getChildren() {
			return children;
		}

		public void setChildren(Set<ChildDoc> children) {
			this.children = children;
		}

		public void addChild(ChildDoc c) {
			if (children == null) {
				children = new LinkedHashSet<EktorpJacksonModuleTest.ChildDoc>();
			}
			children.add(c);
		}
	}

	public static class ParentDocWithCascade extends CouchDbDocument {

		@DocumentReferences(backReference = "parentId", cascade = CascadeType.ALL)
		Set<ChildDoc> children;

		public Set<ChildDoc> getChildren() {
			return children;
		}

		public void setChildren(Set<ChildDoc> children) {
			this.children = children;
		}

		public void addChild(ChildDoc c) {
			if (children == null) {
				children = new LinkedHashSet<EktorpJacksonModuleTest.ChildDoc>();
			}
			children.add(c);
		}
	}

	public static class ChildDoc extends CouchDbDocument {

		private String parentId;

		@JsonCreator
		public ChildDoc(@JsonProperty("_id") String id, @JsonProperty("_rev") String rev) {
			setId(id);
			setRevision(rev);
		}

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}


	}

}
