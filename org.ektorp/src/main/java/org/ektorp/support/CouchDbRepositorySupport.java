package org.ektorp.support;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.ektorp.*;
import org.ektorp.impl.*;
import org.ektorp.util.*;
import org.slf4j.*;

/**
 * Provides "out of the box" CRUD functionality for sub classes.
 * 
 * Note that this class will try to access the standard design document named according
 * to this convention:
 * 
 * _design/[repository type simple name]
 * 
 *  e.g. _design/Sofa if this repository's handled type is foo.bar.Sofa
 *  
 *  It is preferable that this design document must define a view named "all".
 *  The "all"-view should only return document id's that refer to documents that can be loaded as this repository's handled type.
 * 
 * @author henrik lundgren
 * @param <T>
 */
public class CouchDbRepositorySupport<T> implements GenericRepository<T> {
	/**
	 * System property key: org.ektorp.support.AutoUpdateViewOnChange
	 */
	public static final String AUTO_UPDATE_VIEW_ON_CHANGE = "org.ektorp.support.AutoUpdateViewOnChange";
	protected static final Logger log = LoggerFactory.getLogger(CouchDbRepositorySupport.class);
	protected final CouchDbConnector db;
	protected final Class<T> type;
	
	protected final String stdDesignDocumentId;
	
	private DesignDocumentFactory designDocumentFactory; 
	
	protected CouchDbRepositorySupport(Class<T> type, CouchDbConnector db) {
		this(type, db, true);
	}
	
	protected CouchDbRepositorySupport(Class<T> type, CouchDbConnector db, boolean createIfNotExists) {
		Assert.notNull(db, "CouchDbConnector may not be null");
		Assert.notNull(type);
		this.db = db;
		this.type = type;
		if (createIfNotExists) {
			db.createDatabaseIfNotExists();	
		}
		stdDesignDocumentId = NameConventions.designDocName(type);
	}
	/**
	 * Alternative constructor allowing a custom design document name (not linked to the type class name)
	 * @param type
	 * @param db
	 * @param designDocName
	 */
	protected CouchDbRepositorySupport(Class<T> type, CouchDbConnector db, String designDocName) {
		Assert.notNull(db, "CouchDbConnector may not be null");
		Assert.notNull(type);
		this.db = db;
		this.type = type;
		db.createDatabaseIfNotExists();
		stdDesignDocumentId = NameConventions.designDocName(designDocName);
	}
	/**
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	public void add(T entity) {
		assertEntityNotNull(entity);
		Assert.isTrue(Documents.isNew(entity), "entity must be new");
		db.create(entity);
	}

	/**
	 * If the repository's design document has a view named "all" it will be used
	 * to fetch all documents of this repository's handled type.
	 * 
	 * "all" must return document ids that refers documents that are readable by this repository.
	 * 
	 * If the "all"-view is not defined, all documents in the database (except design documents)
	 * will be fetched. In this case the database must only contain documents that are readable by
	 * this repository.
	 * 
	 * @return all objects of this repository's handled type in the db.
	 */
	public List<T> getAll() {
		if (designDocContainsAllView()) {
			return queryView("all");
		}
		return loadAllByAllDocIds();
	}

	private boolean designDocContainsAllView() {
		if (db.contains(stdDesignDocumentId)) {
			DesignDocument dd = db.get(DesignDocument.class, stdDesignDocumentId);
			return dd.containsView("all");
		}
		return false;
	}

	private List<T> loadAllByAllDocIds() {
		List<String> ids = db.getAllDocIds();
		List<T> all = new ArrayList<T>(ids.size());
		for (String id : ids) {
			if (!id.startsWith("_design")) {
				all.add(get(id));
			}
		}
		return all;
	}
	/**
	 * 
	 * @param id
	 * @return
	 * @throws DocumentNotFoundException if the document was not found.
	 */
	public T get(String id) {
		return db.get(type, id);
	}
	/**
	 * 
	 * @param id
	 * @param options
	 * @return
	 * @throws DocumentNotFoundException if the document was not found.
	 */
	public T get(String id, Options options) {
		return db.get(type, id, options);
	}
	/**
	 * 
	 * @param id
	 * @param rev
	 * @return
	 * @throws DocumentNotFoundException if the document was not found.
	 * @deprecated use get(String id, Options options)
	 */
	public T get(String id, String rev) {
		return db.get(type, id, rev);
	}

