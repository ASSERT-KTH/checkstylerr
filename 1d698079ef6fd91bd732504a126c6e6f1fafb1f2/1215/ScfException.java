package com.hlj.utils;

public class ScfException extends RuntimeException {

    /** 返回错误码 */
    private int code;
    /** 返回错误信息 */
    private String msg;

    public ScfException(int code) {
        this.code = code;
    }

    public ScfException(SysEnum.ResponseEnum responseEnum) {
        this.code = responseEnum.getCode();
        this.msg = responseEnum.getMsg();
    }

    public ScfException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public ScfException(Exception e , int code , String msg) {
        super(e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

