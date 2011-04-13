package org.ektorp.support;
/**
 * 
 * @author henrik lundgren
 *
 */
public class ViewGenerationException extends RuntimeException {

	private static final long serialVersionUID = -1442749478483625619L;

	public ViewGenerationException(String message) {
		super(message);
	}
	
	public ViewGenerationException(String format, Object... args) {
		super(String.format(format, args));
	}
}
