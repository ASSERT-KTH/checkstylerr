package com.hlj.netty.websocket.handler;


import com.hlj.netty.websocket.bean.RequestBean;
import com.hlj.netty.websocket.bean.ResponseBean;
import com.hlj.netty.websocket.executor.AsyncExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.PromiseNotifier;
import net.sf.json.JSONObject;

/**
 * Created by j.sh on 27/11/2017.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            final String request = ((TextWebSocketFrame) frame).text();

            //它判断异步任务执行的状态，如果执行完成，就理解通知监听者，否则加入到监听者队列
            DefaultPromise<String> promise = new DefaultPromise<>(ctx.executor()) ;
            promise.addListener(new PromiseNotifier<String,DefaultPromise<String>>(){
                @Override
                public void operationComplete(DefaultPromise<String> future) throws Exception {
                    if (!future.isSuccess()) {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.fromObject(ResponseBean.buildFailure(future.cause().getMessage())).toString()));
                    }
                }
            });
            AsyncExecutor.offerQueue(new RequestBean(request,ctx.channel(),promise));
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
}

