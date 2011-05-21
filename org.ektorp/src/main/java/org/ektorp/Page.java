package org.ektorp;

import java.util.Iterator;
import java.util.List;

public class Page<T> implements Iterable<T> {

	private final int totalSize;
	private final int pageSize;
	private final PageRequest previousPageRequest;
	private final PageRequest nextPageRequest;
	
	private final List<T> rows;
	
	public Page(List<T> rows, int totalSize, int pageSize, PageRequest previousPageRequest,  PageRequest nextPageRequest) {
		this.rows = rows;
		this.totalSize = totalSize;
		this.pageSize = pageSize;
		this.previousPageRequest = previousPageRequest;
		this.nextPageRequest = nextPageRequest;
	}
	
	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}
	/**
	 * 
	 * Exists mainly for providing compatibility with the c:forEach in jstl (1.2) as it cannot handle java.lang.Iterable
	 * @return
	 */
	public List<T> getRows() {
		return rows;
	}
	/**
	 * Has a silly name in order to conform with the java beans naming convention
	 * @return true if there is a next page
	 */
	public boolean isHasNext() {
		return nextPageRequest != null;
	}
	/**
	 * Has a silly name in order to conform with the java beans naming convention
	 * @return true if there is a previous page
	 */
	public boolean isHasPrevious() {
		return previousPageRequest != null;
	}
	/**
	 * @return the total number of items across all pages
	 */
	public int getTotalSize() {
		return totalSize;
	}
	/**
	 * 
	 * @return the maximum number of items this page may contain.
	 *  
	 */
	public int getPageSize() {
		return pageSize;
	}
	/**
	 * @return the next page request encoded in a URL-save string.
	 * @see org.ektorp.PageRequest
	 * @throws java.lang.IllegalStateException of no next page exists
	 */
	public String getNextLink() {
		if (!isHasNext()) {
			throw new IllegalStateException("Cannot create next link as no next page exists");
		}
		return nextPageRequest.asLink();
	}
	
	public PageRequest getNextPageRequest() {
		return nextPageRequest;
	}
	
	public PageRequest getPreviousPageRequest() {
		return previousPageRequest;
	}
	/**
	 * @return the previous page request encoded in a URL-save string.
	 * @see org.ektorp.PageRequest
	 * @throws java.lang.IllegalStateException of no previous page exists
	 */
	public String getPreviousLink() {
		if (!isHasPrevious()) {
			throw new IllegalStateException("Cannot create previous link as no previous page exists");
		}
		return previousPageRequest.asLink();
	}
	
	public int size() {
		return rows.size();
	}
	
}
