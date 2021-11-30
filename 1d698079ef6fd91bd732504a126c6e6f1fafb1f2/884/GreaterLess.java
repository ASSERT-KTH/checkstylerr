package com.hlj.vialidate.validator.anno;

import com.hlj.vialidate.validator.GreaterLessValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;


/**
 * 自定义注解 校验大于 小于 的校验
 *   min <  number <  max
 */
@Constraint(validatedBy = {GreaterLessValidator.class})
@Documented
@Target( {  ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GreaterLess {

    String message() ; //报错返回的信息

    Class<?>[] groups() default { }; //被哪个组校验

    String min() ; //给定的数肯定大于它

    String max() ; //给定的数肯定小于它

    Class<? extends Payload>[] payload() default { };
}
