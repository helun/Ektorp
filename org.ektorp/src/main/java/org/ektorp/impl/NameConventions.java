package org.ektorp.impl;

import static java.lang.String.format;

public class NameConventions {

	private final static String BACK_REF_VIEW_NAME = "ektorp_docrefs_%s";
	private final static String DESIGN_DOC_NAME_FOR_TYPE = "_design/%s";
	
	private NameConventions() {}
	/**
	 * The name of the view supporting a @DocumentReferences collection.
	 * @param fieldName
	 * @return
	 */
	public static String backReferenceViewName(String fieldName) {
		return format(BACK_REF_VIEW_NAME, fieldName);
	}
	/**
	 * The name for design document belonging to a specific type.
	 * @param klass
	 * @return
	 */
	public static String designDocName(Class<?> klass) {
		return format(DESIGN_DOC_NAME_FOR_TYPE, klass.getSimpleName());
	}
	
}
