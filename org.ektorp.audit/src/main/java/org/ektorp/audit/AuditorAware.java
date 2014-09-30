package org.ektorp.audit;

/**
 * Interface for components that are aware of the application's current auditor. This will be some kind of user id mostly.
 * 
 * @param String the id of the auditing instance
 * @author Eric Benzacar
 */
public interface AuditorAware<T> {

	/**
	 * Returns the current auditor of the application.  The AuditingHandler stores this auditor
	 * in the document's audit fields
	 * 
	 * @return the current auditor
	 */
	T getCurrentAuditor();
}
