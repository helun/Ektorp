package org.ektorp.impl;

import org.ektorp.ViewQuery;
import org.ektorp.http.ResponseCallback;
import org.ektorp.http.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of the executeQuery method of StdCouchDbConnector,
 * as of before the method was delegating to the QueryExecutor strategy interface.
 *
 * Be aware that, as stated in https://github.com/helun/Ektorp/issues/165 this implementation is making use of POST HTTP method in case of multiple keys,
 * so that it may not be appropriate for hosted services like Cloudant where POST are more charged that GET.
 *
*/
public class DefaultQueryExecutor implements QueryExecutor {

    /**
     * This logger is referencing the StdCouchDbConnector class on purpose,
     * because it was like that before the method was extracted from StdCouchDbConnector,
     * and we do not want to change the logging behavior for existing users.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StdCouchDbConnector.class);

    private RestTemplate restTemplate;

    public DefaultQueryExecutor() {
        super();
    }

    public DefaultQueryExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate value) {
        this.restTemplate = value;
    }

    @Override
    public <T> T executeQuery(ViewQuery query, ResponseCallback<T> rh) {
        LOG.debug("Querying CouchDb view at {}.", query);
        T result;
        if (!query.isCacheOk()) {
            result = query.hasMultipleKeys() ? getRestTemplate().postUncached(query.buildQuery(), query.getKeysAsJson(), rh)
                    : getRestTemplate().getUncached(query.buildQuery(), rh);
        } else {
            result = query.hasMultipleKeys() ? getRestTemplate().post(query.buildQuery(), query.getKeysAsJson(), rh)
                    : getRestTemplate().get(query.buildQuery(), rh);
        }
        LOG.debug("Answer from view query: {}.", result);
        return result;
    }
}
