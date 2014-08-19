package org.ektorp.impl.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ektorp.docref.DocumentReferences;
import org.ektorp.impl.NameConventions;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

public class EktorpAnnotationIntrospector extends NopAnnotationIntrospector {

	private final Map<Class<?>, Set<String>> ignorableMethods = new HashMap<Class<?>, Set<String>>();
	private final Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();


	@Override
	public boolean hasIgnoreMarker(AnnotatedMember member) {
		return super.hasIgnoreMarker(member);
	}

	public boolean isIgnorableField(AnnotatedField f) {
		return f.hasAnnotation(DocumentReferences.class);
	}

	public boolean isIgnorableMethod(AnnotatedMethod m) {
		Set<String> names = ignorableMethods.get(m.getDeclaringClass());
		if (names == null) {
			initIgnorableMethods(m.getDeclaringClass());
			names = ignorableMethods.get(m.getDeclaringClass());
		}

		return names.contains(m.getName());
	}

	@Override
	public String[] findPropertiesToIgnore(Annotated ac) {
		if(ac instanceof AnnotatedClass){
			return findPropertiesToIgnore((AnnotatedClass) ac);
		}
		return super.findPropertiesToIgnore(ac);
	}
	
    public String[] findPropertiesToIgnore(AnnotatedClass ac) {
    	List<String> ignoreFields = null;
    	for (AnnotatedField f : ac.fields()) {
    		if (isIgnorableField(f)) {
    			if (ignoreFields == null) {
    				ignoreFields = new ArrayList<String>();
    			}
    			ignoreFields.add(f.getName());
    		}
    	}
        return ignoreFields != null ? ignoreFields.toArray(new String[ignoreFields.size()]) : null;
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
