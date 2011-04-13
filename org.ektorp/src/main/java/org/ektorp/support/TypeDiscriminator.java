package org.ektorp.support;

import java.lang.annotation.*;
/**
 * Used to distinguish a type's documents in the database.
 * 
 * Declare on fields or getter methods in order for them to be used in generated views filter conditions.
 * 
 * Declare on type in order specify a custom filter condition.
 * 
 * A TypeDiscriminator declared on type level cannot be mixed with TypeDiscriminators declared onb fields.
 * @author henrik lundgren
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeDiscriminator {
	/**
	 * If TypeDiscriminator is declared on type level, a filter condition must be specified.
	 * This condition is inserted along other conditions in the generated views map function:
	 * function(doc) { if(CONDITION INSERTED HERE && doc.otherField) {emit(null, doc._id)} }
	 * 
	 * Not valid to use if declared on field or method level.
	 */
	String value() default "";
	
}
