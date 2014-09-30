package org.ektorp.audit;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementation of the AuditableBean interface
 */
public class AuditableBeanImpl implements AuditableBean{
   	
    	@NotNull
    	@JsonProperty("createdDate")
    	private DateTime createdDate;

    	@NotNull
    	@JsonProperty("lastModifiedDate")
    	private DateTime lastModifiedDate;
    	
    	@JsonProperty("createdBy")
    	private Object createdBy;
    	
    	@JsonProperty("lastModifiedBy")
    	private Object lastModifiedBy;
    	
    	@Override
    	public DateTime getCreatedDate(){
    		return this.createdDate;
    	}
    	
        @Override
    	public DateTime getLastModifiedDate(){
    		return this.lastModifiedDate;
    	}
    	
        @Override
    	public void setCreatedDate(final DateTime createdDate){
    		this.createdDate = createdDate;
    	}
    	
        @Override
    	public void setLastModifiedDate(final DateTime lastModifiedDate){
    		this.lastModifiedDate = lastModifiedDate;
    	}
    	
        @Override
    	public Object getLastModifiedBy(){
    		return this.lastModifiedBy;
    	}
    	
        @Override
    	public Object getCreatedBy(){
    		return this.createdBy;
    	}
    	
        @Override
    	public void setCreatedBy(final Object createdBy ){
    		this.createdBy = createdBy;
    	}

        @Override
    	public void setLastModifiedBy(final Object modifiedBy){
    		this.lastModifiedBy = modifiedBy;
    	}
}
