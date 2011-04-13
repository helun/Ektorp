package org.ektorp.support;

import java.io.*;


/**
 * Support class for domain entities.
 * 
 * Enforces strict control of the id attribute and implements equals and hashCode that only considers id for equality.
 * 
 * @author henrik lundgren
 *
 */
public class Entity extends CouchDbDocument implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof Entity) {
			Entity a2 = (Entity) o;
			return getId() != null && getId().equals(a2.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : super.hashCode();
	}

}
