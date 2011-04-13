package org.ektorp.support;

import java.lang.annotation.*;

/**
 * Annotation for defining filter functions embedded in repositories.
 * @author henrik lundgren
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
	/**
	 * The name of the filter
	 * @return
	 */
	String name();
	/**
	 * Inline filter function.
	 * @return
	 */
	String function() default "";
	/**
	 * Filter functions are best stored in a separate files.
	 * 
	 * By specifying the file parameter a function can be loaded from the classpath.
	 * The path is relative to the class annotated by this annotation.
	 * 
	 * If the file my_filter.json is in the same directory as the repository this
	 * parameter should be set to "my_filter.js".
	 * 
	 * @return
	 */
	String file() default "";
}
