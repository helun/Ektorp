package org.ektorp.util;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.map.*;

public class JSONComparator {

	private final static String UTF_8 = "UTF-8";
	
	private static Map<Class<?>, ValueComparator> valueComparators = new ClassHierarchyMap<ValueComparator>();
	static {
		valueComparators.put(Map.class, new MapComparator());
		valueComparators.put(List.class, new ListComparator());
		valueComparators.put(Object.class, new ObjectComparator());
	}
	
	private JSONComparator() {}
	
	@SuppressWarnings("unchecked")
	public static boolean areEqual(String jsonA, String jsonB) {
		ObjectMapper om = new ObjectMapper();
		try {
			Map<String, ?> mapA = om.readValue(IOUtils.toInputStream(jsonA, UTF_8), Map.class);
			Map<String, ?> mapB = om.readValue(IOUtils.toInputStream(jsonB, UTF_8), Map.class);
			return areEquals(mapA, mapB);
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

	private static boolean areEquals(Map<String, ?> mapA, Map<String, ?> mapB) {
		return valueComparators.get(Map.class).equals(mapA,mapB);
	}
	
	private static boolean compareValue(Object aValue, Object bValue) {
		if (bValue == null) {
			return false;
		}		
		ValueComparator vp = getComparator(aValue);
		if (!vp.equals(aValue, bValue)) {
			return false;
		}
		return true;
	}

	private static ValueComparator getComparator(Object aValue) {
		ValueComparator vp = valueComparators.get(aValue.getClass());
		if (vp == null) {
			throw new IllegalStateException(format("No value comparator found for class: %s", aValue.getClass()));
		}
		return vp;
	}

	private static interface ValueComparator {
		
		boolean equals(Object a, Object b);
		
	}
	
	private static class ObjectComparator implements ValueComparator {

		public boolean equals(Object a, Object b) {
			return a.equals(b);
		}
		
	}
	
	private static class MapComparator implements ValueComparator {

		@SuppressWarnings("unchecked")
		public boolean equals(Object a, Object b) {
			if (!(a instanceof Map)) {
				throw new IllegalArgumentException("Object a is not a Map");
			}
			if (b instanceof Map) {
				Map<String, ?> aValueMap = (Map<String, ?>) a;
				Map<String, ?> bValueMap = (Map<String, ?>) b;
				if (aValueMap.size() != bValueMap.size()) {
					return false;
				}
				for(Map.Entry<String, ?> entry : aValueMap.entrySet()) {
					Object bValue = bValueMap.remove(entry.getKey());
					if (!compareValue(entry.getValue(), bValue)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		
	}
	
	private static class ListComparator implements ValueComparator {

		public boolean equals(Object a, Object b) {
			if (!(a instanceof List)) {
				throw new IllegalArgumentException("Object a is not a List");
			}
			if (b instanceof List) {
				List<?> aList = (List<?>) a;
				List<?> bList = (List<?>) b;
				if (aList.size() != bList.size()) {
					return false;
				}
				for (int i = 0; i < aList.size(); i++) {
					if (!compareValue(aList.get(i), bList.get(i))) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}
}
