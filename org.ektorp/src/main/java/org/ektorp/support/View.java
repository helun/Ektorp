package org.ektorp.support;

import java.lang.annotation.*;
/**
 * Annotation for defining views embedded in repositories.
 * @author henrik lundgren
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
	/**
	 * The name of the view
	 * @return
	 */
	String name();
	/**
	 * Map function or path to function.
     * <p>
     * This value may be a string of code to use for the function.
     * Alternatively, the string may specify a file to load for
     * the function by starting the string with <i>classpath:</i>.
     * The rest of the string then represents a relative path to
     * the function.
	 * @return
	 */
	String map() default "";
	/**
	 * Reduce function or path to function.
     * <p>
     * This value may be a string of code to use for the function.
     * Alternatively, the string may specify a file to load for
     * the function by starting the string with <i>classpath:</i>.
     * The rest of the string then represents a relative path to
     * the function.
	 * @return
	 */
	String reduce() default "";
	/**
	 * Non-trivial views are best stored in a separate files.
	 * 
	 * By specifying the file parameter a view definition can be loaded from the classpath.
	 * The path is relative to the class annotated by this annotation.
	 * 
	 * If the file complicated_view.json is in the same directory as the repository this
	 * parameter should be set to "complicated_view.json".
	 * 
	 * The file must be a valid json document:
	 * 
	 * {
	 *     "map": "function(doc) { much javascript here }",
	 *     // the reduce function is optional
	 *     "reduce": "function(keys, values) { ... }"
	 * }
	 * 
	 * @return
	 */
	String file() default "";
	
}
