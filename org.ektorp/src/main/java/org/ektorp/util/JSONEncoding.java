package org.ektorp.util;

import java.util.regex.*;
/**
 * 
 * @author henrik
 * @deprecated don't use as this encoder is too simplistic.
 */
@Deprecated
public class JSONEncoding {

	private final static String QUOTE = "\"";
	private final static Pattern isJSONPattern = Pattern
			.compile("^[\\[{\"].+[\\]}\"]$");

	public static String jsonEncode(String s) {
		if (!isJSONPattern.matcher(s).matches()) {
			return QUOTE + s + QUOTE;
		}
		return s;
	}

}
