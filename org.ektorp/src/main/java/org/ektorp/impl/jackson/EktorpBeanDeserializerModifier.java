package org.ektorp.impl.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.MethodProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import org.ektorp.CouchDbConnector;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.impl.docref.BackReferencedBeanDeserializer;
import org.ektorp.impl.docref.ConstructibleAnnotatedCollection;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EktorpBeanDeserializerModifier extends BeanDeserializerModifier {

	private final CouchDbConnector db;
	private final ObjectMapper objectMapper;

	public EktorpBeanDeserializerModifier(CouchDbConnector db, ObjectMapper objectMapper) {
		this.db = db;
		this.objectMapper = objectMapper;
	}

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
			BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
			if (deserializer instanceof BeanDeserializer) {
				List<ConstructibleAnnotatedCollection> fields = collectFields(config, beanDesc);
				if (!fields.isEmpty()) {
					return new BackReferencedBeanDeserializer(
							(BeanDeserializer) deserializer, fields, db, beanDesc
									.getType().getRawClass());
				}
			}
			return super.modifyDeserializer(config, beanDesc, deserializer);


	}

	private List<ConstructibleAnnotatedCollection> collectFields(final DeserializationConfig config, final BeanDescription desc) {
		final List<ConstructibleAnnotatedCollection> fields = new ArrayList<ConstructibleAnnotatedCollection>();

		final Map<String, AnnotatedMethod> setters = new LinkedHashMap<String, AnnotatedMethod>();
		List<BeanPropertyDefinition> properties = desc.findProperties();
		for (BeanPropertyDefinition beanPropertyDefinition : properties) {
			setters.put(beanPropertyDefinition.getInternalName(), beanPropertyDefinition.getSetter());
		}

		ReflectionUtils.eachField(desc.getType().getRawClass(), new Predicate<Field>() {
			@Override
			public boolean apply(Field input) {
				if (ReflectionUtils.hasAnnotation(input, DocumentReferences.class)) {
					ConstructibleAnnotatedCollection c = collectBackrefField(config, desc, setters, input);
					if (c != null) {
						fields.add(c);
					}
				}
				return false;
			}

		});

		return fields;
	}

	private ConstructibleAnnotatedCollection collectBackrefField(DeserializationConfig config,
			BeanDescription beanDesc,
			Map<String, AnnotatedMethod> setters, Field field) {


		JavaType type = objectMapper.getTypeFactory().constructType(field.getGenericType());

		if (!(type instanceof CollectionType)) {
			return null;
		}
			CollectionType collectionType = (CollectionType) type;
			return new ConstructibleAnnotatedCollection(
					field,
					findCtor(config,
							collectionType,
							field.getType()),
					constructSettableProperty(config,
											beanDesc,
											field.getName(),
											setters.get(field.getName()),
											collectionType),
					collectionType);
	}

	/**
	 * Method copied from org.codehaus.jackson.map.deser.BeanDeserializerFactory
	 */
	protected SettableBeanProperty constructSettableProperty(
			DeserializationConfig config, BeanDescription beanDesc,
			String name, AnnotatedMethod setter, JavaType type) {
		// need to ensure method is callable (for non-public)
		if (config
				.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
			Method member = setter.getAnnotated();
			if (!Modifier.isPublic(member.getModifiers()) || !Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
				member.setAccessible(true);
			}
		}

		/*
		 * First: does the Method specify the deserializer to use? If so, let's
		 * use it.
		 */

		TypeDeserializer typeDeser = type.getTypeHandler();
		SettableBeanProperty prop =
			new MethodProperty(SimpleBeanPropertyDefinition.construct(config, setter), type, typeDeser, beanDesc.getClassAnnotations(),
				setter);

		// [JACKSON-235]: need to retain name of managed forward references:
		AnnotationIntrospector.ReferenceProperty ref = config
				.getAnnotationIntrospector().findReferenceType(setter);
		if (ref != null && ref.isManagedReference()) {
			prop.setManagedReferenceName(ref.getName());
		}
		return prop;
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
				.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		@SuppressWarnings("unchecked")
		Constructor<Collection<Object>> ctor = ClassUtil.findConstructor(
				(Class<Collection<Object>>) collectionClass, fixAccess);
		return ctor;
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

	@Override
	public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
			BeanDescription beanDesc, BeanDeserializerBuilder builder) {

		return super.updateBuilder(config, beanDesc, builder);
	}
}
