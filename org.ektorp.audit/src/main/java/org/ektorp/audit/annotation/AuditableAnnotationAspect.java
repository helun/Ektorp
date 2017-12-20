package org.ektorp.audit.annotation;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;
import org.ektorp.audit.AuditableBean;
import org.ektorp.audit.AuditableBeanImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
public class AuditableAnnotationAspect {

    // SLF4J logger
    private static final Logger logger = LoggerFactory.getLogger(AuditableAnnotationAspect.class);
    
    /*
     * Assign  the AuditableBean interface to any bean with the Auditable annotation
     */
    @DeclareMixin("@org.ektorp.audit.annotation.Auditable *")
    public AuditableBean auditableBeanMixin(){
        return new AuditableBeanImpl();
    }
}
