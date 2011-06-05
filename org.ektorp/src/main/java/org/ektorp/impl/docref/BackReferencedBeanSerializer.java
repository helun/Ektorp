package org.ektorp.impl.docref;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentOperationResult;
import org.ektorp.docref.CascadeType;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;

public class BackReferencedBeanSerializer<T> extends JsonSerializer<T> {

	private final JsonSerializer<T> delegate;
	private final List<BeanPropertyWriter> documentReferenceFields;
	private final CouchDbConnector couchDbConnector;

	public BackReferencedBeanSerializer(JsonSerializer<T> delegate,
			List<BeanPropertyWriter> list, CouchDbConnector couchDbConnector) {
		this.delegate = delegate;
		this.documentReferenceFields = list;
		this.couchDbConnector = couchDbConnector;
	}

	@Override
	public void serialize(T value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {

		Set<Object> docsToSave = new LinkedHashSet<Object>();
		try {

			for (BeanPropertyWriter writer : documentReferenceFields) {
				if (!cascadeUpdates(writer.getName(), value)) { continue; }
				Object o = writer.get(value);
				findDocumentsToSave(docsToSave, o);
			}
			if (docsToSave.size() > 0) {
				List<DocumentOperationResult> res = couchDbConnector
						.executeBulk(docsToSave);
				if (res.size() > 0) {
					throwBulkUpdateError(res);
				}
			}

		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}

		delegate.serialize(value, jgen, provider);

	}

	private boolean cascadeUpdates(final String propertyName, Object value) {
		DocumentReferences referenceMetaData = ReflectionUtils.findAnnotation(value.getClass(), DocumentReferences.class, new Predicate<Field>() {
			public boolean apply(Field input) {
				return propertyName.equals(input.getName());
			}
		});
		for (CascadeType t : referenceMetaData.cascade()) {
			if (CascadeType.PERSIST_TYPES.contains(t)) {
				return true;
			}
		}
		return false;
	}

	private void throwBulkUpdateError(List<DocumentOperationResult> res) {
		StringBuilder sb = new StringBuilder();
		int maxErrors = 10;
		for (DocumentOperationResult docResult : res) {
			if (maxErrors == 0) {
				sb.append(".. " + res.size() + " more ");
				break;
			}
			sb.append(docResult.getId());
			sb.append(" ");
			sb.append(docResult.getError());
			sb.append(" ");
			sb.append(docResult.getReason());
			sb.append(" ");
			maxErrors--;

		}
		throw new DbAccessException(sb.toString());
	}

	private void findDocumentsToSave(Set<Object> docsToSave, Object o) {
		if (o == null) {
			return;
		}
		
		if (Proxy.isProxyClass(o.getClass())
				&& Proxy.getInvocationHandler(o) instanceof ViewBasedCollection) {

			ViewBasedCollection c = (ViewBasedCollection) Proxy
					.getInvocationHandler(o);

			if (c.initialized()) {
				docsToSave.addAll((Collection<?>) o);
				docsToSave.addAll(c.getPendingRemoval());
			}
		} else if (o instanceof Collection && ((Collection<?>) o).size() > 0) {
			docsToSave.addAll((Collection<?>) o);
		}
	}

}
