package org.ektorp.support;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
/**
 * 
 * @author henrik
 *
 */
public class Revisions implements Serializable {

	private static final long serialVersionUID = -4563658160451114070L;
	private final long start;
	private final List<String> ids;
	
	@JsonCreator
	public Revisions(@JsonProperty("start") long start, @JsonProperty("ids") List<String> ids) {
		this.start = start;
		this.ids = ids;
	}
	/**
	 * @return A list of valid revision IDs, in reverse order (latest first)
	 */
	public List<String> getIds() {
		return ids;
	}
	/**
	 * @return Prefix number for the latest revision
	 */
	public long getStart() {
		return start;
	}
}
