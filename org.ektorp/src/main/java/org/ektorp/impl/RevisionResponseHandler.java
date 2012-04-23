package org.ektorp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
/**
 * Extracts the document revision if the operation was successful
 * @author henrik lundgren
 *
 */
public class RevisionResponseHandler extends StdResponseHandler<DocumentOperationResult> {

	ObjectMapper objectMapper;

	public RevisionResponseHandler(ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper cannot be null");
		objectMapper = om;
	}

	@Override
	public DocumentOperationResult success(HttpResponse hr) throws Exception {
		return objectMapper.readValue(hr.getContent(), DocumentOperationResult.class);
	}
}
