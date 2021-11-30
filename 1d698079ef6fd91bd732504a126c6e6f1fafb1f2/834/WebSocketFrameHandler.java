package com.hlj.netty.websocket.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlj.netty.websocket.bean.RequestBean;
import com.hlj.netty.websocket.selector.ChannelRelation;
import com.hlj.netty.websocket.selector.ChannelSelector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;


/**
 * Created by j.sh on 27/11/2017.
 */

public class WebSocketFrameHandler  extends SimpleChannelInboundHandler<WebSocketFrame> {

    private ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketFrameHandler() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            // 返回应答消息
            final String request = ((TextWebSocketFrame) frame).text();

            RequestBean requestBean = objectMapper.readValue(request,RequestBean.class);
             //如果不是初始化的RequestBean 0 不是初始化
            if (requestBean.getInit() == null || requestBean.getInit() == 0) {
                // 转发消息 获取通道，通过 用户的id获取通道id，再通过通道id获取通道
                Channel channel = ChannelSelector.getChannel(ChannelRelation.getChannelId(requestBean.getTo()));
                channel.writeAndFlush(new TextWebSocketFrame(request));
            } else {
                //初始化
                ChannelRelation.addRelation(requestBean.getFrom(),ctx.channel());
            }
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }


}