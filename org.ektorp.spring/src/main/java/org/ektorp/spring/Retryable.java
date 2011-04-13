package org.ektorp.spring;

import java.lang.annotation.*;
/**
 * Used to annotate methods that can be retries in case of update conflict.
 * 
 * Best realized in an aspect. No implementation supplied with Ektorp as this would introduce
 * unnecessary  dependencies.
 *  
 * @author henrik lundgren
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Retryable {

	
}
