package com.hlj.springboot.dome.anno.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlj.springboot.dome.anno.JsonAnnoSerializer;
import com.hlj.springboot.dome.anno.JSON;
import com.hlj.springboot.dome.common.data.ResponseBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/20  上午11:21.
 */
@Aspect
@Component
public class JsonInterceptor {

    /**
     * ObjectMapper
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /**
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.hlj.springboot.dome.anno.JSON) || @within(com.hlj.springboot.dome.anno.JSON)")
    protected Object invoke(ProceedingJoinPoint point) throws Throwable {
        JSON json = null;
        Signature signature = point.getSignature();
        if (signature instanceof MethodSignature) {
            Method method = ((MethodSignature) signature).getMethod();
            json = method.getAnnotation(JSON.class);
        }
        if (json == null) {
            return point.proceed();
        }

        try {
            Object object = point.proceed();
            JsonAnnoSerializer jsonSerializer = new JsonAnnoSerializer();
            jsonSerializer.filter(json.type(), json.include(), json.filter());

            //如果包装类是ResponBean
            if(ResponseBean.class.getName().equals(  object.getClass().getName())){ //如果返回类型是ResponseBean
                ResponseBean responseBean = (ResponseBean)object;
                 String innerJsonStr = jsonSerializer.toJson(responseBean.getResult());
                 Object jsonT = new JSONTokener(innerJsonStr).nextValue();

                if(jsonT instanceof JSONObject){
                       responseBean.setResult (OBJECT_MAPPER.readValue(innerJsonStr, json.type()));
                }else if (jsonT instanceof JSONArray) {
                       responseBean.setResult (OBJECT_MAPPER.readValue(innerJsonStr, new TypeReference<List>() { } ));
                }
                return  responseBean ;
            }

            //没有包装类
            String resultJson = jsonSerializer.toJson(object);
            Object jsonT = new JSONTokener(resultJson).nextValue();
            if(jsonT instanceof JSONObject){
                return OBJECT_MAPPER.readValue(resultJson, json.type());
            }else if (jsonT instanceof JSONArray) {
                return OBJECT_MAPPER.readValue(resultJson, new TypeReference<List>() { } );
            }
            return   object;
        } catch (Exception e) {
            throw e;
        }
    }



}
