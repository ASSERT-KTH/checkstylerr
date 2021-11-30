package com.piksel.sequoia.clientsdk.validation;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
