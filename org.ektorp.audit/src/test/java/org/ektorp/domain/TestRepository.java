package org.ektorp.domain;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class TestRepository extends CouchDbRepositorySupport<TestDocument> {

    public TestRepository(Class type, CouchDbConnector db) {
        super(type, db);
        // TODO Auto-generated constructor stub
    }

    public TestRepository(Class type, CouchDbConnector db, boolean createIfNotExists) {
        super(type, db, createIfNotExists);
        // TODO Auto-generated constructor stub
    }

    public TestRepository(Class type, CouchDbConnector db, String designDocName) {
        super(type, db, designDocName);
        // TODO Auto-generated constructor stub
    }

}
