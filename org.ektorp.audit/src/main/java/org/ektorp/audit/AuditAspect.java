package org.ektorp.audit;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
public class AuditAspect {

    // SLF4J logger
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    /** 
     * The handler to be used during the auditing
     */
    private AuditingHandler handler;
    
    /**
     * Sets the auditing handler
     */
    public void setAuditingHandler( AuditingHandler handler){
        this.handler = handler;
    }

    // Pointcut to the create method
    @Pointcut("(execution(* org.ektorp.CouchDbConnector+.create(.., Object)) && args(o) ) || ( execution(* org.ektorp.CouchDbConnector+.create(Object)) && args(o))")
    public void create(AuditableBean o) {}
    
    // Pointcut to the update method
    @Pointcut("execution(* org.ektorp.CouchDbConnector+.update(Object)) && args(o)")
    public void update(AuditableBean o){}
    
    /**
     * Sets modification and creation date and auditor on the target object on update events.
     * 
     * @param target
     */
    @Before("create(target)")
    public void beforeCreate(AuditableBean target){
     // get handler
        if (handler != null) {
            handler.markCreated(target);
        }
    }
    
    /**
     * Sets creation date and auditor on the target object on create events.
     * 
     * @param target
     */
    @Before("update(target)")
    public void beforeUpdate(AuditableBean target){
        // get handler
        if (handler != null) {
            handler.markModified(target);
        }
    }
    

}
