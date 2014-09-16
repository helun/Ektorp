package org.ektorp;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Standard status response
 *
 * @author henrik lundgren
 *
 */
public class Status implements Serializable {

	private static final long serialVersionUID = 6617269292660336903L;

	@JsonProperty("ok")
	boolean ok;

	private Map<String, Object> unknownFields;

	public boolean isOk() {
		return ok;
	}

	private Map<String, Object> unknown() {
		if (unknownFields == null) {
			unknownFields = new HashMap<String, Object>();
		}
		return unknownFields;
	}

	@JsonAnySetter
	public void setUnknown(String key, Object value) {
		unknown().put(key, value);
	}

	public Object getField(String key) {
		return unknown().get(key);
	}
}
