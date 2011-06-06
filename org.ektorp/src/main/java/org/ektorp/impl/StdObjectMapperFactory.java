package org.ektorp.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
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
		if (instance == null) {
			instance = new ObjectMapper();
			applyDefaultConfiguration(instance);
		}
		return instance;
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
		om.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, writeDatesAsTimestamps);
		om.getSerializationConfig().setSerializationInclusion(
				Inclusion.NON_NULL);
	}

}
