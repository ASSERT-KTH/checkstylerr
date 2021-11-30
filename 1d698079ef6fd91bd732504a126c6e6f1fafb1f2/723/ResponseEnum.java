package com.hlj.proj.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ResponseEnum
 * @date 2019/6/13  20:45.
 * @msgcription
 */
public enum ResponseEnum   {


    正常(200,"访问正常"),

    条件验证失败(201,"条件验证失败"),
    参数解析失败(202,"参数解析失败"),
    操作确认(203,"操作确认"),

    逻辑错误(301,"逻辑错误"),

    登录标识过期(400,"登录标识过期"),
    强制升级(401,"强制升级"),
    访问禁止(403,"访问禁止"),
    页面丢失(404,"页面丢失"),
    系统错误(500,"系统内部错误"),
    未登录(204,"未登录"),
    未知错误(999,"未知错误"),

     ;


    public int code;
    public String msg;

    ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public ResponseEnum value(String enumName){
        return valueOf( enumName ) ;
    }

    public static boolean checkExist( Integer code){
        for (ResponseEnum value : ResponseEnum.values()){
            if (value.code == code){
                return true;
            }
        }
        return false;
    }


    public static ResponseEnum getStatus(int code){
        for (ResponseEnum value : ResponseEnum.values()){
            if (value.code == code){
                return value;
            }
        }
        return ResponseEnum.未知错误;
    }

    public static String getmsg(int code){
        for (ResponseEnum value : ResponseEnum.values()){
            if (value.code == code){
                return value.msg;
            }
        }
        return ResponseEnum.未知错误.msg;
    }

    public static List<ResponseEnum> getTypeList(){
        return Arrays.asList(values());
    }

}
