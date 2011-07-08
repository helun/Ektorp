package org.ektorp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class PurgeResult {

	private final Map<String, List<String>> purged;
	private final long purgeSeq;
	
	@JsonCreator
	public PurgeResult(@JsonProperty("purged") Map<String, 
			List<String>> purged,@JsonProperty("purge_seq") long purgeSeq) {
		this.purged = purged;
		this.purgeSeq = purgeSeq;
	}
	
	public Map<String, List<String>> getPurged() {
		return purged;
	}
	
	public long getPurgeSeq() {
		return purgeSeq;
	}
	
	private Map<String, Object> anonymous;

	/** 
	 * @return a Map containing fields that did not map to any other field in the class during object deserializarion from a JSON document.
	 */
	@JsonAnyGetter
	public Map<String, Object> getAnonymous() {
		return anonymous();
	}
	
	/**
	 * Exists in order to future proof this class. 
	 * @param key
	 * @param value
	 */
	@JsonAnySetter
	public void setAnonymous(String key, Object value) {
		anonymous().put(key, value);
	}
	
	private Map<String, Object> anonymous() {
		if (anonymous == null) {
			anonymous = new HashMap<String, Object>();
		}
		return anonymous;
	}
}
