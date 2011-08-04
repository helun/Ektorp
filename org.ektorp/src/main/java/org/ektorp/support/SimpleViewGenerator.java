package org.ektorp.support;

import static org.ektorp.util.ReflectionUtils.*;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.map.*;
import org.ektorp.docref.*;
import org.ektorp.impl.*;
import org.ektorp.util.*;
import org.slf4j.*;

public class SimpleViewGenerator {

	private final static Logger LOG = LoggerFactory
			.getLogger(SimpleViewGenerator.class);
	private final static String ALL_VIEW_TEMPLATE = "function(doc) { if(%s) {emit(null, doc._id)} }";
	private final static String LOOKUP_BY_PROPERTY_TEMPLATE = "function(doc) { if(%s) {emit(doc.%s, doc._id)} }";
	private final static String ITERABLE_PROPERTY_TEMPLATE = "function(doc) {%s}";
	private final static String ITERABLE_PROPERTY_WITH_DISCRMINATOR_TEMPLATE = "function(doc) {if (%s) {%s}}";
	private final static String ITERABLE_PROPERTY_BODY = "for (var i in doc.%s) {emit(doc.%s[i], doc._id);}";
	private final static String REFERING_CHILDREN_AS_SET_W_ORDER_BY = "function(doc) { if(%s) { emit([doc.%s, '%s', doc.%s], null); } }";
	private final static String REFERING_CHILDREN_AS_SET = "function(doc) { if(%s) { emit([doc.%s, '%s'], null); } }";

	private SoftReference<ObjectMapper> mapperRef;

	public DesignDocument.View generateFindByView(String propertyName,
			String typeDiscriminator) {
		String selector = typeDiscriminator.length() > 0 ? typeDiscriminator
				+ " && doc." + propertyName : "doc." + propertyName;
		return new DesignDocument.View(String.format(
				LOOKUP_BY_PROPERTY_TEMPLATE, selector, propertyName));
	}

	public DesignDocument.View generateFindByIterableView(String propertyName,
			String typeDiscriminator) {
		String body = String.format(ITERABLE_PROPERTY_BODY, propertyName,
				propertyName);
		if (typeDiscriminator.length() > 0) {
			return new DesignDocument.View(String.format(
					ITERABLE_PROPERTY_WITH_DISCRMINATOR_TEMPLATE,
					typeDiscriminator, body));
		} else {
			return new DesignDocument.View(String.format(
					ITERABLE_PROPERTY_TEMPLATE, body));
		}
	}

	public DesignDocument.View generateDocRefsAsSetWithOrderByView(
			String backRef, String fieldName, String orderBy,
			String typeDiscriminator) {
		String selector = typeDiscriminator.length() > 0 ? typeDiscriminator
				+ " && doc." + backRef : "doc." + backRef;
		return new DesignDocument.View(String.format(
				REFERING_CHILDREN_AS_SET_W_ORDER_BY, selector, backRef,
				fieldName, orderBy));
	}

	public DesignDocument.View generateDocRefsAsSetView(String backRef,
			String fieldName, String typeDiscriminator) {
		String selector = typeDiscriminator.length() > 0 ? typeDiscriminator
				+ " && doc." + backRef : "doc." + backRef;
		return new DesignDocument.View(String.format(REFERING_CHILDREN_AS_SET,
				selector, backRef, fieldName));
	}

	private DesignDocument.View generateAllView(String typeDiscriminator) {
		return new DesignDocument.View(String.format(ALL_VIEW_TEMPLATE,
				typeDiscriminator));
	}

