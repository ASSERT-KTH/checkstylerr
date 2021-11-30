package com.hlj.netty.websocket.server;

import com.hlj.netty.websocket.handler.ChannelStatusHandler;
import com.hlj.netty.websocket.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * Created by j.sh on 20/03/2018.
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        //// HttpServerCodec：将请求和应答消息解码为HTTP消息
        pipeline.addLast(new HttpServerCodec());
        // max 1kb, HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
        pipeline.addLast(new HttpObjectAggregator(1024));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(new WebSocketFrameHandler());//// 在管道中添加我们自己的接收数据实现方法
        pipeline.addLast(new ChannelStatusHandler()); //将传来的用户id和生成的管道进行保存
    }

}