package org.ektorp;

import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.node.*;
/**
 * Class for creating complex keys for view queries.
 * The keys's components can consists of any JSON-encodeable objects, but are most likely to be Strings and Integers.
 * @author henrik lundgren
 *
 */
public class ComplexKey {
	
	private final static ObjectMapper mapper = new ObjectMapper();
	
	private final List<Object> components;
	
	private static final Object EMPTY_OBJECT = new Object();
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	public static ComplexKey of(Object... components) {
		return new ComplexKey(components);
	}
	/**
	 * Add this Object to the key if an empty object definition is desired:
	 * ["foo",{}]
	 * @return an object that will serialize to {}
	 */
	public static Object emptyObject() {
		return EMPTY_OBJECT;
	}
	/**
	 * Add this array to the key if an empty array definition is desired:
	 * [[],"foo"]
	 * @return an object array that will serialize to []
	 */
	public static Object[] emptyArray() {
		return EMPTY_ARRAY;
	}
	
	private ComplexKey(Object[] components) {
		this.components = Arrays.asList(components);
	}
	
	@JsonValue
	public JsonNode toJson() {
		ArrayNode key = mapper.createArrayNode();
		for (Object component : components) {
			if (component == EMPTY_OBJECT) {
				key.addObject();
			} else {
				key.addPOJO(component);
			}
		}
		return key;
	}
}
