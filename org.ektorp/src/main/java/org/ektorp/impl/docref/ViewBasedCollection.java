package org.ektorp.impl.docref;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.*;
import org.ektorp.*;
import org.ektorp.docref.*;
import org.ektorp.impl.*;
import org.slf4j.*;

public class ViewBasedCollection implements InvocationHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ViewBasedCollection.class);
	
	final String id;
	final CouchDbConnector couchDbConnector;
	final Class<?> clazz;
	final DocumentReferences referenceMetaData;
	final ConstructibleAnnotatedCollection constructibleAnnotatedCollection;
	final Collection<?> collection;
	final ObjectMapper objectMapper;
	private final Collection<BulkDeleteDocument> pendingRemoval = new LinkedHashSet<BulkDeleteDocument>();

	public ViewBasedCollection(String id, CouchDbConnector couchDbConnector,
			Class<?> clazz, DocumentReferences documentReferences,
			ConstructibleAnnotatedCollection constructibleField,
			ObjectMapper objectMapper) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		this.id = id;
		this.couchDbConnector = couchDbConnector;
		this.clazz = clazz;
		this.referenceMetaData = documentReferences;
		this.constructibleAnnotatedCollection = constructibleField;
		this.collection = constructibleField.getConstructor().newInstance();
		this.objectMapper = objectMapper;
	}

	private List<?> loadFromBackReferences(String thisId,
			DocumentReferences ann, CollectionType collectionType,
			String fieldName) throws JsonMappingException {

		try {
			Class<?> raw = collectionType.getRawClass();
			if (Set.class.isAssignableFrom(raw)) {
				ViewQuery query = createBackReferenceQuery(thisId, ann,
						fieldName);
				Class<?> klass = collectionType.getContentType().getRawClass();
				return loadSetResult(query, klass);
			} else {
				throw new DbAccessException("Unsupported back reference collection type: " + raw);
			}

		} catch (DbAccessException e) {
			if (e.getCause() instanceof JsonProcessingException) {
				JsonProcessingException jpe = (JsonProcessingException) e
						.getCause();
				throw JsonMappingException.wrapWithPath(jpe,
						new JsonMappingException.Reference(collectionType
								.getContentType().getRawClass()), fieldName);
			} else {
				throw e;
			}
		}
	}

	private List<?> loadSetResult(ViewQuery query, Class<?> klass) {
		return couchDbConnector.queryView(query, klass);
	}

	private ViewQuery createBackReferenceQuery(String thisId,
			DocumentReferences ann, String fieldName) {
		boolean desc = ann.descendingSortOrder();
		ComplexKey start = ComplexKey.of(thisId, fieldName);
		ComplexKey end = ComplexKey.of(thisId, fieldName,
				ComplexKey.emptyObject());
		if (desc) {
			ComplexKey tmp = start;
			start = end;
			end = tmp;
		}
		
		ViewQuery query = new ViewQuery().designDocId(resolveDesignDocId(ann))
				.viewName(resolveViewName(ann, fieldName))
				.includeDocs(true)
				.descending(ann.descendingSortOrder()).startKey(start)
				.endKey(end);
		return query;
	}

	private String resolveViewName(DocumentReferences ann, String fieldName) {
		return ann.view().length() > 0 ? ann.view() : NameConventions.backReferenceViewName(fieldName);
	}

	private String resolveDesignDocId(DocumentReferences ann) {
		return ann.designDoc().length() > 0 ? ann.designDoc() : NameConventions.designDocName(clazz);
	}

	@SuppressWarnings("unchecked")
	protected void initialize() throws IOException {
		@SuppressWarnings("rawtypes")
		Collection list = loadFromBackReferences(id, referenceMetaData,
				constructibleAnnotatedCollection.getCollectionType(),
				constructibleAnnotatedCollection.getField().getName());
		collection.addAll(list);
	}

	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getName().equals("set")) {
			List<?> list = (List<?>) collection;
			addToPendingRemoval(list.get((Integer) args[1]));
		}
		if (method.getName().equals("remove")) {
			addToPendingRemoval(args[0]);
		}
		if (method.getName().equals("removeAll")) {
			addToPendingRemoval((Collection<? extends Object>) args[0]);
		}
		if (method.getName().equals("retainAll")) {
			addToPendingRemoval(difference(collection, (Collection<?>) args[0]));
		}
		if (method.getName().equals("iterator")) {
			return new RememberRemovedIterator();
		}
		if (method.getName().equals("listIterator")) {
			return new RememberRemovedListIterator();
		} else {
			return method.invoke(collection, args);
		}
	}

	private Collection<? extends Object> difference(Collection<?> c1,
			Collection<?> c2) {
		List<Object> a1 = new ArrayList<Object>(c1);
		List<Object> b1 = new ArrayList<Object>(c1);
		List<Object> a2 = new ArrayList<Object>(c2);
		a1.retainAll(a2);
		b1.removeAll(a1);
		return b1;
	}

	public boolean initialized() {
		return true;
	}

	public Collection<BulkDeleteDocument> getPendingRemoval() {
		return pendingRemoval;
	}

	public class RememberRemovedIterator implements Iterator<Object> {

		private final Iterator<?> it = collection.iterator();
		private Object current = null;

		public boolean hasNext() {
			return it.hasNext();
		}

		public Object next() {
			current = it.next();
			return current;
		}

		public void remove() {
			if (current != null) {
				addToPendingRemoval(current);
			}
			it.remove();
		}

	}

	void addToPendingRemoval(Object o) {
		if (!cascadeDelete()) {
			return;
		}
		LOG.debug("adding {} to pending removal list", o);
		pendingRemoval.add(BulkDeleteDocument.of(o));
	}

	private boolean cascadeDelete() {
		for (CascadeType t : referenceMetaData.cascade()) {
			if (CascadeType.DELETE_TYPES.contains(t)) {
				return true;
			}
		}
		return false;
	}

	public class RememberRemovedListIterator implements ListIterator<Object> {

		@SuppressWarnings("unchecked")
		private final ListIterator<Object> it = ((List<Object>) collection)
				.listIterator();
		private Object current = null;

		public boolean hasNext() {
			return it.hasNext();
		}

		public Object next() {
			current = it.next();
			return current;
		}

		public void remove() {
			if (current != null) {
				addToPendingRemoval(current);
			}
			it.remove();
		}

		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		public Object previous() {
			current = it.previous();
			return current;
		}

		public int nextIndex() {
			return it.nextIndex();
		}

		public int previousIndex() {
			return it.previousIndex();
		}

		public void set(Object e) {
			addToPendingRemoval(e);
			it.set(e);
		}

		public void add(Object e) {
			it.add(e);
		}
	}

}
