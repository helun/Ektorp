package org.ektorp.impl;

import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

public class NameConventions {

	private final static String BACK_REF_VIEW_NAME = "ektorp_docrefs_%s";
	private final static String DESIGN_DOC_NAME_FOR_TYPE = "_design/%s";

	private NameConventions() {
	}

	/**
	 * The name of the view supporting a @DocumentReferences collection.
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String backReferenceViewName(String fieldName) {
		return format(BACK_REF_VIEW_NAME, fieldName);
	}

	/**
	 * The name for design document belonging to a specific type.
	 * 
	 * @param klass
	 * @return
	 */
	public static String designDocName(Class<?> klass) {
		return format(DESIGN_DOC_NAME_FOR_TYPE, klass.getSimpleName());
	}

	/**
	 * The name for a design document derived from a string.
	 * 
	 * @param docName
	 * @return
	 */
	public static String designDocName(String docName) {
		return format(DESIGN_DOC_NAME_FOR_TYPE, docName);
	}

	public static String getterName(String property) {
		return "get" + capitalize(property);
	}

	/**
	 * Returns a String which capitalizes the first letter of the string.
	 */
	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}

}
