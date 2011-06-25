package org.ektorp.spring;

import java.lang.annotation.*;
/**
 * Used to annotate idempotent methods that can be retried in case of update conflict.
 *  
 * @author henrik lundgren
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Idempotent {

	
}
