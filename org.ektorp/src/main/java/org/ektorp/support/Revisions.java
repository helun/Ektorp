package org.ektorp.support;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 *
 * @author henrik
 *
 */
public class Revisions extends com.dw.couchdb.dto.Revisions implements Serializable {

	private static final long serialVersionUID = -4563658160451114070L;

	@JsonCreator
	public Revisions(@JsonProperty("start") long start, @JsonProperty("ids") List<String> ids) {
		super(start, ids);
	}
}
