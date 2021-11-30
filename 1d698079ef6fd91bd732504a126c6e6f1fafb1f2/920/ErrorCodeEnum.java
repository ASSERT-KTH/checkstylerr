package com.duodian.youhui.enums.exception;

/**
 * 类名称：ErrorCodeEnum
 * 类描述：错误状态枚举值
 * 创建人：qingxu
 * 修改人：
 * 修改时间：2015/11/5 11:27
 * 修改备注：
 *
 * @version 1.0.0
 */
public enum ErrorCodeEnum {

    正常("200","访问正常"),
    条件验证失败("201","条件验证失败"),
    参数解析失败("202","参数解析失败"),
    操作确认("203","操作确认"),
    逻辑错误("301","逻辑错误"),
    登录标识过期("400","登录标识过期"),
    强制升级("401","强制升级"),
    访问禁止("403","访问禁止"),
    页面丢失("404","页面丢失"),
    系统错误("500","系统内部错误"),
    未登录("204","未登录"),
    不是企业认证("205","不是企业认证"),
    未知错误("999","未知错误"),
    token认证失败("1004", "token认证失败"),
    操作频繁("40010", "操作频繁"),

    ;

    public String code;

    public String desc;

    ErrorCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code){
        if(code == null || "".equals(code)){
            return ErrorCodeEnum.未知错误.desc;
        }
        for(ErrorCodeEnum desc: ErrorCodeEnum.values()){
            if(desc.code.equals(code)){
                return desc.desc;
            }
        }
        return ErrorCodeEnum.未知错误.desc;
    }
}
