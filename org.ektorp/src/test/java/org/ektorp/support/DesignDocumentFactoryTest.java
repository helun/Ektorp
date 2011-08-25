package org.ektorp.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class DesignDocumentFactoryTest {

    DesignDocumentFactory factory = new StdDesignDocumentFactory();

    @Test
    public void testGenerateFrom() {
        DesignDocument dd = factory.generateFrom(new MetaDataClass());
        assertEquals(3, dd.getLists().size());
        assertThatFunctionExists(dd.getLists(), "list_1");
        assertThatFunctionExists(dd.getLists(), "list_2");
        assertThatFunctionExists(dd.getLists(), "list_3");

        assertEquals(3, dd.getShows().size());
        assertThatFunctionExists(dd.getShows(), "show_1");
        assertThatFunctionExists(dd.getShows(), "show_2");
        assertThatFunctionExists(dd.getShows(), "show_3");

        assertEquals(3, dd.getFilters().size());
        assertThatFunctionExists(dd.getFilters(), "filter_1");
        assertThatFunctionExists(dd.getFilters(), "filter_2");
        assertThatFunctionExists(dd.getFilters(), "filter_3");

        assertEquals(3, dd.getUpdates().size());
        assertThatFunctionExists(dd.getUpdates(), "update_1");
        assertThatFunctionExists(dd.getUpdates(), "update_2");
        assertThatFunctionExists(dd.getUpdates(), "update_3");

    }

    private void assertThatFunctionExists(Map<String, String> functions, String name) {
        assertTrue(functions.containsKey(name));
        assertTrue(functions.get(name).length() > 0);
    }

    @Lists({
            @ListFunction(name = "list_1", function = "function(head, req) { /*...*/ }"),
            @ListFunction(name = "list_2", function = "function(head, req) { /*...*/ }"),
            @ListFunction(name = "list_3", file = "list_func.js")
    })
    @Shows({
            @ShowFunction(name = "show_1", function = "function(doc, req) { /*...*/ }"),
            @ShowFunction(name = "show_2", function = "function(doc, req) { /*...*/ }"),
            @ShowFunction(name = "show_3", file = "show_func.js")
    })
    @Filters({
            @Filter(name = "filter_1", function = "function(doc, req) { /*...*/ }"),
            @Filter(name = "filter_2", function = "function(doc, req) { /*...*/ }"),
            @Filter(name = "filter_3", file = "filter.js")
    })
    @UpdateHandlers({
            @UpdateHandler(name = "update_1", function = "function(doc, req) {/*...*/}"),
            @UpdateHandler(name = "update_2", function = "function(doc, req) {/*...*/}"),
            @UpdateHandler(name = "update_3", function = "update.js")
    })
    public static class MetaDataClass {

        public String someFoo() {
            return "";
        }

    }

}
