package org.ektorp.impl.docref;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.introspect.*;
import org.codehaus.jackson.map.ser.*;
import org.codehaus.jackson.map.type.*;
import org.ektorp.*;
import org.ektorp.docref.*;
import org.ektorp.util.*;

public class BackReferencedBeanSerializer extends JsonSerializer<Object> {

	private final JsonSerializer<Object> delegate;
	private final List<BeanPropertyWriter> documentReferenceFields;
	private final CouchDbConnector couchDbConnector;

	public BackReferencedBeanSerializer(JsonSerializer<Object> delegate,
			List<BeanPropertyWriter> list, CouchDbConnector couchDbConnector) {
		this.delegate = delegate;
		this.documentReferenceFields = list;
		this.couchDbConnector = couchDbConnector;
	}

	@Override
	public void serialize(Object value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {

		String id = Documents.getId(value);
		Set<Object> docsToSave = new LinkedHashSet<Object>();
		try {

			BasicBeanDescription beanDesc = provider.getConfig().introspect(
					TypeFactory.type(value.getClass()));

			for (BeanPropertyWriter writer : documentReferenceFields) {
				if (!cascadeUpdates(writer.getName(), value)) { continue; }
				Object o = writer.get(value);
				findDocumentsToSave(value, id, docsToSave, beanDesc, writer, o);
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

	private void findDocumentsToSave(Object value, String id,
			Set<Object> docsToSave, BasicBeanDescription beanDesc,
			BeanPropertyWriter writer, Object o) {
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
