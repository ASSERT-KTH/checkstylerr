package com.hlj.proj.utils.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 * @ClassName SensitiveInfoSerialize
 * @Author TD
 * @Date 2019/1/10 17:28
 * @Description Json脱敏序列化
 */
public class SensitiveInfoSerialize extends JsonSerializer<Object> implements ContextualSerializer {

    private SensitiveTypeEnum type;

    public SensitiveInfoSerialize() {
    }

    public SensitiveInfoSerialize(final SensitiveTypeEnum type) {
        this.type = type;
    }


    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        switch (this.type) {
            case ID_CARD: {
                jsonGenerator.writeString(SensitiveInfoUtils.idCard(String.valueOf(value)));
                break;
            }
            case MOBILE_PHONE: {
                jsonGenerator.writeString(SensitiveInfoUtils.mobilePhone(String.valueOf(value)));
                break;
            }
            case EMAIL: {
                jsonGenerator.writeString(SensitiveInfoUtils.email(String.valueOf(value)));
                break;
            }
            case ACCOUNT_NO: {
                jsonGenerator.writeString(SensitiveInfoUtils.acctNo(String.valueOf(value)));
                break;
            }
            case PASSWORD: {
                jsonGenerator.writeString(SensitiveInfoUtils.password(String.valueOf(value)));
                break;
            }
            case REAL_NAME: {
                jsonGenerator.writeString(SensitiveInfoUtils.realName(String.valueOf(value)));
                break;
            }
            default:
                jsonGenerator.writeString(String.valueOf(value));

        }

    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {

        if (beanProperty != null) {

            // 非 String 类直接跳过
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                SensitiveInfo sensitiveInfo = beanProperty.getAnnotation(SensitiveInfo.class);
                if (sensitiveInfo == null) {
                    sensitiveInfo = beanProperty.getContextAnnotation(SensitiveInfo.class);
                }
                // 如果能得到注解，就将注解的 value 传入 SensitiveInfoSerialize
                if (sensitiveInfo != null) {

                    return new SensitiveInfoSerialize(sensitiveInfo.value());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);

    }
}
