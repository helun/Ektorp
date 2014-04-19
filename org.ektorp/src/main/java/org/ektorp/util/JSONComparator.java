package org.ektorp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.*;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class JSONComparator {

    private static final Map<Class<?>, ValueComparator> valueComparators = createValueComparatorsMap();

    private static <T> Map<Class<?>, ValueComparator> createValueComparatorsMap() {
        Map<Class<?>, ValueComparator> valueComparators = new ClassHierarchyMap<ValueComparator>();
        valueComparators.put(Map.class, new MapComparator());
        valueComparators.put(List.class, new ListComparator());
        valueComparators.put(Object.class, new ObjectComparator());
        return valueComparators;
    }

    private JSONComparator() {
    }

    public static boolean areEqual(String jsonA, String jsonB) {
        return areEqual(new StringReader(jsonA), new StringReader(jsonB));
    }

    public static <T> boolean areEqual(Reader jsonA, Reader jsonB) {
        ObjectMapper om = new ObjectMapper();
        Map<String, T> mapA = null;
        try {
            mapA = om.readValue(jsonA, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, T> mapB = null;
        try {
            mapB = om.readValue(jsonB, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return areEquals(mapA, mapB);
    }

    private static <T> boolean areEquals(Map<String, T> mapA, Map<String, T> mapB) {
        @SuppressWarnings("unchecked")
        ValueComparator<Map> valueComparator = valueComparators.get(Map.class);
        return valueComparator.equals(mapA, mapB);
    }

    private static boolean compareValue(Object aValue, Object bValue) {
        if (aValue == bValue) {
            return true;
        }
        if (bValue == null) {
            return false;
        }
        ValueComparator<Object> vp = getComparator(aValue);
        return vp.equals(aValue, bValue);
    }

    private static <T> ValueComparator<T> getComparator(T aValue) {
        @SuppressWarnings("unchecked")
        ValueComparator<T> vp = valueComparators.get(aValue.getClass());
        if (vp == null) {
            throw new IllegalStateException(format("No value comparator found for class: %s", aValue.getClass()));
        }
        return vp;
    }

    private static interface ValueComparator<T> {

        boolean equals(T a, T b);

    }

    private static class ObjectComparator implements ValueComparator<Object> {

        public boolean equals(Object a, Object b) {
            return a.equals(b);
        }

    }

    private static class MapComparator implements ValueComparator<Map<String, ?>> {

        @SuppressWarnings("unchecked")
        public boolean equals(Map<String, ?> aValueMap, Map<String, ?> bValueMap) {
            if (aValueMap.size() != bValueMap.size()) {
                return false;
            }
            for (Map.Entry<String, ?> entry : aValueMap.entrySet()) {
                Object bValue = bValueMap.get(entry.getKey());
                if (!compareValue(entry.getValue(), bValue)) {
                    return false;
                }
            }
            return true;
        }

    }

    private static class ListComparator implements ValueComparator<List> {

        public boolean equals(List aList, List bList) {
            if (aList.size() != bList.size()) {
                return false;
            }
            for (int i = 0; i < aList.size(); i++) {
                if (!compareValue(aList.get(i), bList.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
