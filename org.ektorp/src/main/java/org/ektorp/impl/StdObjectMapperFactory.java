package org.ektorp.impl;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.ektorp.CouchDbConnector;
import org.ektorp.impl.jackson.EktorpJacksonModule;
import org.ektorp.util.Assert;

/**
 *
 * @author henrik lundgren
 *
 */
public class StdObjectMapperFactory implements ObjectMapperFactory {

	private ObjectMapper instance;
	private boolean writeDatesAsTimestamps = false;

	public synchronized ObjectMapper createObjectMapper() {
		ObjectMapper result = instance;
		if (result == null) {
			result = new ObjectMapper();
			applyDefaultConfiguration(result);
			instance = result;
		}
		return result;
	}

	public ObjectMapper createObjectMapper(CouchDbConnector connector) {
		ObjectMapper objectMapper = new ObjectMapper();
		applyDefaultConfiguration(objectMapper);
		objectMapper.registerModule(new EktorpJacksonModule(connector, objectMapper));
		return objectMapper;
	}

	public synchronized void setObjectMapper(ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		this.instance = om;
	}

	public void setWriteDatesAsTimestamps(boolean b) {
		this.writeDatesAsTimestamps = b;
	}

	private void applyDefaultConfiguration(ObjectMapper om) {
		om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, this.writeDatesAsTimestamps);
		om.getSerializationConfig().withSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

}
