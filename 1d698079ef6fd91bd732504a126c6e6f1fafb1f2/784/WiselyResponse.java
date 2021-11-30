package com.hlj.websocket.WebSocket.domain;

/**
 * 服务器向浏览器发送从此勒的消息
 */
public class WiselyResponse {
    private String responseMessage;
    public WiselyResponse(String responseMessage){
        this.responseMessage = responseMessage;
    }
    public String getResponseMessage(){
        return responseMessage;
    }
}