	/**
	 * Generates views based on annotations found in a repository class. If the
	 * repository class extends org.ektorp.support.CouchDbRepositorySupport its
	 * handled type will also examined for annotations eligible for view
	 * generation.
	 * 
	 * @param repository
	 * @return a Map with generated views.
	 */
	public Map<String, DesignDocument.View> generateViews(
			final Object repository) {
		final Map<String, DesignDocument.View> views = new HashMap<String, DesignDocument.View>();
		final Class<?> repositoryClass = repository.getClass();

		final Class<?> handledType = repository instanceof CouchDbRepositorySupport<?> ? ((CouchDbRepositorySupport<?>) repository)
				.getHandledType() : null;

		createDeclaredViews(views, repositoryClass);

		eachMethod(repositoryClass, new Predicate<Method>() {
			public boolean apply(Method input) {
				if (hasAnnotation(input, GenerateView.class)) {
					generateView(views, input, handledType);
				}
				return false;
			}
		});

		if (handledType != null) {
			views.putAll(generateViewsFromPersistentType(handledType));
		}
		return views;
	}

	private void createDeclaredViews(
			final Map<String, DesignDocument.View> views, final Class<?> klass) {
		eachAnnotation(klass, Views.class, new Predicate<Views>() {

			public boolean apply(Views input) {
				for (View v : input.value()) {
					addView(views, v, klass);
				}
				return true;
			}
		});

		ReflectionUtils.eachAnnotation(klass, View.class,
				new Predicate<View>() {

					public boolean apply(View input) {
						addView(views, input, klass);
						return true;
					}
				});
	}

	private String resolveTypeDiscriminator(final Class<?> persistentType) {
		final List<String> discrimintators = new ArrayList<String>();
		TypeDiscriminator td = persistentType
				.getAnnotation(TypeDiscriminator.class);
		if (td != null) {
			if (td.value().length() == 0) {
				throw new ViewGenerationException(
						String.format(
								"@TypeDiscriminator declared on type level must specify custom discriminator condition",
								persistentType));
			}
			if (hasTypeDiscriminatorFieldOrMethod(persistentType)) {
				throw new ViewGenerationException(
						String.format(
								"@TypeDiscriminator declared on type level may not be combined with @TypeDiscriminator in fields or on methods",
								persistentType));
			}
			return td.value();
		}

		eachField(persistentType, new Predicate<Field>() {
			public boolean apply(Field input) {
				if (hasAnnotation(input, TypeDiscriminator.class)) {
					discrimintators.add("doc." + input.getName());
				}
				return false;
			}
		});

		eachMethod(persistentType, new Predicate<Method>() {
			public boolean apply(Method input) {
				if (hasAnnotation(input, TypeDiscriminator.class)) {
					discrimintators
							.add("doc."
									+ firstCharToLowerCase(input.getName()
											.substring(3)));
				}
				return true;
			}
		});
		return Joiner.join(discrimintators, " && ");
	}

	private boolean hasTypeDiscriminatorFieldOrMethod(
			final Class<?> persistentType) {
		Collection<?> hits = eachField(persistentType, new Predicate<Field>() {
			public boolean apply(Field input) {
				return hasAnnotation(input, TypeDiscriminator.class);
			}
		});
		if (!hits.isEmpty()) {
			return true;
		}

		hits = eachMethod(persistentType, new Predicate<Method>() {
			public boolean apply(Method input) {
				return hasAnnotation(input, TypeDiscriminator.class);
			}
		});
		return !hits.isEmpty();
	}

	/**
	 * Generates views based on annotations found in a persistent class.
	 * Typically @DocumentReferences annotations.
	 * 
	 * @param persistentType
	 * @return a Map with generated views.
	 */
	public Map<String, DesignDocument.View> generateViewsFromPersistentType(
			final Class<?> persistentType) {
		Assert.notNull(persistentType, "persistentType may not be null");
		final Map<String, DesignDocument.View> views = new HashMap<String, DesignDocument.View>();

		createDeclaredViews(views, persistentType);

		eachField(persistentType, new Predicate<Field>() {
			public boolean apply(Field input) {
				if (hasAnnotation(input, DocumentReferences.class)) {
					generateView(views, input);
				}
				return false;
			}
		});
		return views;
	}

	private void addView(Map<String, DesignDocument.View> views, View input,
			Class<?> repositoryClass) {
		if (input.file().length() > 0) {
			views.put(input.name(),
					loadViewFromFile(views, input, repositoryClass));
		} else if (shouldLoadFunctionFromClassPath(input.map())
				|| shouldLoadFunctionFromClassPath(input.reduce())) {
			views.put(input.name(), loadViewFromFile(input, repositoryClass));
		} else {
			views.put(input.name(), DesignDocument.View.of(input));
		}
	}

