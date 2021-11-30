package com.hlj.utils;

public interface SysEnum {


    /**
     * 返回httpStatus枚举
     */
    enum ResponseEnum implements SysEnum {

        /** 参数错误 */
        OK(200, "成功"),
        SYSTEM_ERROR(497, "系统错误"),
        BAD_REQUEST(400, "请求无法被服务器理解"),

        ;

        private int code;
        private String msg;

        ResponseEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

         public static ResponseEnum match( Integer code){
            for (ResponseEnum value : ResponseEnum.values()){
                if (value.code == code){
                    return value;
                }
            }
            return null ;
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



}
