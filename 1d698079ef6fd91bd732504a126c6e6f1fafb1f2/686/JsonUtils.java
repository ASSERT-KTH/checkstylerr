package com.hlj.proj.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.hlj.proj.utils.sensitivity.SensitiveInfoUtils;
import com.hlj.proj.utils.sensitivity.SensitiveSerializerModifier;
import com.hlj.proj.utils.sensitivity.SensitiveTypeEnum;
import com.hlj.proj.utils.sensitivity.SensitivityConstants;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * Utils - JSON
 */
public final class JsonUtils {

    /**
     * ObjectMapper
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper  objectMapperSensitivity = new ObjectMapper();


    static {


        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //NULL 不打印
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 排除json字符串中实体类没有的字段
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        //LocalDateTime   LocalDate LocalTime 转化成 String
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        OBJECT_MAPPER.registerModule(javaTimeModule);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);




        //脱敏日志创建
        objectMapperSensitivity.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        objectMapperSensitivity.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapperSensitivity.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapperSensitivity.registerModule(javaTimeModule);
        objectMapperSensitivity.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //脱敏
        objectMapperSensitivity.setSerializerFactory(objectMapperSensitivity.getSerializerFactory().withSerializerModifier(new SensitiveSerializerModifier()));

    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }


    /**
     * 将对象转换为JSON字符串
     *
     * @param value 对象
     * @return JSON字符串
     */
    public static String toJsonString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json      JSON字符串
     * @param c 类型
     * @return 对象
     */
    public static <T> T toObject(String json, Class<T> c) {
        try {
            return OBJECT_MAPPER.readValue(json, c);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     * @param json          JSON字符串
     * @param typeReference 类型 可以通过这个转化为List集合 ，举例：
     *                      List<JavaBean> list =  JsonUtils.toObject(jsonArrayStr, new TypeReference<List<JavaBean>>() { });
     *                      Map<String, Object> map =  JsonUtils.toObject(JsonUtils.toJson(javaBean),new TypeReference<Map<String, Object>>( ){} );
     * @return 对象
     */
    public static <T> T toObject(String json, TypeReference<?> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json     JSON字符串
     * @param javaType 类型
     * @return 对象
     */
    public static <T> T toObject(String json, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * 将JSON字符串转换为树
     * @param json JSON字符串
     * @return 树
     */
    public static JsonNode toTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON流
     *
     * @param writer Writer
     * @param value  对象
     */
    public static void writeValue(Writer writer, Object value) {
        try {
            OBJECT_MAPPER.writeValue(writer, value);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * 构造类型
     * @param type 类型,类，接口，枚举字段类型 （java.class）
     * @return 类型
     */
    public static JavaType constructType(Type type) {
        return TypeFactory.defaultInstance().constructType(type);
    }


    /**
     * 构造类型
     * @param typeReference 类型 new TypeReference<List<JavaBean>>() { }
     * @return 类型
     */
    public static JavaType constructType(TypeReference<?> typeReference) {
        return TypeFactory.defaultInstance().constructType(typeReference);
    }


    /**
     * @param resString
     * @return String
     * @Description Json 格式化到控制台打印
     */
    public static String responseFormat(String resString) {

        StringBuffer jsonForMatStr = new StringBuffer();
        int level = 0;
        ////将字符串中的字符逐个按行输出
        for (int index = 0; index < resString.length(); index++) {
            //获取s中的每个字符
            char c = resString.charAt(index);

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    /**
     * @param level
     * @return
     * @throws
     */
    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }



    /**
     * 对象转Json格式字符串----脱敏处理(包含map)
     * @return
     */
    public static String toJsonStringWithSensitivity(Object propName){
        if(propName != null && propName instanceof Map){
            Map map = (Map) propName;
            if(map != null && !map.isEmpty()){
                Set<Map.Entry> set = map.entrySet();
                for (Map.Entry item: set) {
                    Object key = item.getKey();
                    Object value = item.getValue();
                    if(key instanceof String){
                        String keyString = key.toString();
                        String s = dealSensitivity(keyString, value.toString());
                        map.put(keyString, s);
                    }
                }
            }
        }
        try {
            return objectMapperSensitivity.writeValueAsString(propName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String dealSensitivity(String mapkey, String mapValue){
        //正则匹配
        for(Map.Entry<String, SensitiveTypeEnum> entry : SensitivityConstants.sensitivityRules.entrySet()){
            String rule = entry.getKey();
            int length = rule.length();
            int propLen = mapkey.length();
            if(mapkey.length() < length){
                continue;
            }
            int temp = rule.indexOf("*");
            String key = null;
            String substring = null;
            if(temp >= 0 ){
                if(temp < (length >> 2)){
                    key = rule.substring(temp+1,length);
                    substring = mapkey.substring(propLen-key.length(), propLen);
                }else{
                    key = rule.substring(0,temp);
                    substring = mapkey.substring(0,temp);
                }
                if(substring.equals(key)) {
                    return SensitiveInfoUtils.sensitveValue(entry.getValue(), mapValue);
                }
            }else if (rule.equals(mapkey)){
                return SensitiveInfoUtils.sensitveValue(entry.getValue(), mapValue );
            }
        }
        return mapValue;
    }


}
