package org.ektorp.util;
/**
 * 
 * @author henrik lundgren
 *
 */
public class Joiner {

	public static String join(Iterable<String> src, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String s : src) {
			sb.append(s);
			sb.append(separator);
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - separator.length()) : "";
	}

}
