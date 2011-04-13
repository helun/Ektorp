package org.ektorp.docref;

/**
 * Controls when document referenced documents are loaded.
 *
 * @author ragnar rova
 *
 */
public enum FetchType {
	/**
	 * Load referenced documents when the parent document is loaded.
	 *
	 */
	EAGER,
	/**
	 * Load referenced documents when any method on the annotated collection is
	 * called which needs the content.
	 *
	 */
	LAZY
}
