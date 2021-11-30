package com.hlj.websocket.WebSocket;

import com.hlj.websocket.WebSocket.domain.WiselyMessage;
import com.hlj.websocket.WebSocket.domain.WiselyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 *
 * Created by HealerJean on 2017/4/8.
 */
@Controller
public class WsController {
    /**
     *  广播式
     */
    @MessageMapping("/welcome")//用户发送信息时候的地址
    @SendTo("/topic/getResponse") //服务器有消息时候，会对订阅了这个路径中的浏览器发送消息
    public WiselyResponse say(WiselyMessage message) throws  Exception{
        Thread.sleep(3000);
        return  new WiselyResponse("welcome,"+message.getName());
    }


    /**
     * 点对点
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/chat")
    public void handleChat(Principal principal,String msg){
        if (principal.getName().equals("zyj")){
            messagingTemplate.convertAndSendToUser("HealerJean","/queue/notifications",principal.getName()+"-send:"+msg);

        }else {
            messagingTemplate.convertAndSendToUser("zyj","/queue/notifications",principal.getName()+"-send:"+msg);
        }
    }







}
