package com.hlj.proj.utils.validate;

import com.hlj.proj.constants.CommonConstants;
import com.hlj.proj.enums.ResponseEnum;
import com.hlj.proj.exception.BusinessException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ValidateUtils
 * @Date 2019/8/9  15:37.
 * @Description
 */
public class ValidateUtils {

    public static Validator validator;

    static {

        validator = Validation
                //一般情况下使用这里就可以了
                // .byProvider(HibernateValidator.class)

                //当 一个字段 配置非空group ，其他无需加组
                .byProvider(CustomValidationProvider.class)
                .configure()
                .failFast(true)
                .buildValidatorFactory()
                .getValidator();
        //快速返回模式，有一个验证失败立即返回错误信息

    }


    /**
     * 静态方法校验使用的
     *
     * @param object
     * @return
     */
    public static String validate(Object object) {
        if (object == null) {
            throw new BusinessException(ResponseEnum.参数错误);
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
        if (object == null) {
            return "校验对象为空";
        }
        if (group == null) {
            return validate(object);
        } else {
            Set<ConstraintViolation<Object>> validate = validator.validate(object);
            if (validate != null && !validate.isEmpty()) {
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
        return CommonConstants.COMMON_SUCCESS;
    }
}
