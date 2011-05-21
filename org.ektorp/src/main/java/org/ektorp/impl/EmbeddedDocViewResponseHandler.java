package org.ektorp.impl;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.util.Assert;
/**
 * Reads view result and extracts documents and maps them to the specified type.
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class EmbeddedDocViewResponseHandler<T> extends
		StdResponseHandler<List<T>> {

	private QueryResultParser<T> parser;

	public EmbeddedDocViewResponseHandler(Class<T> docType, ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		parser = new QueryResultParser<T>(docType, om);
	}

	public EmbeddedDocViewResponseHandler(Class<T> docType, ObjectMapper om,
			boolean ignoreNotFound) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		parser = new QueryResultParser<T>(docType, om);
		parser.setIgnoreNotFound(ignoreNotFound);
	}

	@Override
	public List<T> success(HttpResponse hr) throws Exception {
		parser.parseResult(hr.getContent());
		return parser.getRows();
	}

}
