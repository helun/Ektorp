package org.ektorp.support;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.map.*;
import org.ektorp.util.*;
import org.junit.*;

public class OpenCouchDbDocumentTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAnonymous() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String orgJson = IOUtils.toString(getClass().getResourceAsStream("open_doc.json"), "UTF-8");
		FlexDoc d = mapper.readValue(orgJson, FlexDoc.class);
		assertEquals("doc_id", d.getId());
		assertEquals("unknown", d.getAnonymous().get("mysteryStringField"));
		assertEquals(12345, d.getAnonymous().get("mysteryIntegerField"));
		
		Map<String, Object> mysteryObject = (Map<String, Object>) d.getAnonymous().get("mysteryObjectField");
		assertEquals("foo", mysteryObject.get("nestedField1"));
		assertEquals("bar", mysteryObject.get("nestedField2"));
		
		List<String> mysteryList = (List<String>) d.getAnonymous().get("mysteryArrayField");
		assertEquals(3, mysteryList.size());
		assertEquals("a1", mysteryList.get(0));
		
		String newJson = mapper.writeValueAsString(d);
		
		assertTrue(JSONComparator.areEqual(newJson, orgJson));
	}

	@SuppressWarnings("serial")
	static class FlexDoc extends OpenCouchDbDocument {
		
		private String name;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
	}
}
