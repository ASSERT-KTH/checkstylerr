package com.hlj.netty.websocket.bean;

/**
 * Created by j.sh on 27/11/2017.
 */
public class ResponseBean {

    private Boolean success;
    private String message;
    private Object result;


    public static ResponseBean buildSuccess(){
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        return responseBean;
    }

    public static ResponseBean buildSuccess(Object result){
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setResult(result);
        return responseBean;
    }

    public static ResponseBean buildFailure(){
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        return responseBean;
    }

    public static ResponseBean buildFailure(String message){
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setMessage(message);
        return responseBean;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
