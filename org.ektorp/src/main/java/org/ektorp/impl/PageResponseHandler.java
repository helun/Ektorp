package org.ektorp.impl;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.Page;
import org.ektorp.PageRequest;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author henrik
 *
 * @param <T>
 */
public class PageResponseHandler<T> extends StdResponseHandler<Page<T>> {

	private final QueryResultParser<T> parser;
	private final PageRequest pageRequest;
	private final static Logger LOG = LoggerFactory.getLogger(PageResponseHandler.class);
	
	public PageResponseHandler(PageRequest pr, Class<T> docType, ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		parser = new QueryResultParser<T>(docType, om);
		this.pageRequest = pr;
	}
	
	public PageResponseHandler(PageRequest pr, Class<T> docType, ObjectMapper om,
			boolean ignoreNotFound) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		parser = new QueryResultParser<T>(docType, om);
		parser.setIgnoreNotFound(ignoreNotFound);
		this.pageRequest = pr;
	}
	
	@Override
	public Page<T> success(HttpResponse hr) throws Exception {
		parser.parseResult(hr.getContent());
		List<T> rows = parser.getRows();
		
		int rowsSize = rows.size();
		LOG.debug("got {} rows", rowsSize);
		PageRequest nextLink = (rowsSize == pageRequest.getPageSize() + 1) ? pageRequest.getNextPageRequest(parser.getLastKey(), parser.getLastId()) : null;
		if (nextLink != null) {
			rows.remove(rowsSize-1);
		}
		return new Page<T>(rows, parser.getTotalRows(), pageRequest.getPageSize(), pageRequest.getPreviousPageRequest(), nextLink);
	}
}
