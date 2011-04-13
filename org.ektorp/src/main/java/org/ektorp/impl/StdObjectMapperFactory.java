package org.ektorp.impl;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.deser.*;
import org.ektorp.*;
import org.ektorp.impl.docref.*;
import org.ektorp.util.*;

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

		DocumentReferenceDeserializerFactory dsf;
		try {
			dsf = new DocumentReferenceDeserializerFactory(connector,
					objectMapper);
		} catch (JsonMappingException e) {
			throw Exceptions.propagate(e);
		}
		DeserializerProvider dp = new StdDeserializerProvider(dsf);
		objectMapper.setDeserializerProvider(dp);
		DocumentReferenceSerializerFactory sf = new DocumentReferenceSerializerFactory(
				connector);

		DocumentReferenceSerializerProvider dsp = new DocumentReferenceSerializerProvider(
				objectMapper.getSerializationConfig(), sf,
				objectMapper.getSerializerProvider());

		objectMapper.setSerializerProvider(dsp);
		objectMapper.setSerializerFactory(sf);

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
