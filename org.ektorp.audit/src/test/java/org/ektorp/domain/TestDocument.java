package org.ektorp.domain;

import org.ektorp.audit.annotation.Auditable;
import org.ektorp.support.CouchDbDocument;

@Auditable
public class TestDocument extends CouchDbDocument {

    public TestDocument() {
        // TODO Auto-generated constructor stub
    }
    
    private String value;

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    

}
