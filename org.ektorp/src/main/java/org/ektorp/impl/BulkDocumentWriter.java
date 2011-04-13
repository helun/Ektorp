package org.ektorp.impl;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.util.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class BulkDocumentWriter {

	private final ObjectMapper objectMapper;

	public BulkDocumentWriter(ObjectMapper om) {
		objectMapper = om;
	}
	/**
	 * Writes the objects collection as a bulk operation document.
	 * The output stream is flushed and closed by this method.
	 * @param objects
	 * @param allOrNothing
	 * @param out
	 */
	public void write(Collection<?> objects, boolean allOrNothing, OutputStream out) {
		try {
			JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(out, JsonEncoding.UTF8);
			jg.writeStartObject();
			if (allOrNothing) {
				jg.writeBooleanField("all_or_nothing", true);	
			}
			jg.writeArrayFieldStart("docs");
			for (Object o : objects) {
				jg.writeObject(o);
			}
			jg.writeEndArray();
			jg.writeEndObject();
			jg.flush();
			jg.close();
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
}
