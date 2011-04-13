package org.ektorp.impl;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class BulkOperationResponseHandler extends StdResponseHandler<List<DocumentOperationResult>> {

	private final ObjectMapper objectMapper;
	private final Collection<?> objects;
	
	public BulkOperationResponseHandler(Collection<?> objects,ObjectMapper om) {
		this.objects = objects;
		this.objectMapper = om;
	}
	
	@Override
	public List<DocumentOperationResult> success(HttpResponse hr)
			throws Exception {
		JsonParser jp = objectMapper.getJsonFactory().createJsonParser(hr.getContent());
		List<DocumentOperationResult> result = new ArrayList<DocumentOperationResult>();
		Iterator<?> objectsIter = objects.iterator();
		while (jp.nextToken() != null) {
			switch (jp.getCurrentToken()) {
				case START_OBJECT:
				jp.nextToken();
				jp.nextToken();
				String id = jp.getText();
				jp.nextToken();
				String nextField = jp.getCurrentName();
				if ("error".equals(nextField)) {
					result.add(readError(jp, objectsIter, id));
				} else {
					setIdAndRevision(jp, objectsIter, id);
				}
			}
		}
		return result;
	}

	private void setIdAndRevision(JsonParser jp, Iterator<?> objectsIter,
			String id) throws IOException, JsonParseException {
		jp.nextToken();
		String rev = jp.getText();
		Object o = objectsIter.next();
		Documents.setId(o, id);
		Documents.setRevision(o, rev);
	}

	private DocumentOperationResult readError(JsonParser jp,
			Iterator<?> objectsIter,
			String id) throws IOException, JsonParseException {
		jp.nextToken();
		String error = jp.getText();
		jp.nextToken();
		jp.nextToken();
		String reason = jp.getText();
		objectsIter.next();
		return DocumentOperationResult.newInstance(id, error, reason);
	}
}
