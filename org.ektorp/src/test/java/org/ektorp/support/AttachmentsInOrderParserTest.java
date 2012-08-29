package org.ektorp.support;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AttachmentsInOrderParserTest
{
    private static final String[] KNOWN_ORDER = {
            "washington.html",
            "idaho.html",
            "wyoming.html",
            "colorado.html",
            "kansas.html",
            "missouri.html",
            "tennessee.html",
            "georgia.html",
            "florida.html"
    };

    private JsonParser attachmentsJsonParser;
    private List<String> knownOrderList;

    @Before
    public void setUp() throws IOException
    {
        InputStream attachmentsInputStream = getClass().getResourceAsStream("attachments.json");

        JsonFactory jsonFactory = new JsonFactory();
        attachmentsJsonParser = jsonFactory.createJsonParser(attachmentsInputStream);

        knownOrderList = Arrays.asList(KNOWN_ORDER);
    }

    @Test
    public void parseAttachmentNames_should_return_the_attachment_names_in_order() throws IOException
    {
        List<String> attachmentNames = AttachmentsInOrderParser.parseAttachmentNames(attachmentsJsonParser);

        assertThat(attachmentNames, notNullValue());

        assertThat(attachmentNames, is(knownOrderList));
    }
}
