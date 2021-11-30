package com.lc.client2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * netty客户端
 * 
 * @author HealerJean
 * @date 2017年6月26日 下午5:37:23
 * 
 */


public class HelloClient {

	public static String host = "127.0.0.1";
	public static int port = 7878;

	public static void main(String[] args) throws IOException, InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new HelloClientInitializer());

		// 连接服务端
		Channel ch = b.connect(host, port).sync().channel();

		// 控制台输入
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		for (;;) {
			String line = in.readLine();
			if (line == null)
				continue;
			ch.writeAndFlush(line + "\r\n");
		}
	}
}