	public void remove(T entity) {
		assertEntityNotNull(entity);
		db.delete(Documents.getId(entity), Documents.getRevision(entity));
	}
	/**
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	public void update(T entity) {
		assertEntityNotNull(entity);
		db.update(entity);
	}

	private void assertEntityNotNull(T entity) {
		Assert.notNull(entity, "entity may not be null");
	}
	
	/**
	 * Creates a ViewQuery pre-configured with correct dbPath, design document id and view name.
	 * @param viewName
	 * @return
	 */
	protected ViewQuery createQuery(String viewName) {
		return new ViewQuery()
				.dbPath(db.path())
				.designDocId(stdDesignDocumentId)
				.viewName(viewName);
	}
	/**
	 * Allows subclasses to query views with simple String value keys
	 * and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * @param viewName
	 * @param key
	 * @return
	 */
	protected List<T> queryView(String viewName, String key) {
		return db.queryView(createQuery(viewName)
								.includeDocs(true)
								.key(key),
							type);
	}
	/**
	 * Allows subclasses to query views with simple String value keys
	 * and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * @param viewName
	 * @param keyValue
	 * @return
	 */
	protected List<T> queryView(String viewName, int key) {
		return db.queryView(createQuery(viewName)
								.includeDocs(true)
								.key(key),
							type);
	}
	/**
	 * Allows subclasses to query views with simple String value keys
	 * and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * @param viewName
	 * @param key
	 * @return
	 */
	protected List<T> queryView(String viewName, ComplexKey key) {
		return db.queryView(createQuery(viewName)
								.includeDocs(true)
								.key(key),
							type);
	}
	/**
	 * Allows subclasses to query a view and load the result as the repository's handled type.
	 * 
	 * The viewName must be defined in this repository's design document.
	 * 
	 * @param viewName
	 * @return
	 */
	protected List<T> queryView(String viewName) {
		return db.queryView(createQuery(viewName)
								.includeDocs(true),
							type);
	}
	/**
	 * <p>
	 * Will create the standard design document if it does not exists in the database.
	 * </p>
	 * <p>
	 * Will also generate view definitions for finder methods defined in this class and annotated by the @GenerateView
	 * annotation. The method name must adhere to the name convention of findBy[Property].
	 * </p>
	 * <p>
	 * The method:
	 * </p>
	 * <code>
	 * <pre>
	 * @GenerateView
	 * public List<Sofa> findByColor(String s) {
	 * 	return queryView("by_color", s);
	 * }
	 * </pre>
	 * </code>
	 * <p>
	 * Will result in a generated view named "by_color" in the document _design/Sofa
	 * </p>
	 * <p>
	 * Any existing view with the same name will be kept unchanged.
	 * 
 	 * TIP: The generated DesignDocument will be written to the log if debug log level is enabled.
	 * </p>
	 */
	public void initStandardDesignDocument() {
		initDesignDocInternal(0);
	}
	
	private void initDesignDocInternal(int invocations) {
		DesignDocument designDoc;
		if (db.contains(stdDesignDocumentId)) {
			designDoc = db.get(DesignDocument.class, stdDesignDocumentId);
		} else {
			designDoc = new DesignDocument(stdDesignDocumentId);
		}
		log.debug("Generating DesignDocument for {}", getHandledType());
		DesignDocument generated = getDesignDocumentFactory().generateFrom(this);
		boolean changed = designDoc.mergeWith(generated);
		if (log.isDebugEnabled()) {
			debugDesignDoc(designDoc);
		}
		if (changed) {
			log.debug("DesignDocument changed or new. Updating database");
			try {
				db.update(designDoc);	
			} catch (UpdateConflictException e) {
				log.warn("Update conflict occurred when trying to update design document: {}", designDoc.getId());
				if (invocations == 0) {
					backOff();
					log.info("retrying initStandardDesignDocument for design document: {}", designDoc.getId());
					initDesignDocInternal(1);
				}
			}
		} else if (log.isDebugEnabled()){
			log.debug("DesignDocument was unchanged. Database was not updated.");
		}
	}
	/**
	 * Wait a short while in order to prevent racing initializations from other repositories.
	 */
	private void backOff() {
		try {
			Thread.sleep(new Random().nextInt(400));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			return;
		}
	}
	
	protected void debugDesignDoc(DesignDocument generated) {
		ObjectMapper om = new ObjectMapper();
		om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);		
		om.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
		try {
			String json = om.writeValueAsString(generated);
			log.debug("DesignDocument source:\n" + json);
		} catch (Exception e) {
			log.error("Could not write generated design document as json", e);
		}
	}

	public boolean contains(String docId) {
		return db.contains(docId);
	}
	
	public void setDesignDocumentFactory(
			DesignDocumentFactory df) {
		this.designDocumentFactory = df;
	}
	
	protected DesignDocumentFactory getDesignDocumentFactory() {
		if (designDocumentFactory == null) {
			designDocumentFactory = new StdDesignDocumentFactory();
		}
		return designDocumentFactory;
	}
	
	Class<?> getHandledType() {
		return type;
	}
}
