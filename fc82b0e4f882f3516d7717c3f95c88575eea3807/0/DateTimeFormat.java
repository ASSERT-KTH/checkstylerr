package com.piksel.sequoia.clientsdk.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.piksel.sequoia.annotations.PublicEvolving;

/**
 * A timestamp represents a date and time with accuracy to the nearest
 * millisecond.
 * 
 * <p>In data interchange representations (i.e. query parameters, headers and body)
 * a timestamp is represented as a string and must be formatted as a ISO 8601
 * UTC timestamp to millisecond precision e.g. 2014-05-15T15:16:05.592Z.
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = DateTimeFormatValidator.class)
@Documented
@PublicEvolving
public @interface DateTimeFormat {

    String message() default "Invalid Date Time Format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String regex() default "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

}
