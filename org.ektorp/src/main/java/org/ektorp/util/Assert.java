package org.ektorp.util;

import java.util.*;
/**
 * 
 * @author henrik lundgren
 * 
 */
public final class Assert {

	private Assert() {
		// noninstantiable
	}

	public static void notNull(Object o) {
		notNull(o, null);
	}
	
	public static void notNull(Object o, String message) {
		if (o == null) {
			throw message == null ?
			new NullPointerException() : new NullPointerException(message);
		}
	}

	public static void isNull(Object o, String message) {
		if (o != null) {
			throwIllegalArgument(message);
		}
	}
	
	public static void isTrue(boolean b) {
		isTrue(b, null);
	}
	
	public static void isTrue(boolean b, String message) {
		if (!b) {
			throwIllegalArgument(message);
		}
	}

	public static void notEmpty(Collection<?> c, String message) {
		notNull(c, message);
		if (c.isEmpty()) {
			throwIllegalArgument(message);
		}
	}

	public static void notEmpty(Object[] a, String message) {
		notNull(a, message);
		if (a.length == 0) {
			throwIllegalArgument(message);
		}
	}

	public static void hasText(String s) {
		hasText(s, null);
	}
	
	public static void hasText(String s, String message) {
		if (s == null || s.length() == 0) {
			throwIllegalArgument(message);
		}
	}
	
	private static void throwIllegalArgument(String s) {
		throw s == null ?
				new IllegalArgumentException() : new IllegalArgumentException(s);
	}
}