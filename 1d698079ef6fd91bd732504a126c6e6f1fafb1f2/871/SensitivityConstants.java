package com.hlj.proj.utils.sensitivity;


import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SensitivityConstants
 * @Author TD
 * @Date 2019/1/10 10:58
 * @Description 脱敏规则
 */
public class SensitivityConstants {

    public static Map<String,SensitiveTypeEnum> sensitivityRules = new HashMap<>();

    static {
        /** 真实姓名 */
        sensitivityRules.put("*RealName",SensitiveTypeEnum.REAL_NAME);
        sensitivityRules.put("realName",SensitiveTypeEnum.REAL_NAME);
        /** 身份证 */
        sensitivityRules.put("*IdCard",SensitiveTypeEnum.ID_CARD);
        sensitivityRules.put("idCard",SensitiveTypeEnum.ID_CARD);
        /** 手机号 */
        sensitivityRules.put("*Phone",SensitiveTypeEnum.MOBILE_PHONE);
        sensitivityRules.put("*phone",SensitiveTypeEnum.MOBILE_PHONE);
        sensitivityRules.put("phone",SensitiveTypeEnum.MOBILE_PHONE);
        sensitivityRules.put("*Mobile",SensitiveTypeEnum.MOBILE_PHONE);
        sensitivityRules.put("mobile",SensitiveTypeEnum.MOBILE_PHONE);
        /** 邮箱 */
        sensitivityRules.put("*Email",SensitiveTypeEnum.EMAIL);
        sensitivityRules.put("email",SensitiveTypeEnum.EMAIL);
        /** 密码 */
        sensitivityRules.put("passWord",SensitiveTypeEnum.PASSWORD);
        sensitivityRules.put("password",SensitiveTypeEnum.PASSWORD);
        sensitivityRules.put("*Password",SensitiveTypeEnum.PASSWORD);
        sensitivityRules.put("*PassWord",SensitiveTypeEnum.PASSWORD);
    }

}
