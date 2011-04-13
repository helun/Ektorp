package org.ektorp.support;

import java.lang.annotation.*;
/**
 * Annotation for defining list functions embedded in repositories.
 * @author henrik lundgren
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListFunction {
	/**
	 * The name of the list function
	 * @return
	 */
	String name();
	/**
	 * Inline list function.
	 * @return
	 */
	String function() default "";
	/**
	 * List functions are best stored in a separate files.
	 * 
	 * By specifying the file parameter a function can be loaded from the classpath.
	 * The path is relative to the class annotated by this annotation.
	 * 
	 * If the file my_list_func.json is in the same directory as the repository this
	 * parameter should be set to "my_list_func.js".
	 * 
	 * @return
	 */
	String file() default "";
}
