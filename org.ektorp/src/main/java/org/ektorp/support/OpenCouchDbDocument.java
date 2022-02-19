package org.ektorp.support;

import java.util.Map;

/**
 * Provides convenience field and methods for holding unmapped fields in JSON serialization / deserialization.
 *
 * Subclasses of this class can be read and written to and from JSON documents without loosing unmapped fields in the process.
 * i.e. the class will be tolerant for changes in the underlying JSON data,
 * @author henriklundgren
 *
 */
public class OpenCouchDbDocument extends CouchDbDocument {

	private static final long serialVersionUID = 4252717502666745598L;

	/**
	 * @return a Map containing fields that did not map to any other field in the class during object deserializarion from a JSON document.
	 */
	public Map<String, Object> getAnonymous() {
		return super.extraFields();
	}

}
