package org.ektorp.docref;

import java.lang.annotation.*;

import org.codehaus.jackson.annotate.*;

/**
 * Follow references to other documents when loading and updating this
 * collection based on the result of a view query. The parameter
 * <code>view</code> defines the name of a view located in the design document
 * of the class or in the design document specified by <code>designDoc</code>.
 * The parameter <code>fetch</code> controls when reference loading is
 * performed. This annotation only has meaning on collection class members.
 * 
 * @author ragnar rova
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface DocumentReferences {

	/**
	 * Controls when referenced documents are loaded. Default is
	 * <code>LAZY</code> and implies that references should be loaded when
	 * a method on the collection is accessed which needs the documents.
	 * <code>EAGER</code> means that all references of arbitrary depth will be
	 * followed directly at load time.
	 */
	public FetchType fetch() default FetchType.LAZY;

	/**
	 * If view is left unspecified a view will be generated for this document reference
	 * (given the persistent type is managed by a repository class based on org.ektorp.support.CouchDbRepositorySupport).
	 * 
	 * View name for backward references. Backward references are stored in the
	 * documents on the other side of the reference. References are loaded into
	 * this collection by performing a view query against the specified view
	 * with the document id of the declaring class.
	 */
	public String view() default "";

	/**
	 * Design document of the view specified by <code>view</code>
	 */
	public String designDoc() default "";
	/**
	 * Defines the sort direction of the loaded collection.
	 */
	public boolean descendingSortOrder() default false;
	/**
	 * The sort order  of the loaded collection can be specified by this parameter.
	 * This parameter will refer to a field in the child docs.
	 * Note: This parameter is only applied if view is not specified. 
	 */
	public String orderBy() default "";
	/**
	 * The name of the back referring field in the child must be specified here. 
	 * Note: This parameter is only applied if view is not specified. 
	 */
	public String backReference() default "";
	/**
	 * Set the type if cascade behaviour this collection should have.
	 * @return
	 */
	public CascadeType[] cascade() default {CascadeType.NONE};

}
