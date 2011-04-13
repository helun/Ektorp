package org.ektorp.impl;

import java.util.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class ThreadLocalBulkBufferHolder {

	private static final ThreadLocal<List<Object>> currentBulkBuffer = new ThreadLocal<List<Object>>();
	
	public void add(Object o) {
		List<Object> buffer = currentBulkBuffer.get();
		if (buffer == null) {
			buffer = new ArrayList<Object>();
			currentBulkBuffer.set(buffer);
		}
		buffer.add(o);
	}
	
	public void clear() {
		currentBulkBuffer.remove();
	}
	
	public List<Object> getCurrentBuffer() {
		List<Object> b = currentBulkBuffer.get(); 
		return b != null ? b : Collections.emptyList();
	}
}
