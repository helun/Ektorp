package org.ektorp.audit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auditing handler to mark entity objects created and modified.  Heavily influenced by Spring Data.
 * 
 * @author Eric Benzacar
 */
public class AuditingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditingHandler.class);

    private AuditorAware<?> auditorAware;
    private boolean modifyOnCreation = true;

    /**
     * Setter to inject a {@code AuditorAware} component to retrieve the current auditor.
     * 
     * @param auditorAware the auditorAware to set
     */
    public void setAuditorAware(final AuditorAware<?> auditorAware) {

        if (auditorAware == null) 
            throw new IllegalArgumentException("argument required.  It must not be null" );
        this.auditorAware = auditorAware;
    }


    /**
     * Set this to false if you want to treat entity creation as modification and thus set the current date as
     * modification date, too. Defaults to {@code true}.
     * 
     * @param modifyOnCreation if modification information shall be set on creation, too
     */
    public void setModifyOnCreation(boolean modifyOnCreation) {
        this.modifyOnCreation = modifyOnCreation;
    }


    /**
     * Marks the given object as created.
     * 
     * @param source
     */
    public void markCreated(Object source) {
        touch(source, true);
    }

    /**
     * Marks the given object as modified.
     * 
     * @param source
     */
    public void markModified(Object source) {
        touch(source, false);
    }

    private void touch(Object target, boolean isNew) {

        if( !( target instanceof AuditableBean ))
            return;
        
        AuditableBean bean = (AuditableBean)target;

        Object auditor = touchAuditor(bean, isNew);
        DateTime now = touchDate(bean, isNew);

        Object defaultedNow = now == null ? "not set" : now;
        Object defaultedAuditor = auditor == null ? "unknown" : auditor;

        LOGGER.debug("Touched {} - Last modification at {} by {}", new Object[] { target, defaultedNow, defaultedAuditor });
    }

    /**
     * Sets modifying and creating auditioner. Creating auditioner is only set on new auditables.
     * 
     * @param auditable
     * @return
     */
    private Object touchAuditor(AuditableBean bean, boolean isNew) {

        if (null == auditorAware) {
            return null;
        }

        Object auditor = auditorAware.getCurrentAuditor();

        if (isNew) {
            bean.setCreatedBy(auditor);
            if (!modifyOnCreation) {
                return auditor;
            }
        }

        bean.setLastModifiedBy(auditor);
        return auditor;
    }

    /**
     * Touches the auditable regarding modification and creation date. Creation date is only set on new auditables.
     * 
     * @param wrapper
     * @return
     */
    private DateTime touchDate(AuditableBean bean, boolean isNew) {

        DateTime now = new DateTime();

        if (isNew) {
            bean.setCreatedDate(now);
            if (!modifyOnCreation) {
                return now;
            }
        }

        bean.setLastModifiedDate(now);
        return now;
    }

}
