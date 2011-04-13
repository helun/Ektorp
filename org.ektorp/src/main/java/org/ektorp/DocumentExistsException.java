package org.ektorp;


/**
 * 
 * @author henrik lundgren
 * @deprecated UpdateConflictException is now thrown if the document already exists.
 */
@Deprecated
public class DocumentExistsException extends DbAccessException {

	private static final long serialVersionUID = 1L;

	public DocumentExistsException(String id) {
		super(String.format("A document with id %s already exists", id));
	}
}
