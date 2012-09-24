package org.ektorp.impl.docref;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.util.*;
import org.junit.Assert;
import org.mockito.*;

/**
 *
 * @author ragnar rova
 *
 */

class InputStreamAsJsonMatcher extends ArgumentMatcher<InputStream> {

	final String expected;
	final ObjectMapper objectMapper;

	public InputStreamAsJsonMatcher(String expected) {
		this.expected = expected;
		this.objectMapper = new ObjectMapper();
	}

	public String readString(InputStream is) throws IOException {
		Writer writer = new StringWriter();

		char[] buf = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			int n;
			while ((n = reader.read(buf)) != -1) {
				writer.write(buf, 0, n);
			}
		} finally {
			is.close();
		}
		return writer.toString();
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof InputStream) {

			BufferedInputStream bis = new BufferedInputStream(
					(InputStream) argument);

			String actual = "";
			try {
				actual = readString(bis);
				boolean equal = JSONComparator.areEqual(expected, actual);
				if (!equal) {
					Assert.assertEquals(expected, actual);
				}
				return equal;
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage() + " s: " + actual, e);
			}

		}
		return false;
	}

}
