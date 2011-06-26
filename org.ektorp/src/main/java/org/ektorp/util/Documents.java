package org.ektorp.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.node.*;
import org.ektorp.*;
import org.slf4j.*;

/**
 * Class for handling id and revision for persistent classes.
 * A persistent class must either declare setters and getters for the properties id and revision or
 * annotate equivalent methods with org.codehaus.jackson.annotate.JsonProperty("_id") and JsonProperty("_rev")
 * 
 * Map can be used as a document class, the map must contain the id field with key "_id" and revision with the key "_rev".
 * 
 * org.codehaus.jackson.node.ObjectNode can olso be used as a document class.
 * 
 * For special needs, a custom document accessor can be registered through the method registerAccessor(Class<?> documentType, DocumentAccessor accessor)
 * 
 * @author henrik lundgren
 * @author bjohnson.professional
 * @author Pascal G√©linas (issue 99)
 * 
 */
public final class Documents {

	private final static Logger LOG = LoggerFactory.getLogger(Documents.class);
	private final static ConcurrentMap<Class<?>, DocumentAccessor> accessors = new ConcurrentHashMap<Class<?>, DocumentAccessor>();
	private static final String ID_FIELD_NAME = "_id";
	private static final String REV_FIELD_NAME = "_rev";

	static {
		accessors.put(Map.class, new MapAccessor());
		putAccessor(ObjectNode.class, new ObjectNodeAccessor());
	}
	
	/**
	 * Used to register a custom DocumentAccessor for a particular class.
	 * Any existing accessor for the class will be overridden.
	 * @param documentType
	 * @param accessor
	 */
	public static void registerAccessor(Class<?> documentType, DocumentAccessor accessor) {
		Assert.notNull(documentType, "documentType may not be null");
		Assert.notNull(accessor, "accessor may not be null");
		if (accessors.containsKey(documentType)) {
			DocumentAccessor existing = getAccessor(documentType);
			LOG.warn(String.format("DocumentAccessor for class %s already exists: %s will be overridden by %s", documentType, existing.getClass(), accessor.getClass()));
		}
		putAccessor(documentType, accessor);
		LOG.debug("Registered document accessor: {} for class: {}", accessor.getClass(), documentType);
	}

	public static String getId(Object document) {
		return getAccessor(document).getId(document);
	}

	/**
	 * Will set the id property on the document IF a mutator exists. Otherwise
	 * nothing happens.
	 * 
	 * @param document
	 * @param id
	 */
	public static void setId(Object document, String id) {
		DocumentAccessor d = getAccessor(document);
		if (d.hasIdMutator()) {
			d.setId(document, id);
		}
	}

	public static String getRevision(Object document) {
		return getAccessor(document).getRevision(document);
	}

	public static void setRevision(Object document, String rev) {
		getAccessor(document).setRevision(document, rev);
	}

	public static boolean isNew(Object document) {
		return getRevision(document) == null;
	}
	
	private static <T> void putAccessor(Class<? extends T> documentType, DocumentAccessor accessor){
		accessors.put(documentType, accessor);
	}
	
	private static DocumentAccessor getAccessor(Class<?> documentType){
		return accessors.get(documentType);
	}

	private static DocumentAccessor getAccessor(Object document) {
		Class<?> clazz = document.getClass();
		DocumentAccessor accessor = getAccessor(clazz);
		if (accessor == null) {
			if (document instanceof Map<?, ?>) {
				accessor = getAccessor(Map.class);
				putAccessor(clazz, accessor);
			} else if (document instanceof ObjectNode) {
				accessor = getAccessor(ObjectNode.class);
				putAccessor(clazz, accessor);
			} else {
				try {
					accessor = new AnnotatedMethodAccessor(clazz);
				} catch (InvalidDocumentException eAnnotatedMethod) {
					try {
						accessor = new AnnotatedFieldAccessor(clazz);
					} catch (InvalidDocumentException eAnnotatedField) {
						accessor = new MethodAccessor(clazz);
					}
				}
				accessors.putIfAbsent(clazz, accessor);
				accessor = getAccessor(clazz);
			}
		}
		return accessor;
	}

	private static class MethodAccessor implements DocumentAccessor {

		private final Class<?>[] NO_PARAMS = new Class<?>[0];
		private final Object[] NO_ARGS = new Object[0];

