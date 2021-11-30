package com.healerjean.proj.util.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.healerjean.proj.util.xml.strategy.UpperCaseSnackNamingStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @ClassName JacksonUtils
 * @Author TD
 * @Date 2018/11/14 16:51
 * @Description
 */
@Slf4j
public class XmlUtils {

    public static final XmlMapper xmlMapper;


    static {
        xmlMapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        xmlMapper.registerModule(module);
        xmlMapper.setDefaultUseWrapper(false);
        //字段为null，自动忽略，不再序列化
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //XML标签名:使用骆驼命名的属性名，
        xmlMapper.setPropertyNamingStrategy(new UpperCaseSnackNamingStrategy());
        //设置转换模式
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
    }


    public static String toXml(Object object) {
        try {
            return xmlMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Xml转换失败", e);
            throw new RuntimeException("Xml转换失败", e);
        }
    }


    public static <T> T toObject(String xml, Class<T> c) {
        try {
            return xmlMapper.readValue(xml, c);
        } catch (IOException e) {
            log.error("Xml转换失败", e);
            throw new RuntimeException("Xml转换失败", e);
        }
    }


}
