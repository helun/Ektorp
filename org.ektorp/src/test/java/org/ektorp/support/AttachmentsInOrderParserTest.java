package org.ektorp.support;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

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
        JsonFactory jsonFactory = new JsonFactory();

        InputStream attachmentsInputStream = null;
        try {
            attachmentsInputStream = getClass().getResourceAsStream("attachments.json");
            attachmentsJsonParser = jsonFactory.createParser(attachmentsInputStream);
        } finally {
            IOUtils.closeQuietly(attachmentsInputStream);
        }

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
