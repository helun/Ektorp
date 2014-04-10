package org.ektorp.impl;

import org.ektorp.ViewQuery;
import org.ektorp.http.ResponseCallback;

/**
* Strategy interface that may be implemented in order to inject an alternative implementation into the StdCouchDbConnector.
*/
public interface QueryExecutor {
    <T> T executeQuery(final ViewQuery query, ResponseCallback<T> rh);
}
