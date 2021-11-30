package com.hlj.vialidate;

import com.hlj.data.general.AppException;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @Description 校验工具
 */
public class ValidateUtils {

    public static Validator validator;

    static {

        validator = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
                .buildValidatorFactory()
                .getValidator();
    }

    /**
     * 静态方法校验使用的
     *
     * @param object
     * @return
     */
    public static String validate(Object object) {
        if(object == null){
            throw new AppException("参数不完整");
        }
        Set<ConstraintViolation<Object>> validate = validator.validate(object);
        return resultValidate(validate);

    }

    /**
     * 静态方法校验使用，并且带分组的
     *
     * @param object
     * @param group
     * @return
     */
    public static String validate(Object object, Class group) {
        if (group == null) {
            return validate(object);
        } else {
            Set<ConstraintViolation<Object>> validate = validator.validate(object);
            if(validate != null && !validate.isEmpty()){
                return resultValidate(validate);
            }

            Set<ConstraintViolation<Object>> validateGroup = validator.validate(object, group);
            return resultValidate(validateGroup);
        }
    }


    private static String resultValidate(Set<ConstraintViolation<Object>> validate) {
        if (!validate.isEmpty()) {
            final StringBuffer stringBuffer = new StringBuffer();
            validate.stream().forEach(
                    item -> stringBuffer.append(item.getMessage()).append(","));
            stringBuffer.setLength(stringBuffer.length() - 1);
            return stringBuffer.toString();
        }
        return "success";
    }

}
