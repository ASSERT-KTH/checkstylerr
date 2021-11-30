package com.hlj.springboot.dome.common.data;



/**
 * Created by j.sh on 2015/4/23.
 */
public class ResponseBean {

    private ResponseBean() {
    }

    public static ResponseBean buildSuccess() {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setCode(ErrorCodeEnum.正常.code);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    public static ResponseBean buildSuccess(Object result) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setCode(ErrorCodeEnum.正常.code);
        responseBean.setResult(result);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    public static ResponseBean buildFailure() {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setCode(ErrorCodeEnum.系统错误.code);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    public static ResponseBean buildFailure(String message) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setCode(ErrorCodeEnum.系统错误.code);
        responseBean.setMessage(message);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    public static ResponseBean buildFailure(ErrorCodeEnum errorCodeEnum) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setCode(errorCodeEnum.code);
        responseBean.setMessage(errorCodeEnum.desc);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    public static ResponseBean buildFailure(String code,String message) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setCode(code);
        responseBean.setMessage(message);
        responseBean.setDate(System.currentTimeMillis()+"");
        return responseBean;
    }

    private boolean success;
    private Object result = "{}";
    private String message = "";
    private String code = "";
    private String date ;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
