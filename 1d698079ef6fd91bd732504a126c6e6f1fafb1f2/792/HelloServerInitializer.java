package com.lc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author HealerJean
 * @Description 通道的初始化类，规定通道的定界符、编码器、解码器
 * @Date   2018/3/27 下午4:02.
 */


public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// 以("\n")为结尾分割的 解码器，最大帧长度为8192
		// Delimiter Based Frame Decoder 基于定界符的帧解码器
		// Delimiters.lineDelimiter()行分隔符
		// 这里使用时，会把换行符作为一个消息的分隔界限使用，即接收时，换行符前为一条消息
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));

		// 字符串解码 和 编码
		// encoder 编码器， decoder 解码器
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("encoder", new StringEncoder());

		// 自己的逻辑Handler
		pipeline.addLast("handler", new HelloServerHandler());
	}
}
