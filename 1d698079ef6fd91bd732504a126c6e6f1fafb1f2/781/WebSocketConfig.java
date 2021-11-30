package com.hlj.websocket.WebSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * 1 通过 @EnableWebSocketMessageBroker
 * 开启使用stomp协议来传输 基于代理的 message broker,
 * 这个时候控制器支持使用MessageMapping
 * */
@Configuration
@EnableWebSocketMessageBroker //1
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    /**
     * 注册Stomp的协议的节点，并映射指定的url
     * @param registry
     */
	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //注册一个stomp的节点，并指定使用SockJS协议，提供给用户进行连接
        //广播式
	    registry.addEndpoint("/endpointWisely").withSockJS();
	    //点对点式
        registry.addEndpoint("/endpointChat").withSockJS();//1
    }

    /**
     * 配置信息代理
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //广播式应配置一个topic的消息代理  ，表示客户端订阅地址的前缀信息，也就是客户端接收服务端消息的地址的前缀信息，@SendTo("/topic/getResponse")
        //点对点式queue，这里的queue 为用户中间互相订阅的url前缀 和上面的不一样
        registry.enableSimpleBroker("/topic","/queue"); //2
       

    }

}
