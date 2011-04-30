package org.ektorp;

import java.util.Iterator;
import java.util.List;

public class Page<T> implements Iterable<T> {

	private final int totalSize;
	private final int pageSize;
	private final PageRequest previousLink;
	private final PageRequest nextLink;
	
	private final List<T> rows;
	
	public Page(List<T> rows, int totalSize, int pageSize, PageRequest previousLink, PageRequest nextLink) {
		this.rows = rows;
		this.totalSize = totalSize;
		this.pageSize = pageSize;
		this.previousLink = previousLink;
		this.nextLink = nextLink;
	}
	
	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}
	
	public List<T> getRows() {
		return rows;
	}
	
	public boolean hasNext() {
		return nextLink != null;
	}
	
	public boolean hasPrevious() {
		return previousLink != null;
	}
	
	public int getTotalSize() {
		return totalSize;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public String getNextLink() {
		if (!hasNext()) {
			throw new IllegalStateException("Cannot create next link as no next page exists");
		}
		return nextLink.asLink();
	}
	
	public String getPreviousLink() {
		if (!hasPrevious()) {
			throw new IllegalStateException("Cannot create previous link as no previous page exists");
		}
		return previousLink.asLink();
	}
	
}
