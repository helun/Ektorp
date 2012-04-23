package org.ektorp.impl.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
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
	public boolean hasIgnoreMarker(AnnotatedMember member) {
		if(member instanceof AnnotatedField){
			return isIgnorableField((AnnotatedField) member);
		}else if(member instanceof AnnotatedMethod){
			return isIgnorableMethod((AnnotatedMethod) member);
		}
		return false;
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
    	if(!(ac instanceof AnnotatedClass)){
    		return null;
    	}
    	AnnotatedClass clazz = (AnnotatedClass) ac;
    	List<String> ignoreFields = null;
    	for (AnnotatedField f : clazz.fields()) {
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
