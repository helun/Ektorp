package org.ektorp.util;

import org.ektorp.*;

/**
 * 
 * @author Henrik Lundgren
 * created 1 nov 2009
 *
 */
public final class Exceptions {

	private Exceptions() {}
	
	public static RuntimeException propagate(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new DbAccessException(e);
	}
	
	public static RuntimeException newRTE(String format, Object... args) {
		return new RuntimeException(String.format(format, args));
	}
	
}
