package org.ektorp.impl.docref;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.schema.*;
import org.codehaus.jackson.type.*;

public class DocumentReferenceSerializerProvider extends SerializerProvider {

	DocumentReferenceSerializerFactory documentReferenceSerializerFactory;

	public DocumentReferenceSerializerProvider(SerializationConfig config,
			DocumentReferenceSerializerFactory dsf, SerializerProvider provider) {
		super(config);
		delegate = provider;
		this.documentReferenceSerializerFactory = dsf;
	}

	private SerializerProvider delegate;

	public final void serializeValue(SerializationConfig config,
			JsonGenerator jgen, Object value, SerializerFactory jsf)
			throws IOException, JsonGenerationException {
		if (documentReferenceSerializerFactory.hasDocumentReferenceFields(
				config, value)) {
			SerializationConfig localConfig = config.createUnshared(null,
					config.getDefaultVisibilityChecker(),
					config.getSubtypeResolver());

			localConfig.setSerializationView(String.class);
			delegate.serializeValue(localConfig, jgen, value, jsf);
		} else {
			delegate.serializeValue(config, jgen, value, jsf);
		}
	}

	public final void serializeValue(SerializationConfig config,
			JsonGenerator jgen, Object value, JavaType rootType,
			SerializerFactory jsf) throws IOException, JsonGenerationException {
		if (documentReferenceSerializerFactory.hasDocumentReferenceFields(
				config, value)) {
			SerializationConfig localConfig = config.createUnshared(null,
					config.getDefaultVisibilityChecker(),
					config.getSubtypeResolver());

			localConfig.setSerializationView(String.class);
			delegate.serializeValue(localConfig, jgen, value, rootType, jsf);
		} else {
			delegate.serializeValue(config, jgen, value, rootType, jsf);
		}
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public JsonSchema generateJsonSchema(Class<?> type,
			SerializationConfig config, SerializerFactory jsf)
			throws JsonMappingException {
		return delegate.generateJsonSchema(type, config, jsf);
	}

	public boolean hasSerializerFor(SerializationConfig cfg, Class<?> cls,
			SerializerFactory jsf) {
		return delegate.hasSerializerFor(cfg, cls, jsf);
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public JsonSerializer<Object> findValueSerializer(Class<?> runtimeType,
			BeanProperty property) throws JsonMappingException {
		return delegate.findValueSerializer(runtimeType, property);
	}

	public JsonSerializer<Object> findValueSerializer(
			JavaType serializationType, BeanProperty property)
			throws JsonMappingException {
		return delegate.findValueSerializer(serializationType, property);
	}

	public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType,
			boolean cache, BeanProperty property) throws JsonMappingException {
		return delegate.findTypedValueSerializer(valueType, cache, property);
	}

	public JsonSerializer<Object> findTypedValueSerializer(JavaType valueType,
			boolean cache, BeanProperty property) throws JsonMappingException {
		return delegate.findTypedValueSerializer(valueType, cache, property);
	}

	public JsonSerializer<Object> getKeySerializer(JavaType keyType,
			BeanProperty property) throws JsonMappingException {
		return delegate.getKeySerializer(keyType, property);
	}

	public String toString() {
		return delegate.toString();
	}

	public JsonSerializer<Object> getNullKeySerializer() {
		return delegate.getNullKeySerializer();
	}

	public JsonSerializer<Object> getNullValueSerializer() {
		return delegate.getNullValueSerializer();
	}

	public JsonSerializer<Object> getUnknownTypeSerializer(Class<?> unknownType) {
		return delegate.getUnknownTypeSerializer(unknownType);
	}

	public void defaultSerializeDateValue(long timestamp, JsonGenerator jgen)
			throws IOException, JsonProcessingException {
		delegate.defaultSerializeDateValue(timestamp, jgen);
	}

	public void defaultSerializeDateValue(Date date, JsonGenerator jgen)
			throws IOException, JsonProcessingException {
		delegate.defaultSerializeDateValue(date, jgen);
	}

	public int cachedSerializersCount() {
		return delegate.cachedSerializersCount();
	}

	public void flushCachedSerializers() {
		delegate.flushCachedSerializers();
	}


}