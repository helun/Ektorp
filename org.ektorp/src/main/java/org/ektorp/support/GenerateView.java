package org.ektorp.support;

import java.lang.annotation.*;
/**
 * Indicates that a CouchDb view should be generated  for the annotated method.
 * @author henrik lundgren
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateView {

	 /**
     * Defines the name of the property to use.
     */
    String field() default "";
	
}
