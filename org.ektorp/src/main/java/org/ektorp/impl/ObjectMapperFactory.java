package org.ektorp.impl;

import org.codehaus.jackson.map.*;
import org.ektorp.*;
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
