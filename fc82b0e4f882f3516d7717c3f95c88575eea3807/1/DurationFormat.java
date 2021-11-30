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
 * <b>Validation for Duration</b>.
 * <p>
 * The duration type is used to specify a time interval. The time interval is
 * specified in the following form "PnYnMnDTnHnMnS" where:
 * 
 * P indicates the period (required) nY indicates the number of years nM
 * indicates the number of months nD indicates the number of days T indicates
 * the start of a time section (required if you are going to specify hours,
 * minutes, or seconds) nH indicates the number of hours nM indicates the number
 * of minutes nS indicates the number of seconds
 * </p>
 *
 *
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = DurationFormatValidator.class)
@Documented
@PublicEvolving
public @interface DurationFormat {

    String message() default "Invalid Duration Format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
