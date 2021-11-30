package com.healerjean.proj.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ResponseEnum
 * @date 2019/6/13  20:45.
 * @msgcription
 */
public enum ResponseEnum {


    正常(200, "访问正常"),

    参数错误(301, "参数错误"),
    参数格式异常(302, "参数格式异常"),
    不支持的请求方式(303, "不支持的请求方式"),
    参数类型错误(304, "参数类型错误"),
    逻辑错误(305, "逻辑错误"),
    未登陆(306, "未登陆"),
    登陆成功(307, "登陆成功"),
    重复操作(308, "重复操作"),
    非法操作(309, "非法操作"),

    请求无法被服务器理解(400, "请求无法被服务器理解"),
    未授权(401, "未授权"),
    访问禁止(403, "访问禁止"),
    页面丢失(404, "页面丢失"),
    系统错误(500, "系统错误"),
    未知错误(999, "未知错误"),


    用户已经存在(1000, "用户已经存在"),
    用户不存在(1001, "用户不存在"),
    用户创建失败(1002, "用户创建失败"),


    微信接口请求异常(2001, "微信接口请求异常"),
    淘宝接口请求异常(2002, "淘宝接口请求异常"),
    淘宝接口数据异常(2003, "淘宝接口数据异常"),
    好单库口请求异常(2004, "好单库口请求异常"),
    好单库接口数据异常(2005, "好单库接口数据异常"),

    ;


    public int code;
    public String msg;


    ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public static boolean checkExist(Integer code) {
        for (ResponseEnum value : ResponseEnum.values()) {
            if (value.code == code) {
                return true;
            }
        }
        return false;
    }


    public static ResponseEnum toEnum(int code) {
        for (ResponseEnum value : ResponseEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return ResponseEnum.未知错误;
    }

    public static String getMsg(int code) {
        for (ResponseEnum value : ResponseEnum.values()) {
            if (value.code == code) {
                return value.msg;
            }
        }
        return ResponseEnum.未知错误.msg;
    }


    public ResponseEnum value(String enumName) {
        return valueOf(enumName);
    }

    public static List<ResponseEnum> getList() {
        return Arrays.asList(values());
    }


}
