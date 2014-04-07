package org.ektorp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.RegexMatcher;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	public void testToString() throws Exception {
		String str = ComplexKey.of(ComplexKey.emptyArray(), Integer.valueOf(2), "fooz", ComplexKey.emptyObject()).toString();
		assertEquals("[[],2,\"fooz\",{}]", str);
	}
	
	@Test
	public void dates_should_be_serialized_as_ISO_string() throws Exception {
		String json = mapper.writeValueAsString(ComplexKey.of(new Date(), Integer.valueOf(2)));		
		assertThat(json, RegexMatcher.matches(ISO_8601_DATE_FORMAT_REGEX));
	}

}
