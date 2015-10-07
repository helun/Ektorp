package org.ektorp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.databind.*;
import org.ektorp.ViewResult.Row;
import org.ektorp.http.HttpResponse;

/**
 *
 * @author Sverre Kristian Valskrå
 */
public class StreamingViewResult implements Serializable, Iterable<Row>, Closeable{

	private static final String OFFSET_FIELD_NAME = "offset";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final String UPDATE_SEQ_FIELD_NAME = "update_seq";
	private static final long serialVersionUID = 4750290767936801714L;
	private int totalRows = -1;
	private int offset = -1;
	private long sequence = -1;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
	private final BufferedReader reader;

	private final ObjectMapper objectMapper;
	private boolean iteratorCalled;
    private final boolean ignoreNotFound;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
    private final HttpResponse httpResponse;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"DM_DEFAULT_ENCODING", "NP_DEREFERENCE_OF_READLINE_VALUE"})
	public StreamingViewResult(ObjectMapper objectMapper, HttpResponse httpResponse, boolean ignoreNotFound) {

		this.objectMapper = objectMapper;
        this.httpResponse = httpResponse;
        this.ignoreNotFound = ignoreNotFound;
		reader = new BufferedReader(new InputStreamReader(httpResponse.getContent()));
		try{
		String info = reader.readLine();
		totalRows = getFieldIntValue(info, TOTAL_ROWS_FIELD_NAME);
		offset = getFieldIntValue(info, OFFSET_FIELD_NAME);
		sequence = getFieldLongValue(info, UPDATE_SEQ_FIELD_NAME);

		}catch(IOException e) {
			throw new DbAccessException(e);
		}
	}

	/**
	 *
	 * @return -1 if result did not contain an sequence field
	 */
	public long getSequence() {
		return sequence;
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
	public void abort() {
	    httpResponse.abort();
	}
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
		}
	}

	private int getFieldIntValue(String line, String key) {
		int index = line.indexOf(key);
		if (index == -1) {
			return -1;
		}
		int fromIndex = index + key.length() + 2;
		return Integer.parseInt(line.substring(fromIndex, line.indexOf(",", fromIndex)));
	}

	private long getFieldLongValue(String line, String key) {
		int index = line.indexOf(key);
		if (index == -1) {
			return -1;
		}
		int fromIndex = index + key.length() + 2;
		return Long.parseLong(line.substring(fromIndex, line.indexOf(",", fromIndex)));
	}

	private class StreamingViewResultIterator implements Iterator<Row>{
		private Row row;
		private boolean closed = false;
		public boolean hasNext() {
			if (closed) {
				// The BufferedReader is closed. There can't be any more rows.
				return false;
			}
			if (row != null) {
				// We still already have an 'uncollected' row from last time.
				return true;
			}
			try {
			    JsonNode node;
			    do {
    				String doc = reader.readLine();
    				if (doc == null || doc.equals("]}")) {
    					reader.close();
					closed = true;
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
			if (!hasNext()) {
				throw new NoSuchElementException("Attempt to iterate beyond the result set");
			}
			Row toReturn = row;
			row = null;
			return toReturn;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
