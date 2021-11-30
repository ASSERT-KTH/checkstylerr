package com.healerjean.proj.util.sensitivity;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;
import java.util.Map;


/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName MyBeanSerializerModifier
 * @date 2019/6/13  20:01.
 * @Description 提供给JsonUtils工具类进行脱敏
 */
public class SensitiveSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        // 循环所有的beanPropertyWriter
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanPropertyWriter writer = beanProperties.get(i);
            //判断是否有注解
            SensitiveInfo sensitiveInfo = writer.getAnnotation(SensitiveInfo.class);
            if (sensitiveInfo != null) {
                return beanProperties;
            }
            final String propName = writer.getName();
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
                        key = rule.substring(temp + 1, length);
                        substring = propName.substring(propLen - key.length(), propLen);
                    } else {
                        key = rule.substring(0, temp);
                        substring = propName.substring(0, temp);
                    }
                    if (substring.equals(key)) {
                        writer.assignSerializer(new SensitiveInfoSerialize(entry.getValue()));
                    }
                } else if (rule.equals(propName)) {
                    writer.assignSerializer(new SensitiveInfoSerialize(entry.getValue()));
                }
            }
        }
        return beanProperties;
    }
}
