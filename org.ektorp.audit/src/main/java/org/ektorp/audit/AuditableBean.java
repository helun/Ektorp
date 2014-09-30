package org.ektorp.audit;

import org.joda.time.DateTime;


public interface AuditableBean {

    /**
     * Returns the unique identifier of the user who caused the creation of this
     * record. This column is not a foreign key to the User table.
     *
     * @return The unique identifier of the user who created this record.
     */
	public Object getCreatedBy();

    /**
     * Sets the unique identifier of the user who caused the creation of this
     * record.
     *
     * @param createdBy The unique identifier of the user who created this
     *                  record.
     */
	public void setCreatedBy(final Object createdBy);

    /**
     * Returns the date and time of when this record was created.
     *
     * @return The date and time this record was created.
     */
    public DateTime getCreatedDate();

    /**
     * Sets the date and time of when this record was created.
     *
     * @param createdOn The date and time this record was created.
     */
    public void setCreatedDate(final DateTime creationDate);

    /**
     * Returns the unique identifier of the user who most recently caused the
     * modification of this record. This column is not a foreign key to the User
     * table.
     *
     * @return The unique identifier of the user who last modified this record.
     */
    public Object getLastModifiedBy();

    /**
     * Sets the unique identifier of the user who most recently caused the
     * modification of this record.
     *
     * @param lastModifiedBy The unique identifier of the user who last modified
     *                       this record.
     */
    public void setLastModifiedBy(final Object lastModifiedBy);

    /**
     * Returns the date and time of when this record was last modified.
     *
     * @return The date and time this record was last modified.
     */
    public DateTime getLastModifiedDate();

    /**
     * Sets the date and time of when this record was last modified.
     *
     * @param lastModifiedOn The date and time this record was last modified.
     */
    public void setLastModifiedDate(final DateTime lastModifiedOn);
    
}
