package com.duodian.youhui.admin.utils;

import com.duodian.youhui.admin.Exceptions.AppException;

import java.lang.reflect.Field;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/24  下午4:30.
 * 类描述：判断是否为null 工具
 */
public class JudgeNullUtils {

    public static boolean isNull(Object object,String ... fieldName){
        try {
            for (int i = 0; i < fieldName.length; i++) {
                Field field = null;

                    field = object.getClass().getDeclaredField(fieldName[i]);

                field.setAccessible(true);//暴力反射，获取获取数据
                if(field.get(object)==null){
                    //返回flase或者直接抛出异常，根据我们的情况而定
                    throw  new AppException(fieldName[i]+"不能为空");
                }
            }
            return true ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false ;
    }

}
