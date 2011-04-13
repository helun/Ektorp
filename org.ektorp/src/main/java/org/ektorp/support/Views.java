package org.ektorp.support;

import java.lang.annotation.*;

/**
 * Annotation for defining multiple views embedded in repositories.
 * @author henrik lundgren
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Views {

	View[] value();
	
}
