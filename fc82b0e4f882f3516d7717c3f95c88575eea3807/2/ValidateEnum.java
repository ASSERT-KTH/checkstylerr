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
 * <b>Validation for Enums</b>.
 * <p>
 * Validate that the value defined is included on the enum definition.
 * </p>
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidateEnumValidator.class)
@Documented
@PublicEvolving
public @interface ValidateEnum {

    Class<? extends Enum<?>> enumClazz();

    String message() default "Value is not included in the enum definition";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
