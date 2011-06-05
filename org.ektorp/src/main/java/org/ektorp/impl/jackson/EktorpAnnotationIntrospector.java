package org.ektorp.impl.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.impl.NameConventions;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;

public class EktorpAnnotationIntrospector extends NopAnnotationIntrospector {

	private final Map<Class<?>, Set<String>> ignorableMethods = new HashMap<Class<?>, Set<String>>();
	private final Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();

	
	@Override
	public boolean isHandled(Annotation ann) {
		return DocumentReferences.class == ann.annotationType();
	}
	
	@Override
	public boolean isIgnorableField(AnnotatedField f) {
		return f.hasAnnotation(DocumentReferences.class);
	}
	
	@Override
	public boolean isIgnorableMethod(AnnotatedMethod m) {
		Set<String> names = ignorableMethods.get(m.getDeclaringClass());
		if (names == null) {
			initIgnorableMethods(m.getDeclaringClass());
			names = ignorableMethods.get(m.getDeclaringClass());
		}
		
		return names.contains(m.getName());
	}
	
	private void initIgnorableMethods(final Class<?> clazz) {
		final Set<String> names = new HashSet<String>();
		ReflectionUtils.eachField(clazz, new Predicate<Field>() {
			@Override
			public boolean apply(Field input) {
				if (ReflectionUtils.hasAnnotation(input, DocumentReferences.class)) {
					annotatedClasses.add(clazz);
					names.add(NameConventions.getterName(input.getName()));
				}
				return false;
			}
		});
		ignorableMethods.put(clazz, names);
	}
	
}
