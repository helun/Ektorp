package org.ektorp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.CouchDbConnector;

public class CachingObjectMapperFactory implements ObjectMapperFactory {

	private final ObjectMapperFactory delegate;

	private ObjectMapper objectMapperInstance;

	public CachingObjectMapperFactory(ObjectMapperFactory delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public ObjectMapper createObjectMapper() {
		ObjectMapper result = objectMapperInstance;
		if (result == null) {
			result = delegate.createObjectMapper();
			objectMapperInstance = result;
		}
		return result;
	}

	@Override
	public ObjectMapper createObjectMapper(CouchDbConnector connector) {
		throw new UnsupportedOperationException();
	}

}
