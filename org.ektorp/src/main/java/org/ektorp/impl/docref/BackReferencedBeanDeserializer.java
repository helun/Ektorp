package org.ektorp.impl.docref;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.deser.*;
import org.ektorp.*;
import org.ektorp.docref.*;
import org.ektorp.util.*;

/**
 *
 * @author ragnar rova
 *
 */
public class BackReferencedBeanDeserializer extends StdDeserializer<Object>
		implements ResolvableDeserializer {

	private final CouchDbConnector couchDbConnector;
	private final BeanDeserializer delegate;
	private final List<ConstructibleAnnotatedCollection> backReferencedFields;
	private final Class<?> clazz;
	private final ObjectMapper objectMapper;

	public BackReferencedBeanDeserializer(BeanDeserializer deserializer,
			List<ConstructibleAnnotatedCollection> fields,
			CouchDbConnector couchDbConnector, Class<?> clazz,
			ObjectMapper objectMapper) {
		super(clazz);
		this.clazz = clazz;
		this.delegate = deserializer;
		this.couchDbConnector = couchDbConnector;
		this.backReferencedFields = fields;
		this.objectMapper = objectMapper;
	}

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		Object deserializedObject = delegate.deserialize(jp, ctxt);
		addbackReferencedFields(deserializedObject, ctxt);
		return deserializedObject;
	}

	private void addbackReferencedFields(Object deserializedObject,
			DeserializationContext ctxt) throws IOException {
		String id = Documents.getId(deserializedObject);

		for (ConstructibleAnnotatedCollection constructibleField : this.backReferencedFields) {
			DocumentReferences ann = constructibleField.getField()
					.getAnnotation(DocumentReferences.class);
			try {

				ViewBasedCollection handler;

				if (ann.fetch().equals(FetchType.EAGER)) {
					handler = new ViewBasedCollection(id, couchDbConnector,
							clazz, ann, constructibleField, objectMapper);
					handler.initialize();
				} else {
					handler = new LazyLoadingViewBasedCollection(id,
							couchDbConnector, clazz, ann, constructibleField,
							objectMapper);

				}

				Object o = Proxy.newProxyInstance(constructibleField
						.getCollectionType().getRawClass().getClassLoader(),
						new Class[] { constructibleField.getCollectionType()
								.getRawClass() }, handler);
				constructibleField.getSetter().set(deserializedObject, o);
			} catch (Exception e) {
				throw new IOException(
						"Failed creating reflection proxy for collection "
								+ constructibleField, e);
			}

		}
	}

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt,
			Object intoValue) throws IOException, JsonProcessingException {
		Object deserializedObject = super.deserialize(jp, ctxt, intoValue);
		addbackReferencedFields(deserializedObject, ctxt);
		return deserializedObject;
	}

	public void resolve(DeserializationConfig config,
			DeserializerProvider provider) throws JsonMappingException {
		delegate.resolve(config, provider);
	}

}
