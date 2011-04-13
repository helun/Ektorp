package org.ektorp.impl.docref;

import java.lang.reflect.*;
import java.util.*;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.deser.*;
import org.codehaus.jackson.map.introspect.*;
import org.codehaus.jackson.map.type.*;
import org.codehaus.jackson.map.util.*;
import org.codehaus.jackson.type.*;
import org.ektorp.*;
import org.ektorp.docref.*;

/**
 * 
 * @author ragnar rova
 * 
 * 
 */
public class DocumentReferenceDeserializerFactory extends
		BeanDeserializerFactory {

	private final CouchDbConnector couchDbConnector;
	private final ObjectMapper objectMapper;

	public DocumentReferenceDeserializerFactory(
			CouchDbConnector couchDbConnector, ObjectMapper objectMapper)
			throws JsonMappingException {
		super(null);
		this.couchDbConnector = couchDbConnector;
		this.objectMapper = objectMapper;
	}

	@Override
	public JsonDeserializer<Object> createBeanDeserializer(
			DeserializationConfig config, DeserializerProvider p,
			JavaType type, BeanProperty property) throws JsonMappingException {

		BasicBeanDescription desc = config.introspect(type);
		List<ConstructibleAnnotatedCollection> fields = new ArrayList<ConstructibleAnnotatedCollection>();
		BasicBeanDescription beanDesc = config.introspect(type);
		Map<String, AnnotatedMethod> setters = beanDesc
				.findSetters(getVisibilityChecker(config, beanDesc));

		for (AnnotatedField field : desc.getClassInfo().fields()) {
			if (field.hasAnnotation(DocumentReferences.class)) {
				collectBackrefField(config, fields, beanDesc, setters,
						field);
			}
		}
		if (fields.isEmpty()) {
			return super.createBeanDeserializer(config, p, type, property);
		} else {
			return new BackReferencedBeanDeserializer(
					(BeanDeserializer) super.createBeanDeserializer(config, p,
							type, property), fields, couchDbConnector,
					type.getRawClass(), objectMapper);
		}
	}

	private void collectBackrefField(DeserializationConfig config,
			List<ConstructibleAnnotatedCollection> fields,
			BasicBeanDescription beanDesc,
			Map<String, AnnotatedMethod> setters, AnnotatedField field)
			throws JsonMappingException {

		JavaType collectionType = field.getType(new TypeBindings(field
				.getDeclaringClass()));
		if (collectionType instanceof CollectionType) {
			Constructor<Collection<Object>> ctor = findCtor(config,
					(CollectionType) collectionType, field.getRawType());
			ConstructibleAnnotatedCollection cf = new ConstructibleAnnotatedCollection(
					field, ctor, constructSettableProperty(config, beanDesc,
							field.getName(), setters.get(field.getName())));
			fields.add(cf);
		}
	}

	protected VisibilityChecker<?> getVisibilityChecker(
			DeserializationConfig config, BasicBeanDescription beanDesc) {
		VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
		if (!config
				.isEnabled(DeserializationConfig.Feature.AUTO_DETECT_SETTERS)) {
			vchecker = vchecker.withSetterVisibility(Visibility.NONE);
		}
		if (!config.isEnabled(DeserializationConfig.Feature.AUTO_DETECT_FIELDS)) {
			vchecker = vchecker.withFieldVisibility(Visibility.NONE);
		}
		vchecker = config.getAnnotationIntrospector().findAutoDetectVisibility(
				beanDesc.getClassInfo(), vchecker);
		return vchecker;
	}

	public boolean isSetter(Method method) {
		if (Modifier.isStatic(method.getModifiers())) {
			return false;
		}
		return method.getName().startsWith("set")
				&& method.getParameterTypes().length == 1
				&& method.getReturnType().equals(Void.TYPE);
	}

	@SuppressWarnings("rawtypes")
	final static Map<String, Class<? extends Collection>> _collectionFallbacks = new HashMap<String, Class<? extends Collection>>();
	static {
		_collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
		_collectionFallbacks.put(List.class.getName(), ArrayList.class);
		_collectionFallbacks.put(Set.class.getName(), LinkedHashSet.class);
		_collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
		_collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
		_collectionFallbacks.put("java.util.Deque", LinkedList.class);
		_collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
	}

	private Constructor<Collection<Object>> findCtor(
			DeserializationConfig config, CollectionType type, Class<?> clazz) {
		Class<?> collectionClass = clazz;
		if (type.isInterface() || type.isAbstract()) {
			@SuppressWarnings("rawtypes")
			Class<? extends Collection> fallback = _collectionFallbacks
					.get(collectionClass.getName());
			if (fallback == null) {
				throw new IllegalArgumentException(
						"Can not find a deserializer for non-concrete Collection type "
								+ type);
			}
			collectionClass = fallback;
		}

		boolean fixAccess = config
				.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		@SuppressWarnings("unchecked")
		Constructor<Collection<Object>> ctor = ClassUtil.findConstructor(
				(Class<Collection<Object>>) collectionClass, fixAccess);
		return ctor;
	}

}
