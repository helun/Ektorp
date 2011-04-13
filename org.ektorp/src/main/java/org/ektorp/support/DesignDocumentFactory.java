package org.ektorp.support;
/**
 * 
 * @author henrik lundgren
 *
 */
public interface DesignDocumentFactory {
	/**
	 * Generates a design document with views, lists, shows and filters generated and loaded
	 * according to the annotations found in the metaDataSource object.
	 * 
	 * @param metaDataSource
	 * @return
	 */
	DesignDocument generateFrom(Object metaDataSource);

}