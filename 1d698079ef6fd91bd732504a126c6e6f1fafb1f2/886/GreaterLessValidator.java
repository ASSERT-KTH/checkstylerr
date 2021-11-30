package com.hlj.vialidate.validator;


import com.hlj.vialidate.validator.anno.GreaterLess;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * 校验数据
 * min <  number <  max
 */
public class GreaterLessValidator implements ConstraintValidator<GreaterLess, Object> {

    private String min;

    private String max;


    /**
     * 获取注解中的值
     *
     * @param constraintAnnotation
     */
    @Override
    public void initialize(GreaterLess constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    /**
     * @param value   字段数据
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (StringUtils.isBlank(min) && StringUtils.isBlank(max)) {
            throw new RuntimeException("至少需要选择一个less 或者 greater");
        }
        //强制将各种对象转化为字符串
        BigDecimal valueDec = new BigDecimal(String.valueOf(value));
        if (StringUtils.isNotBlank(min)) {
            BigDecimal lessDec = new BigDecimal(min);
            if (valueDec.compareTo(lessDec) < 1) {
                return false;
            }
        }
        if (StringUtils.isNotBlank(max)) {
            BigDecimal greaterDec = new BigDecimal(max);
            if (valueDec.compareTo(greaterDec) > -1) {
                return false;
            }
        }
        return true;
    }
}
