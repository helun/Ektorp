package org.ektorp.impl;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.map.*;
import org.ektorp.support.*;
import org.junit.*;

public class StreamingJsonSerializerTest {

	@Test @Ignore
	public void testToJson() throws IOException {
		JsonSerializer js = new StreamingJsonSerializer(new ObjectMapper());
		BulkOperation op = js.createBulkOperation(createTestData(10000), false);
		IOUtils.copy(op.getData(), System.out);
	}
	
	private List<?> createTestData(int size) {
		List<TestDoc> objects = new ArrayList<TestDoc>(size);
		for (int i = 0; i < size; i++) {
			objects.add(new TestDoc("id_" + i, "rev", "name_" + i));
		}
		return objects;
	}
	
	@SuppressWarnings("serial")
	public static class TestDoc extends CouchDbDocument {
		
		String name;
		
		public TestDoc(String id, String rev, String name) {
			setId(id);
			setRevision(rev);
			setName(name);
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
	}

}
