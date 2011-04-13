package org.ektorp;

import java.io.*;

import org.codehaus.jackson.annotate.*;
import org.ektorp.util.*;

/**
 * 
 * @author Henrik Lundgren
 * created 30 okt 2009
 *
 */
public class Revision implements Serializable {

	private static final long serialVersionUID = -1740321573214780237L;
	private final String rev;
	private final String status;
	
	@JsonCreator
	public Revision(@JsonProperty("rev") String rev,@JsonProperty("status") String status) {
		Assert.hasText(rev, "revision cannot be empty");
		Assert.hasText(status, "status cannot be empty");
		this.rev = rev;
		this.status = status;
	}

	public String getRev() {
		return rev;
	}

	public String getStatus() {
		return status;
	}
	
	public boolean isMissing() {
		return "missing".equals(status);
	}
	
	public boolean isOnDisk() {
		return "disk".equals(status);
	}
	
	public boolean isDeleted() {
		return "deleted".equals(status);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof Revision) {
			Revision r = (Revision) o;
			return rev.equals(r.rev);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return rev.hashCode();
	}
}
