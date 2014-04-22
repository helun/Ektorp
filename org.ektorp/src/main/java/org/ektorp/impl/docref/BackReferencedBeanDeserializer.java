package org.ektorp.impl.docref;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.docref.FetchType;
import org.ektorp.util.Documents;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 *
 * @author ragnar rova
 *
 */
public class BackReferencedBeanDeserializer extends StdDeserializer<Object>
		implements ResolvableDeserializer {

	private final CouchDbConnector couchDbConnector;
	private final BeanDeserializer delegate;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
	private final List<ConstructibleAnnotatedCollection> backReferencedFields;

	private final Class<?> clazz;

	public BackReferencedBeanDeserializer(BeanDeserializer deserializer,
			List<ConstructibleAnnotatedCollection> fields,
			CouchDbConnector couchDbConnector, Class<?> clazz) {
		super(clazz);
		this.clazz = clazz;
		this.delegate = deserializer;
		this.couchDbConnector = couchDbConnector;
		this.backReferencedFields = fields;
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
							clazz, ann, constructibleField);
					handler.initialize();
				} else {
					handler = new LazyLoadingViewBasedCollection(id,
							couchDbConnector, clazz, ann, constructibleField);

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

	@Override
	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		delegate.resolve(ctxt);
	}

}
