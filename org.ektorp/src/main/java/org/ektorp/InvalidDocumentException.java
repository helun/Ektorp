package org.ektorp;
/**
 * 
 * @author henrik lundgren
 *
 */
public class InvalidDocumentException extends DbAccessException {

	private static final long serialVersionUID = 1L;
	
	public InvalidDocumentException(Class<?> offendingClass, String missingField) {
		super(String.format("Cannot resolve %s in %s", missingField, offendingClass));
	}

}
