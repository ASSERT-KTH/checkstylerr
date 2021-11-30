package com.hlj.ddkj.ExceptionHandler;

/**
 * @author fengchuanbo
 */
public enum Code {

    OK("0", "OK"),
    ERROR("500", "系统错误"),
    illegalArgument("40001", "参数错误"),
    SESSION_CHECK_ERROR("40002","认证失败"),
    TOKEN_CHECK_ERROR("40003","token认证失败"),
    ;

    private String code;

    private String desc;

    Code(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

