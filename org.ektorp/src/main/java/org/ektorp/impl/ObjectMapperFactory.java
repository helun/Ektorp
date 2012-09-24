package org.ektorp.impl;

import org.ektorp.*;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 *
 * @author henrik lundgren
 *
 */
public interface ObjectMapperFactory {
	/**
	 *
	 * @return
	 */
	ObjectMapper createObjectMapper();
	/**
	 *
	 * @param connector
	 * @return
	 * @throws JsonMappingException
	 */
	ObjectMapper createObjectMapper(CouchDbConnector connector);
}
