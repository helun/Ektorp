package org.ektorp.impl;

import java.util.Collections;
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
		if (pageRequest.isBack()) {
			Collections.reverse(rows);
		}
		int offset = pageRequest.isBack() ? 1 : 1;
		
		String nextId = parser.getLastId();
		JsonNode nextKey = parser.getLastKey();
		
		
		PageRequest.Builder b = pageRequest.nextRequest(nextKey, nextId);
		int currentPage = b.getPageNo();
		
		PageRequest nextRequest = b.page(currentPage + 1).build();
		PageRequest previousRequest = currentPage == 1 ? PageRequest.firstPage(pageRequest.getPageSize()) :
															b.back(true).page(currentPage - 1).build();
		
		boolean hasMore = rowsSize == pageRequest.getPageSize() + offset;
		if (hasMore) {	
			rows.remove(rows.size() - 1);
		} else if (!pageRequest.isBack()) {
			nextRequest = null;
		}
		if (currentPage == 0) {
			previousRequest = null;
		}
		return new Page<T>(rows, parser.getTotalRows(), pageRequest.getPageSize(), previousRequest, nextRequest);
	}
}
