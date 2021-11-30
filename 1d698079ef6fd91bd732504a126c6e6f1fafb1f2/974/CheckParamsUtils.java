package com.duodian.youhui.admin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author WangYuefei
 * @time 2019/2/28
 * @function 校验参数工具类
 */
public class CheckParamsUtils {

    private static Logger logger = LoggerFactory.getLogger(CheckParamsUtils.class);

    public static final String ALLPARAMS = "AllParams";
    public static final String ANYPARAMS = "AnyParams";

    /**
     * 校验参数类
     *
     * @param type 校验类型
     * @param obj  传入的参数类
     * @param str  需要校验的参数列表
     * @return
     */
    public static boolean checkParams(String type, Object obj, String... str) {
        if (ALLPARAMS.equals(type)) {
            try {
                return checkParamsAll(obj, str);
            } catch (Exception e) {
                logger.error("<验参工具类> 验全参时出现异常！");
                e.printStackTrace();
                return false;
            }
        } else if (ANYPARAMS.equals(type)) {
            try {
                return checkParamsAny(obj, str);
            } catch (Exception e) {
                logger.error("<验参工具类> 验任意参时出现异常！");
                e.printStackTrace();
                return false;
            }
        } else {
            logger.info("<验参工具类> 无此验参类型！");
            return false;
        }
    }

    /**
     * 校验类型为ALLPARAMS时调用此方法
     *
     * @param obj
     * @param str
     * @return
     */
    private static boolean checkParamsAll(Object obj, String... str) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            boolean flag = false;
            String fieldName = field.getName();
            for (String s : str) {
                if (s.equals(fieldName)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                Type genericType = field.getGenericType();
                // 如果类型是String
                if (genericType.toString().equals(
                        "class java.lang.String")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    String val = (String) m.invoke(obj);// 调用getter方法获取属性值
                    if (val == null || val.equals("")) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Long
                if (genericType.toString().equals(
                        "class java.lang.Long")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Long val = (Long) m.invoke(obj);// 调用getter方法获取属性值
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Integer
                if (field.getGenericType().toString().equals(
                        "class java.lang.Integer")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Integer val = (Integer) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Integer
                if (field.getGenericType().toString().equals(
                        "class java.lang.Byte")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Byte val = (Byte) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Double
                if (field.getGenericType().toString().equals(
                        "class java.lang.Double")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Double val = (Double) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Boolean 是封装类
                if (field.getGenericType().toString().equals(
                        "class java.lang.Boolean")) {
                    Method m = (Method) objClass.getMethod(
                            fieldName);
                    Boolean val = (Boolean) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是boolean 基本数据类型不一样 这里有点说名如果定义名是 isXXX的 那就全都是isXXX的
                // 反射找不到getter的具体名
                if (field.getGenericType().toString().equals("boolean")) {
                    Method m = (Method) objClass.getMethod(
                            fieldName);
                    Boolean val = (Boolean) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Date
                if (field.getGenericType().toString().equals(
                        "class java.util.Date")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Date val = (Date) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是Short
                if (field.getGenericType().toString().equals(
                        "class java.lang.Short")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Short val = (Short) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
                // 如果类型是BigDecimal
                if (field.getGenericType().toString().equals(
                        "class java.math.BigDecimal")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    BigDecimal val = (BigDecimal) m.invoke(obj);
                    if (val == null) {
                        return false;
                    }
                    continue;
                }
            }
        }
        return true;
    }

    /**
     * 校验类型为ANYPARAMS时调用此方法
     *
     * @param obj
     * @param str
     * @return
     */
    private static boolean checkParamsAny(Object obj, String... str) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            boolean flag = false;
            String fieldName = field.getName();
            for (String s : str) {
                if (s.equals(fieldName)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                Type genericType = field.getGenericType();
                // 如果类型是String
                if (genericType.toString().equals(
                        "class java.lang.String")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    String val = (String) m.invoke(obj);// 调用getter方法获取属性值
                    if (val != null && !val.equals("")) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Long
                if (genericType.toString().equals(
                        "class java.lang.Long")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Long val = (Long) m.invoke(obj);// 调用getter方法获取属性值
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Integer
                if (field.getGenericType().toString().equals(
                        "class java.lang.Integer")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Integer val = (Integer) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Integer
                if (field.getGenericType().toString().equals(
                        "class java.lang.Byte")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Byte val = (Byte) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Double
                if (field.getGenericType().toString().equals(
                        "class java.lang.Double")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Double val = (Double) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Boolean 是封装类
                if (field.getGenericType().toString().equals(
                        "class java.lang.Boolean")) {
                    Method m = (Method) objClass.getMethod(
                            fieldName);
                    Boolean val = (Boolean) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是boolean 基本数据类型不一样 这里有点说名如果定义名是 isXXX的 那就全都是isXXX的
                // 反射找不到getter的具体名
                if (field.getGenericType().toString().equals("boolean")) {
                    Method m = (Method) objClass.getMethod(
                            fieldName);
                    Boolean val = (Boolean) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Date
                if (field.getGenericType().toString().equals(
                        "class java.util.Date")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Date val = (Date) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是Short
                if (field.getGenericType().toString().equals(
                        "class java.lang.Short")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    Short val = (Short) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
                // 如果类型是BigDecimal
                if (field.getGenericType().toString().equals(
                        "class java.math.BigDecimal")) {
                    Method m = (Method) objClass.getMethod(
                            "get" + getMethodName(fieldName));
                    BigDecimal val = (BigDecimal) m.invoke(obj);
                    if (val != null) {
                        return true;
                    }
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * 把一个字符串的第一个字母大写
     *
     * @param fildeName
     * @return
     */
    private static String getMethodName(String fildeName) {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }

}

