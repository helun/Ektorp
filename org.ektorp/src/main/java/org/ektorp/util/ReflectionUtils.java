package org.ektorp.util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtils {
	
	public static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> annotationClass, Predicate<Field> p) {
		for (Field f : clazz.getDeclaredFields()) {
			T a = f.getAnnotation(annotationClass);
			if (a != null & p.apply(f)) {
				return a;
			}
		}
		
		if (clazz.getSuperclass() != null) {
			return findAnnotation(clazz.getSuperclass(), annotationClass, p);
		}
		return null;
	}
	
	public static Collection<Field> eachField(Class<?> clazz, Predicate<Field> p) {
		List<Field> result = new ArrayList<Field>();
		for (Field f : clazz.getDeclaredFields()) {
			if (p.apply(f)) {
				result.add(f);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			result.addAll(eachField(clazz.getSuperclass(), p));
		}
		return result;
	}
	
	public static Collection<Method> eachMethod(Class<?> clazz, Predicate<Method> p) {
		List<Method> result = new ArrayList<Method>();
		for (Method f : clazz.getDeclaredMethods()) {
			if (p.apply(f)) {
				result.add(f);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			result.addAll(eachMethod(clazz.getSuperclass(), p));
		}
		return result;
	}
	
	public static <T extends Annotation> void eachAnnotation(Class<?> clazz,
			Class<T> annotationClass, Predicate<T> p) {
		T a = clazz.getAnnotation(annotationClass);
		if (a != null) {
			p.apply(a);
		}
		for (Method me : clazz.getDeclaredMethods()) {
			a = me.getAnnotation(annotationClass);
			if (a != null) {
				p.apply(a);
			}
		}
		
		if (clazz.getSuperclass() != null) {
			eachAnnotation(clazz.getSuperclass(), annotationClass, p);
		}
		
	}
	
	/**
	 * Ignores case when comparing method names
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Method findMethod(Class<?> clazz, String name) {
		for (Method me : clazz.getDeclaredMethods()) {
			if (me.getName().equalsIgnoreCase(name)) {
				return me;
			}
		}
		if (clazz.getSuperclass() != null) {
			return findMethod(clazz.getSuperclass(), name);
		}
		return null;
	}
	
	public static boolean hasAnnotation(AnnotatedElement e, Class<? extends Annotation> annotationClass) {
		return e.getAnnotation(annotationClass) != null;
	}
	
	public interface AnnotationPredicate {
		boolean equals(Method m, Annotation a);
	}
}
