package org.ektorp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.ViewResult.Row;

/**
 * 
 * @author Sverre Kristian Valskrå
 */
public class StreamingViewResult implements Serializable, Iterable<Row>, Closeable{

	private static final String OFFSET_FIELD_NAME = "offset";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final long serialVersionUID = 4750290767936801714L;
	private int totalRows = -1;
	private int offset = -1;
	private final BufferedReader reader;
	private final ObjectMapper objectMapper;
	private boolean iteratorCalled;
    private final boolean ignoreNotFound;
	
	
	public StreamingViewResult(ObjectMapper objectMapper, InputStream inputStream, boolean ignoreNotFound) {
		
		this.objectMapper = objectMapper;
        this.ignoreNotFound = ignoreNotFound;
		reader = new BufferedReader(new InputStreamReader(inputStream));
		try{
		String info = reader.readLine();
		totalRows = getFieldValue(info, TOTAL_ROWS_FIELD_NAME);
		offset = getFieldValue(info, OFFSET_FIELD_NAME);
	
		}catch(IOException e) {
			throw new DbAccessException(e);
		}
	}
	
	/**
	 * 
	 * @return -1 if result did not contain an offset field
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * 
	 * @return -1 if result did not contain a total_rows field
	 */
	public int getTotalRows() {
		return totalRows;
	}
	
	
	public Iterator<ViewResult.Row> iterator() {
		if (iteratorCalled) {
			throw new IllegalStateException("Iterator can only be called once!");
		}
		iteratorCalled = true;
		return new StreamingViewResultIterator();
	}
	
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
		}
	}
	
	private int getFieldValue(String line, String key) {
		int index = line.indexOf(key);
		if (index == -1) {
			return -1;
		}
		int fromIndex = index + key.length() + 2;
		return Integer.parseInt(line.substring(fromIndex, line.indexOf(",", fromIndex))); 
	}
	
	private class StreamingViewResultIterator implements Iterator<Row>{
		private Row row;
		public boolean hasNext() {
			try {
			    JsonNode node = null;
			    do {
    				String doc = reader.readLine();
    				if (doc == null || doc.equals("]}")) {
    					reader.close();
    					return false;
    				}
    				if (doc.endsWith(",")) {
    					doc = doc.substring(0, doc.length() -1);
    				}
    				node = objectMapper.readTree(doc);
    				
			    }while(ignoreNotFound && node.has(Row.ERROR_FIELD_NAME));
			    
			    row = new ViewResult.Row(node);
				return true;
			} catch (IOException e) {
				throw new DbAccessException(e);
			}
		}
		
		
		
		public Row next() {
			return row;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
}
