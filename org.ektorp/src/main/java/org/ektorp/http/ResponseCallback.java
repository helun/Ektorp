package org.ektorp.http;


/**
 * 
 * @author Henrik Lundgren
 * created 1 nov 2009
 *
 */
public interface ResponseCallback<T> {
	/**
	 * Called when http response code is < 300
	 * @param hr
	 */
	T success(HttpResponse hr) throws Exception;
	T error(HttpResponse hr);
}
