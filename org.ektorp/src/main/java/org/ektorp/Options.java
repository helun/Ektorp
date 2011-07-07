package org.ektorp;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * 
 * @author henrik
 *
 */
public class Options {

	private Map<String, String> options = new LinkedHashMap<String, String>();
	/**
	 * The loaded doc will include the special field '_conflicts' that contains all the conflicting revisions of the document.
	 * @return
	 */
	public Options includeConflicts() {
		options.put("conflicts", "true");
		return this;
	}
	/**
	 * The loaded doc will include the special field '_revisions' that describes all document revisions that exists in the database.
	 * @return
	 */
	public Options includeRevisions() {
		options.put("revisions", "true");
		return this;
	}
	/**
	 * Retrieve a specific revision of the document.
	 * @return
	 */
	public Options revision(String rev) {
		options.put("rev", rev);
		return this;
	}
	/**
	 * Adds a parameter to the GET request sent to the database.
	 * @param name
	 * @param value
	 * @return
	 */
	public Options param(String name, String value) {
		options.put(name, value);
		return this;
	}
	
	public Map<String, String> getOptions() {
		return options;
	}
	
	public boolean isEmpty() {
		return options.isEmpty();
	}
	
}