	public boolean shouldLoadFunctionFromClassPath(String function) {
		return function != null && function.startsWith("classpath:");
	}

	private DesignDocument.View loadViewFromFile(View input,
			Class<?> repositoryClass) {
		String mapPath = input.map();
		String map;
		if (shouldLoadFunctionFromClassPath(mapPath)) {
			map = loadResourceFromClasspath(repositoryClass, mapPath.substring(10));
		} else {
			map = mapPath;
		}

		String reducePath = input.reduce();
		String reduce;
		if (shouldLoadFunctionFromClassPath(reducePath)) {
			reduce = loadResourceFromClasspath(repositoryClass, reducePath.substring(10));
		} else {
			reduce = reducePath.length() > 0 ? reducePath : null;
		}
		return new DesignDocument.View(map, reduce);
	}

	private String loadResourceFromClasspath(Class<?> repositoryClass,
			String path) {
		try {
			InputStream in = repositoryClass.getResourceAsStream(path);
			if (in == null) {
				throw new FileNotFoundException(
						"Could not load view file with path: " + path);
			}
			return IOUtils.toString(in, "UTF-8");
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private DesignDocument.View loadViewFromFile(
			Map<String, DesignDocument.View> views, View input,
			Class<?> repositoryClass) {
		try {
			String json = loadResourceFromClasspath(repositoryClass,
					input.file());
			return mapper().readValue(json.replaceAll("\n", ""),
					DesignDocument.View.class);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private boolean isIterable(Class<?> type) {
		return Iterable.class.isAssignableFrom(type);
	}

	protected void generateView(
			Map<String, org.ektorp.support.DesignDocument.View> views, Field f) {
		DocumentReferences referenceMetaData = f
				.getAnnotation(DocumentReferences.class);
		if (referenceMetaData == null) {
			LOG.warn("No DocumentReferences annotation found in field: ",
					f.getName());
			return;
		}

		if (referenceMetaData.view().length() > 0) {
			LOG.debug(
					"Skipping view generation for field {} as view is already specified",
					f.getName());
			return;
		}

		if (Set.class.isAssignableFrom(f.getType())) {
			generateSetBasedDocRefView(views, f, referenceMetaData);
		} else {
			throw new ViewGenerationException(
					String.format(
							"The type of the field: %s in %s annotated with DocumentReferences is not supported. (Must be assignable from java.util.Set)",
							f.getName(), f.getDeclaringClass()));
		}
	}

	protected void generateView(
			Map<String, org.ektorp.support.DesignDocument.View> views, Method me) {
		DocumentReferences referenceMetaData = me
				.getAnnotation(DocumentReferences.class);
		if (referenceMetaData == null) {
			LOG.warn("No DocumentReferences annotation found in method: ",
					me.getName());
			return;
		}
		if (!me.getName().startsWith("get")) {
			throw new ViewGenerationException(
					String.format(
							"The method: %s in %s annotated with DocumentReferences does not conform to the naming convention of 'getXxxx'",
							me.getName(), me.getDeclaringClass()));
		}

		if (Set.class.isAssignableFrom(me.getReturnType())) {
			generateSetBasedDocRefView(views, me, referenceMetaData);
		} else {
			throw new ViewGenerationException(
					String.format(
							"The return type of: %s in %s annotated with DocumentReferences is not supported. (Must be assignable from java.util.Set)",
							me.getName(), me.getDeclaringClass()));
		}
	}

	private void generateSetBasedDocRefView(
			Map<String, org.ektorp.support.DesignDocument.View> views,
			Member me, DocumentReferences referenceMetaData) {
		String fieldName = firstCharToLowerCase(me.getName());
		String orderBy = referenceMetaData.orderBy();
		String backRef = referenceMetaData.backReference();
		if (backRef.length() == 0) {
			throw new ViewGenerationException(
					String.format(
							"The DocumentReferences annotation in %s must specify a backReference",
							me.getDeclaringClass()));
		}
		String viewName = NameConventions.backReferenceViewName(fieldName);
		String typeDiscriminator = resolveTypeDiscriminatorForBackReference(me);
		if (orderBy.length() > 0) {

			views.put(
					viewName,
					generateDocRefsAsSetWithOrderByView(backRef, fieldName,
							orderBy, typeDiscriminator));
		} else {
			views.put(
					viewName,
					generateDocRefsAsSetView(backRef, fieldName,
							typeDiscriminator));
		}
	}

	private String resolveTypeDiscriminatorForBackReference(Member m) {
		Method me = ReflectionUtils.findMethod(m.getDeclaringClass(), "get"
				+ firstCharToUpperCase(m.getName()));
		if (me != null) {
			return resolveTypeDiscriminator(resolveReturnType(me));
		}
		return "";
	}

	private void generateView(Map<String, DesignDocument.View> views,
			Method me, Class<?> handledType) {
		String name = me.getName();
		if (!name.startsWith("findBy") && !name.equals("getAll")) {
			throw new ViewGenerationException(
					String.format(
							"The method: %s in %s annotated with GenerateView does not conform to the naming convention of 'findByXxxx'",
							name, me.getDeclaringClass()));
		}

		Class<?> type = resolveReturnType(me);
		if (type == null) {
			if (handledType != null) {
				type = handledType;
			} else {
				throw new ViewGenerationException(
						"Could not resolve return type for method: %s in %s",
						me.getName(), me.getDeclaringClass());
			}
		}

		String typeDiscriminator = resolveTypeDiscriminator(type);

		if (name.equals("getAll")) {
			if (typeDiscriminator.length() < 1) {
				throw new ViewGenerationException(
						String.format(
								"Cannot generate 'all' view for %s. No type discriminator could be resolved. Try annotate unique field(s) with @TypeDiscriminator",
								type.getDeclaringClass()));
			}
			views.put("all", generateAllView(typeDiscriminator));
			return;
		}

		String finderName = name.substring(6);
		String fieldName = resolveFieldName(me, finderName);
		Method getter = findMethod(type, "get" + fieldName);
		if (getter == null) {
			// try pluralis
			fieldName += "s";
			getter = findMethod(type, "get" + fieldName);
		}
		if (getter == null) {
			throw new ViewGenerationException(
					"Could not generate view for method %s. No get method found for property %s in %s",
					name, name.substring(6), type);
		}

		fieldName = firstCharToLowerCase(fieldName);

		DesignDocument.View view;
		if (isIterable(getter.getReturnType())) {
			view = generateFindByIterableView(fieldName, typeDiscriminator);
		} else {
			view = generateFindByView(fieldName, typeDiscriminator);
		}

		views.put("by_" + firstCharToLowerCase(finderName), view);
	}

	private String resolveFieldName(Method me, String finderName) {
		GenerateView g = me.getAnnotation(GenerateView.class);
		String field = g.field();
		return field.length() == 0 ? finderName : g.field();
	}

	private String firstCharToLowerCase(String name) {
		return Character.toString(Character.toLowerCase(name.charAt(0)))
				+ name.substring(1);
	}

	private String firstCharToUpperCase(String name) {
		return Character.toString(Character.toUpperCase(name.charAt(0)))
				+ name.substring(1);
	}

	private Class<?> resolveReturnType(Method me) {
		Type returnType = me.getGenericReturnType();

		if (returnType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) returnType;
			Type[] typeArguments = type.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				if (typeArgument instanceof Class<?>) {
					return (Class<?>) typeArgument;
				}
			}
			return null;
		}
		return (Class<?>) returnType;
	}

	private ObjectMapper mapper() {
		if (mapperRef == null) {
			mapperRef = new SoftReference<ObjectMapper>(new ObjectMapper());
		}
		ObjectMapper mapper = mapperRef.get();
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapperRef = new SoftReference<ObjectMapper>(mapper);
		}
		return mapper;
	}

}
