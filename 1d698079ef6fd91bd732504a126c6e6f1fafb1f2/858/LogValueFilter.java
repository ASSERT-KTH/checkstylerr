package com.hlj.proj.utils.log4j2;

import com.alibaba.fastjson.serializer.ValueFilter;
import com.hlj.proj.utils.sensitivity.SensitiveInfo;
import com.hlj.proj.utils.sensitivity.SensitiveInfoUtils;
import com.hlj.proj.utils.sensitivity.SensitiveTypeEnum;
import com.hlj.proj.utils.sensitivity.SensitivityConstants;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName LogValueFilter
 * @date 2019/6/13  20:01.
 * @Description 脱敏拦截器，FastJson
 */
public class LogValueFilter implements ValueFilter {

    @Override
    public Object process(Object object, String propName, Object propertyValue) {
        if (!(propertyValue instanceof String)) {
            return propertyValue;
        }
        //判断是否有注解
        Field declaredField = null;
        try {
            declaredField = object.getClass().getDeclaredField(propName);
        } catch (NoSuchFieldException e) {
            return propertyValue;
        }
        declaredField.setAccessible(true);
        SensitiveInfo sensitiveInfo = declaredField.getAnnotation(SensitiveInfo.class);
        if (sensitiveInfo != null) {
            return SensitiveInfoUtils.sensitveValue(sensitiveInfo.value(), String.valueOf(propertyValue));
        }
        //正则匹配
        for (Map.Entry<String, SensitiveTypeEnum> entry : SensitivityConstants.sensitivityRules.entrySet()) {
            String rule = entry.getKey();
            int length = rule.length();
            int propLen = propName.length();
            if (propName.length() < length) {
                continue;
            }
            int temp = rule.indexOf("*");
            String key = null;
            String substring = null;
            if (temp >= 0) {
                if (temp < (length >> 2)) {
                    //*在开头
                    key = rule.substring(temp + 1, length);
                    substring = propName.substring(propLen - key.length(), propLen);
                } else {
                    //*在结尾
                    key = rule.substring(0, temp);
                    substring = propName.substring(0, temp);
                }
                if (substring.equals(key)) {
                    return SensitiveInfoUtils.sensitveValue(entry.getValue(), String.valueOf(propertyValue));
                }
            } else if (rule.equals(propName)) {
                return SensitiveInfoUtils.sensitveValue(entry.getValue(), String.valueOf(propertyValue));
            }
        }
        return propertyValue;
    }
}
