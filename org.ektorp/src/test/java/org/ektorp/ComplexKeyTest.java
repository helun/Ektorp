package org.ektorp;

import static org.junit.Assert.*;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.ektorp.impl.*;
import org.ektorp.util.*;
import org.junit.*;

public class ComplexKeyTest {
	
	// 1970-01-01T00:00:00.000+0000
	static final String ISO_8601_DATE_FORMAT_REGEX = ".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4}.*";
	
	ObjectMapper mapper = new StdObjectMapperFactory().createObjectMapper();
	
	@Test
	public void testToJson() throws Exception {
		String json = mapper.writeValueAsString(ComplexKey.of(ComplexKey.emptyArray(), Integer.valueOf(2), "fooz", ComplexKey.emptyObject()));
		assertEquals("[[],2,\"fooz\",{}]", json);
	}
	
	@Test
	public void dates_should_be_serialized_as_ISO_string() throws Exception {
		String json = mapper.writeValueAsString(ComplexKey.of(new Date(), Integer.valueOf(2)));		
		assertThat(json, RegexMatcher.matches(ISO_8601_DATE_FORMAT_REGEX));
	}

}
