package com.lc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
/**
 * @Description  netty服务端
 * @Date   2018/3/27 下午4:16.
 */

public class HelloServer {

	private static final int protNumner = 7878;

	public static void main(String[] args) throws InterruptedException {
		// 事件 循环 组
		/*
		 * Main函数开始的位置定义了两个工作线程，一个命名为WorkerGroup，另一个命名为BossGroup。
		 * 都是实例化NioEventLoopGroup。这一点和3.x版本中基本思路是一致的。Worker线程用于管理线程为Boss线程服务。
		 * EventLoopGroup，它是4.x版本提出来的一个新概念。类似于3.x版本中的线程。用于管理Channel连接的。
		 * 在main函数的结尾就用到了EventLoopGroup提供的便捷的方法，shutdownGraceFully()，
		 * 翻译为中文就是优雅的全部关闭。
		 */
		//NioEventLoopGroup可以理解为一个线程池，内部维护了一组线程，每个线程负责处理多个Channel上的事件，
		// 而一个Channel只对应于一个线程，这样可以回避多线程下的数据同步问题。

		EventLoopGroup workerGroup = new NioEventLoopGroup();
		EventLoopGroup bossGroup = new NioEventLoopGroup();

		/* ServerBootstrap负责初始化netty服务器，并且开始监听端口的socket请求 */
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class); //通道的业务处理程序
		b.childHandler(new HelloServerInitializer());//通道的初始化程序

		// 服务器绑定端口监听
		ChannelFuture f = b.bind(protNumner).sync();
		// 监听服务器关闭监听
		f.channel().closeFuture().sync();
	}
}
