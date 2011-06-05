package org.ektorp.impl.docref;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentOperationResult;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.util.Exceptions;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;
/**
 * 
 * @author ragnar rova
 * @author henrik lundgren
 *
 * @param <T>
 */
public class DocumentReferenceSerializer extends JsonSerializer<Object> {

	private final CouchDbConnector couchDbConnector;
	private final JsonSerializer<Object> delegate;
	
	public DocumentReferenceSerializer(CouchDbConnector db, JsonSerializer<Object> delegate) {
		this.couchDbConnector = db;
		this.delegate = delegate;
	}

	@Override
	public void serialize(final Object value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		try {
			final Set<Object> docsToSave = new HashSet<Object>();
			
			ReflectionUtils.eachField(value.getClass(), new Predicate<Field>() {
				@Override
				public boolean apply(Field input) {
					DocumentReferences referenceMetaData = input.getAnnotation(DocumentReferences.class);
					if (referenceMetaData != null) {
						if (Set.class.isAssignableFrom(input.getType())) {
							try {
								input.setAccessible(true);
								Collection<?> d = findDocumentsToSave((Set<?>) input.get(value));
								docsToSave.addAll(d);	
							} catch (IllegalAccessException e) {
								throw Exceptions.propagate(e);
							}
						} else {
							throw new DbAccessException("@DocumentReferences can only be used on java.util.Set");
						}
					}
					return false;
				}
			});
			
			if (!docsToSave.isEmpty()) {
				List<DocumentOperationResult> res = couchDbConnector
						.executeBulk(docsToSave);
				if (res.size() > 0) {
					throwBulkUpdateError(res);
				}
			}
			delegate.serialize(value, jgen, provider);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
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

	private Set<?> findDocumentsToSave(Set<?> o) {
		if (o == null) {
			return Collections.emptySet();
		}
		Set<Object> docsToSave = new LinkedHashSet<Object>();
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
		return docsToSave;
	}
}
