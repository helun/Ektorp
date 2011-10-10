package org.ektorp.changes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ektorp.http.URI;

/**
 * 
 * @author henrik lundgren
 * @since 1.1
 */
public class ChangesCommand {

	public final String since;
	public final boolean continuous;
	public final String filter;
	public final boolean includeDocs;
	public final int heartbeat;
	public final int limit;
	public final Map<String,String> extraQueryParams;
	
	private String queryString; 
	
	private ChangesCommand(Builder b) {
		continuous = b.continuous;
		since = b.since;
		filter = b.filter;
		includeDocs = b.includeDocs;
		heartbeat = b.heartbeat;
		limit = b.limit;
		if (b.extraQueryParams != null) {
			extraQueryParams = Collections.unmodifiableMap(new LinkedHashMap<String, String>(b.extraQueryParams));	
		} else {
			extraQueryParams = null;
		}
	}
	
	@Override
	public String toString() {
		if (queryString == null) {
			URI uri = URI.of("_changes");
			if (continuous) {
				uri.param("feed", "continuous");
			}
			
			if (since != null) {
				uri.param("since", since);
			}
			
			if (filter != null) {
				uri.param("filter", filter);
			}
			
			if (includeDocs) {
				uri.param("include_docs", "true");
			}
			
			if (heartbeat > 0) {
				uri.param("heartbeat", heartbeat);
			}
			
			if (limit > -1){
				uri.param("limit", limit);
			}
			
			if (extraQueryParams != null) {
				uri.params(extraQueryParams);
			}
			
			queryString = uri.toString();
		}
		return queryString;
	}
	/**
	 * Ised to create a valid ChangesCommand
	 * 
	 * @author henrik lundgren
	 *
	 */
	public static class Builder {
		
		private String since;
		private boolean continuous;
		private String filter;
		private boolean includeDocs;
		private int heartbeat = -1;
		private int limit = -1;
		private Map<String,String> extraQueryParams;
		
		/**
		 * Start the results from the change immediately after the given sequence number.
		 * @param l
		 * @return
		 */
		public Builder since(long l) {
			this.since = Long.toString(l);
			return this;
		}
		
		/**
		 * Start the results from the change immediately after the given sequence number.
		 * @param l
		 * @return
		 */
		public Builder since(String s) {
			this.since = s;
			return this;
		}
		
		/**
		 * Adds a parameter to the GET request sent to the database.
		 * This is mainly used for supplying parameters to filter functions.
		 * @param name
		 * @param value
		 * @return
		 */
		public Builder param(String name, String value) {
			if (extraQueryParams == null) {
				extraQueryParams = new LinkedHashMap<String, String>();
			}
			extraQueryParams.put(name, value);
			return this;
		}
		
		public Builder continuous(boolean b) {
			this.continuous = b;
			return this;
		}
		/**
		 * Reference a filter function from a design document to selectively get updates.
		 * @param s
		 * @return
		 */
		public Builder filter(String s) {
			this.filter = s;
			return this;
		}
		/**
		 * Include the associated document with each result.
		 * @param b
		 * @return
		 */
		public Builder includeDocs(boolean b) {
			this.includeDocs = b;
			return this;
		}
		
		public Builder heartbeat(int i) {
			this.heartbeat = i;
			return this;
		}
		
		public Builder limit(int i) {
		    	this.limit = i;
		    	return this;
		}
		
		public Builder merge(ChangesCommand other) {
			continuous = other.continuous;
			filter = other.filter;
			includeDocs = other.includeDocs;
			since = other.since;
			limit = other.limit;
			if (other.extraQueryParams != null) {
			    extraQueryParams = new LinkedHashMap<String, String>(other.extraQueryParams);
			}
			return this;
		}
		
		public ChangesCommand build() {
			return new ChangesCommand(this);
		}
	}
}
