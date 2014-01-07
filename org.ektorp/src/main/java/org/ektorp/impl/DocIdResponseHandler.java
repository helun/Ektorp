package org.ektorp.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.DbAccessException;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author henrik lundgren
 *
 */
public class DocIdResponseHandler extends StdResponseHandler<List<String>> {

	private final JsonFactory jsonFactory;

	public DocIdResponseHandler(ObjectMapper om) {
		jsonFactory = om.getFactory();
	}

    /**
     * The token is required to be on the START_ARRAY value for rows.
     * @param jp
     * @param result
     * @return The results found in the rows object
     */
    private List<String> parseRows(JsonParser jp, List<String> result) throws IOException {
        while(jp.nextToken() == JsonToken.START_OBJECT) {
            while(jp.nextToken() == JsonToken.FIELD_NAME)
            {
                String fieldName = jp.getCurrentName();
                jp.nextToken();
                if ("id".equals(fieldName)) {
                    result.add(jp.getText());
                } else {
                    jp.skipChildren();
                }
            }
        }
        return result;
    }

    @Override
    public List<String> success(HttpResponse hr) throws Exception {
        JsonParser jp = jsonFactory.createParser(hr.getContent());
        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new DbAccessException("Expected data to start with an Object");
        }

        List<String> result = null;

        while (jp.nextToken() != null) {
            if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                String fieldName = jp.getCurrentName();
                if ("total_rows".equals(fieldName)) {
                    if (result != null) {
                        throw new DbAccessException("Two total_rows were provided.");
                    }

                    jp.nextToken();
                    result = new ArrayList<String>(jp.getIntValue());
                } else if ("rows".equals(fieldName)) {
                    if (result == null) {
                        throw new DbAccessException("total_rows is required to be included in value before rows");
                    }

                    if (jp.nextToken() != JsonToken.START_ARRAY) {
                        throw new DbAccessException("rows's value must be an array");
                    }

                    result = parseRows(jp, result);
                } else {
                    jp.skipChildren();
                }
            }
        }

        return result;
    }
}