		Method idAccessor;
		Method idMutator;
		Method revisionAccessor;
		Method revisionMutator;

		MethodAccessor(Class<?> clazz) {
			try {
				idAccessor = resolveIdAccessor(clazz);
				assertMethodFound(clazz, idAccessor, "id accessor");

				idMutator = resolveIdMutator(clazz);

				revisionAccessor = resolveRevAccessor(clazz);
				assertMethodFound(clazz, revisionAccessor, "revision accessor");

				revisionMutator = resolveRevMutator(clazz);
				assertMethodFound(clazz, revisionMutator, "revision mutator");
			} catch (InvalidDocumentException e) {
				throw e;
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#hasIdMutator()
		 */
		public boolean hasIdMutator() {
			return idMutator != null;
		}

		protected void assertMethodFound(Class<?> clazz, Method m,
				String missingField) {
			if (m == null) {
				throw new InvalidDocumentException(clazz, missingField);
			}
		}

		protected Method resolveRevAccessor(Class<?> clazz) throws Exception {
			return findMethod(clazz, "getRevision", NO_PARAMS);
		}

		protected Method resolveIdAccessor(Class<?> clazz) throws Exception {
			return findMethod(clazz, "getId", NO_PARAMS);
		}

		private Method findMethod(Class<?> clazz, String name,
				Class<?>... parameters) throws Exception {
			for (Method me : clazz.getDeclaredMethods()) {
				if (me.getName().equals(name)
						&& me.getParameterTypes().length == parameters.length) {
					me.setAccessible(true);
					return me;
				}
			}
			return clazz.getSuperclass() != null ? findMethod(
					clazz.getSuperclass(), name, parameters) : null;
		}

		protected Method resolveIdMutator(Class<?> clazz) throws Exception {
			return findMethod(clazz, "setId", String.class);
		}

		protected Method resolveRevMutator(Class<?> clazz) throws Exception {
			return findMethod(clazz, "setRevision", String.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#getId(java.lang.Object)
		 */
		public String getId(Object o) {
			try {
				return (String) idAccessor.invoke(o, NO_ARGS);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#setId(java.lang.Object,
		 * java.lang.String)
		 */
		public void setId(Object o, String id) {
			try {
				idMutator.invoke(o, id);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#getRevision(java.lang.Object)
		 */
		public String getRevision(Object o) {
			try {
				return (String) revisionAccessor.invoke(o, NO_ARGS);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#setRevision(java.lang.Object,
		 * java.lang.String)
		 */
		public void setRevision(Object o, String rev) {
			try {
				revisionMutator.invoke(o, rev);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
	}

	private final static class AnnotatedMethodAccessor extends MethodAccessor {

		AnnotatedMethodAccessor(Class<?> clazz) {
			super(clazz);
		}

		@Override
		protected Method resolveRevAccessor(Class<?> clazz) throws Exception {
			return findAnnotatedMethod(clazz, "_rev", String.class);
		}

		@Override
		protected Method resolveIdAccessor(Class<?> clazz) throws Exception {
			return findAnnotatedMethod(clazz, "_id", String.class);
		}

		@Override
		protected Method resolveIdMutator(Class<?> clazz) throws Exception {
			return findAnnotatedMethod(clazz, "_id", void.class);
		}

		@Override
		protected Method resolveRevMutator(Class<?> clazz) throws Exception {
			return findAnnotatedMethod(clazz, "_rev", void.class);
		}

		private Method findAnnotatedMethod(Class<?> clazz,
			String annotationValue, Class<?> returnType) {
			for (Method me : clazz.getDeclaredMethods()) {
				JsonProperty a = me.getAnnotation(JsonProperty.class);
				if (a != null && a.value().equals(annotationValue)
						&& returnType.isAssignableFrom(me.getReturnType())) {
					me.setAccessible(true);
					return me;
				}
			}

			return clazz.getSuperclass() != null ? findAnnotatedMethod(
					clazz.getSuperclass(), annotationValue, returnType)
					: null;
		}
	}

	private final static class AnnotatedFieldAccessor implements DocumentAccessor {

		Field idAccessor;
		Field idMutator;
		Field revisionAccessor;
		Field revisionMutator;

		AnnotatedFieldAccessor(Class<?> clazz) {
			try {
				idAccessor = resolveIdAccessor(clazz);
				assertFieldFound(clazz, idAccessor, "id accessor");

				idMutator = resolveIdMutator(clazz);

				revisionAccessor = resolveRevAccessor(clazz);
				assertFieldFound(clazz, revisionAccessor, "revision accessor");

				revisionMutator = resolveRevMutator(clazz);
				assertFieldFound(clazz, revisionMutator, "revision mutator");
			} catch (InvalidDocumentException e) {
				throw e;
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#hasIdMutator()
		 */
		public boolean hasIdMutator() {
			return idMutator != null;
		}

		private void assertFieldFound(Class<?> clazz, Field f,
				String missingField) {
			if (f == null) {
				throw new InvalidDocumentException(clazz, missingField);
			}
		}

		private Field resolveRevAccessor(Class<?> clazz) throws Exception {
			Field f = null;
			f = findAnnotatedField(clazz, "_rev");
			return f;
		}

		private Field resolveIdAccessor(Class<?> clazz) throws Exception {
			Field f = null;
			f = findAnnotatedField(clazz, "_id");
			return f;
		}

		private Field resolveIdMutator(Class<?> clazz) throws Exception {
			Field f = null;
			f = findAnnotatedField(clazz, "_id");
			return f;
		}

		private Field resolveRevMutator(Class<?> clazz) throws Exception {
			Field f = null;
			f = findAnnotatedField(clazz, "_rev");
			return f;
		}

		private Field findAnnotatedField(Class<?> clazz,
				String annotationValue) {
			for (Field me : clazz.getDeclaredFields()) {
				JsonProperty a = me.getAnnotation(JsonProperty.class);
				if (a != null && a.value().equals(annotationValue)) {
					me.setAccessible(true);
					return me;
				}
			}

			return clazz.getSuperclass() != null ? findAnnotatedField(
					clazz.getSuperclass(), annotationValue) : null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#getId(java.lang.Object)
		 */
		public String getId(Object o) {
			try {
				return (String) idAccessor.get(o);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#setId(java.lang.Object,
		 * java.lang.String)
		 */
		public void setId(Object o, String id) {
			try {
				idMutator.set(o, id);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#getRevision(java.lang.Object)
		 */
		public String getRevision(Object o) {
			try {
				return (String) revisionAccessor.get(o);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ektorp.util.DocumentAccessor#setRevision(java.lang.Object,
		 * java.lang.String)
		 */
		public void setRevision(Object o, String rev) {
			try {
				revisionMutator.set(o, rev);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
	}

	private final static class MapAccessor implements DocumentAccessor {

		public String getId(Object o) {
			return cast(o).get(ID_FIELD_NAME);
		}

		public String getRevision(Object o) {
			return cast(o).get(REV_FIELD_NAME);
		}

		public boolean hasIdMutator() {
			return true;
		}

		public void setId(Object o, String id) {
			cast(o).put(ID_FIELD_NAME, id);
		}

		public void setRevision(Object o, String rev) {
			cast(o).put(REV_FIELD_NAME, rev);
		}
		
		@SuppressWarnings("unchecked")
		private Map<String, String> cast(Object o) {
			return (Map<String, String>)o;
		}

	}

	private final static class ObjectNodeAccessor implements DocumentAccessor {

		public boolean hasIdMutator() {
			return true;
		}

		public String getId(Object o) {
			return getFieldValue(o, ID_FIELD_NAME);
		}

		public void setId(Object o, String id) {
			setField(o, ID_FIELD_NAME, id);
		}

		public String getRevision(Object o) {
			return getFieldValue(o, REV_FIELD_NAME);
		}

		public void setRevision(Object o, String rev) {
			setField(o, REV_FIELD_NAME, rev);
		}


		private String getFieldValue(Object o, String fieldName) {
			JsonNode target = (JsonNode)o;
			JsonNode field = target.get(fieldName);
			if (field == null) {
				return null;
			}
			if (!field.isTextual()) {
				throw Exceptions.newRTE("field %s in node: %s is not textual ",
						fieldName, target);
			}
			return field.getTextValue();
		}

		private void setField(Object o, String fieldName, String fieldValue) {
			((ObjectNode)o).put(fieldName, fieldValue);
		}

	}

}
