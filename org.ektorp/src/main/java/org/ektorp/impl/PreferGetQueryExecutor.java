package org.ektorp.impl;

import org.ektorp.ViewQuery;
import org.ektorp.http.ResponseCallback;
import org.ektorp.http.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@code executeQuery} method of {@link StdCouchDbConnector},
 * from the discussion in <a href="https://github.com/helun/Ektorp/issues/165">Ektorp #165</a>,
 * which prefers {@code GET} HTTP method even in case of multiple keys.
 * It is more appropriate for hosted services like <a href="http://cloudant.com/">Cloudant</a>
 * where {@code POST} requests are charged more than {@code GET}.
 *
 * <p>However, if the HTTP request length exceeds {@value #MAX_KEYS_LENGTH_FOR_GET} characters,
 * it will use {@code POST} HTTP method.
 * 
 * @author Hendy Irawan <ceefour666@gmail.com> 
 */
public class PreferGetQueryExecutor implements QueryExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PreferGetQueryExecutor.class);

    /**
     * Maximum length of {@link ViewQuery#getKeysAsJsonArray()} for a
     * {@code GET} HTTP request in {@link #executeQuery(ViewQuery, ResponseCallback)},
     * otherwise uses {@code POST}. 
     */
    public static final int MAX_KEYS_LENGTH_FOR_GET = 3000;

    private RestTemplate restTemplate;

    public PreferGetQueryExecutor() {
        super();
    }

    public PreferGetQueryExecutor(RestTemplate restTemplate) {
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
			if (query.hasMultipleKeys()) {
				final String keysAsJsonArray = query.getKeysAsJsonArray();
				result = keysAsJsonArray.length() > MAX_KEYS_LENGTH_FOR_GET
						? restTemplate.postUncached(query.buildQuery(), "{\"keys\":" + keysAsJsonArray + "}", rh)
						: restTemplate.getUncached(query.buildQuery(keysAsJsonArray), rh);
			} else {
				result = restTemplate.getUncached(query.buildQuery(), rh);
			}
		} else {
			if (query.hasMultipleKeys()) {
				final String keysAsJsonArray = query.getKeysAsJsonArray();
				result = keysAsJsonArray.length() > MAX_KEYS_LENGTH_FOR_GET
						? restTemplate.post(query.buildQuery(), "{\"keys\":" + keysAsJsonArray + "}", rh)
						: restTemplate.get(query.buildQuery(keysAsJsonArray), rh);
			} else {
				result = restTemplate.get(query.buildQuery(), rh);
			}
		}
        LOG.debug("Answer from view query: {}.", result);
        return result;
    }

}
