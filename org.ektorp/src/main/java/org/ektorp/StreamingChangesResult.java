package org.ektorp;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpResponse;
import org.ektorp.impl.changes.StdDocumentChange;

/**
 *
 * @author Sverre Kristian Valskrå
 */
public class StreamingChangesResult implements Serializable, Iterable<DocumentChange>, Closeable{

	private static final long serialVersionUID = 4750290767936801714L;
	private boolean iteratorCalled;
	private JsonParser jp;
	private long lastSeq = -1l;
    private final HttpResponse response;
	public StreamingChangesResult(ObjectMapper objectMapper, HttpResponse response) {
		this.response = response;
        try{
		    jp = objectMapper.getJsonFactory().createJsonParser(response.getContent());
		    jp.nextValue();
		    jp.nextValue();
		    jp.nextToken();
    	}catch(Exception e) {
            throw new DbAccessException(e);
        }
	}


	public Iterator<DocumentChange> iterator() {
		if (iteratorCalled) {
			throw new IllegalStateException("Iterator can only be called once!");
		}
		iteratorCalled = true;
		return new StreamingResultIterator();
	}

	/**
	 * Aborts the stream immediately
	 */
	public void abort() {
	    try{
	    response.abort();
	    }finally {
	        close();
	    }
	}
	public void close() {
		try {
			jp.close();
		} catch (IOException e) {
		}
	}

	/**
	 * This method can only be called after stream is fully red
	 *
	 * @return
	 */
	public long getLastSeq() {
	    if (lastSeq == -1) {
	        throw new IllegalStateException("Last seq can only be called after stream is fully iterated");
	    }
        return lastSeq;
    }


	private class StreamingResultIterator implements Iterator<DocumentChange>{
		private DocumentChange row;
		public boolean hasNext() {
			try {
			    JsonNode jsonNode = jp.readValueAs(JsonNode.class);
			    if (jsonNode == null) {
			        jsonNode = jp.readValueAs(JsonNode.class);
			        lastSeq = jsonNode.get("last_seq").longValue();
			        close();
			        return false;
			    }
			    row = new StdDocumentChange(jsonNode);

				return true;
			} catch (Exception e) {
				throw new DbAccessException(e);
			}
		}

		public DocumentChange next() {
			return row;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
