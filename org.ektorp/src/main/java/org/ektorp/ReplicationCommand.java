package org.ektorp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ektorp.util.Assert;

import java.io.Serializable;
import java.util.Collection;

@JsonInclude(Include.NON_NULL)
public class ReplicationCommand implements Serializable {

	private static final long serialVersionUID = 6919908757724780784L;

	@JsonProperty
	public final String source;

	@JsonProperty
	public final String target;

	@JsonProperty
	public final String proxy;

	@JsonProperty
	public final String filter;

	@JsonProperty("doc_ids")
	public final Collection<String> docIds;

	@JsonProperty
	public final Boolean continuous;

	@JsonProperty
	public final Boolean cancel;

	@JsonProperty("query_params")
	public final Object queryParams;

	@JsonProperty("create_target")
	public final Boolean createTarget;

    @JsonProperty("since_seq")
    public final Object sinceSeq;

    private ReplicationCommand(Builder b) {
		source = b.source;
		target = b.target;
		proxy = b.proxy;
		filter = b.filter;
		docIds = b.docIds;
		continuous = b.continuous ? Boolean.TRUE : null;
		cancel = b.cancel ? Boolean.TRUE : null;
		createTarget = b.createTarget ? Boolean.TRUE : null;

        if(b.sinceSeqLong!=null) {
            sinceSeq = b.sinceSeqLong;
        } else {
            sinceSeq = b.sinceSeqString;
        }
		queryParams = b.queryParams;
	}

	public static class Builder {

		private String source;
		private String target;
		private String proxy;
		private String filter;
		private Collection<String> docIds;
		private boolean continuous;
		private boolean cancel;
		private boolean createTarget;
        private String sinceSeqString;
        private Long sinceSeqLong;
		private Object queryParams;
		/**
		 * Source and target can both point at local databases, remote databases and any combination of these.
		 *
		 * If your local CouchDB instance is secured by an admin account, you need to use the full URL format
		 * @param s
		 * @return
		 */
		public Builder source(String s) {
			source = s;
			return this;
		}
		/**
		 * Source and target can both point at local databases, remote databases and any combination of these
		 *
		 * If your local CouchDB instance is secured by an admin account, you need to use the full URL format.
		 * @param s
		 * @return
		 */
		public Builder target(String s) {
			target = s;
			return this;
		}
		/**
		 * Pass a "proxy" argument in the replication data to have replication go through an HTTP proxy
		 * @param s
		 * @return
		 */
		public Builder proxy(String s) {
			proxy = s;
			return this;
		}
		/**
		 * Specify a filter function.
		 * @param s
		 * @return
		 */
		public Builder filter(String s) {
			filter = s;
			return this;
		}
		/**
		 * Restricts replication to the specified document ids.
		 * @param docIds
		 * @return
		 */
		public Builder docIds(Collection<String> docIds) {
			this.docIds = docIds;
			return this;
		}
		/**
		 * true makes replication continuous
		 * @param b
		 * @return
		 */
		public Builder continuous(boolean b) {
			continuous = b;
			return this;
		}
		/**
		 * true cancels a continuous replication task
		 * @param b
		 * @return
		 */
		public Builder cancel(boolean b) {
			cancel = b;
			return this;
		}
		/**
		 * Pass parameters to the filter function if specified.
		 * @param o
		 * @return
		 */
		public Builder queryParams(Object o) {
			queryParams = o;
			return this;
		}
		/**
		 * To create the target database (remote or local) prior to replication.
		 * The names of the source and target databases do not have to be the same.
		 * @param b
		 * @return
		 */
		public Builder createTarget(boolean b) {
			createTarget = b;
			return this;
		}
        /**
         * The sequence from which the replication should start
         * See http://docs.couchdb.org/en/latest/json-structure.html#replication-settings for details
         *
         * CouchDB expects a Long value for the sequence
         * Cloudant expects a String value for the sequence
         *
         * @param sinceSeq as String
         * @return
         */
        public Builder sinceSeq(String sinceSeq) {
            try {
                this.sinceSeqLong =  Long.parseLong(sinceSeq);
            } catch (NumberFormatException e) {
                this.sinceSeqString = sinceSeq;
            }
            return this;
        }

		public ReplicationCommand build() {
			Assert.hasText(source, "source may not be null or empty");
			Assert.hasText(target, "target may not be null or empty");
			return new ReplicationCommand(this);
		}
	}
}
