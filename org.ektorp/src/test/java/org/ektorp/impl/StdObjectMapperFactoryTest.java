package org.ektorp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StdObjectMapperFactoryTest {

	private StdObjectMapperFactory factory;

	@Before
	public void setUp() {
		factory = new StdObjectMapperFactory();
	}

	@Test
	public void shouldSerializeBeanPropertiesByDefault() throws JsonProcessingException {
		ObjectMapper objectMapper = factory.createObjectMapper();

		String actual = objectMapper.writeValueAsString(new Pair("couch", "db"));

		assertEquals("{\"name\":\"couch\",\"value\":\"db\"}", actual);
	}

	@Test
	public void shouldNotSerializeNullBeanPropertiesByDefault() throws JsonProcessingException {
		ObjectMapper objectMapper = factory.createObjectMapper();

		String actual = objectMapper.writeValueAsString(new Pair("couch", null));

		assertEquals("{\"name\":\"couch\"}", actual);
	}

	public static class Pair {
		private String name;
		private String value;

		public Pair(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}
}
