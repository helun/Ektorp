package org.ektorp;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class ReplicatorDocument_UserContextTest
{
    private ObjectMapper objectMapper;

    @Before
    public void setUp()
    {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void getUnknownFields_should_return_an_empty_map_if_there_were_no_fields_which_were_not_mapped() throws IOException
    {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("name", UUID.randomUUID().toString());

        String jsonString = objectMapper.writeValueAsString(jsonMap);

        ReplicatorDocument.UserContext document = objectMapper.readValue(jsonString, ReplicatorDocument.UserContext.class);
        assertThat(document.getUnknownFields(), notNullValue());
        assertThat(document.getUnknownFields().isEmpty(), is(true));
    }

    @Test
    public void getUnknownFields_should_return_fields_which_were_not_mapped() throws IOException
    {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        String unknownField = UUID.randomUUID().toString();
        jsonMap.put(unknownField, UUID.randomUUID().toString());

        String jsonString = objectMapper.writeValueAsString(jsonMap);

        ReplicatorDocument.UserContext document = objectMapper.readValue(jsonString, ReplicatorDocument.UserContext.class);
        assertThat(document.getUnknownFields(), notNullValue());
        assertThat(document.getUnknownFields().containsKey(unknownField), is(true));
        assertThat(document.getUnknownFields().get(unknownField), is(jsonMap.get(unknownField)));
    }
}
