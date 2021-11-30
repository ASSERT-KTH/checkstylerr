package com.hlj.netty.websocket;

import com.hlj.netty.websocket.server.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComHljNettyWebsocketApplication  implements CommandLineRunner {

	@Value("${server.port2}")
	private int port;

	public static void main(String[] args) {
		SpringApplication.run(ComHljNettyWebsocketApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		// 事件 循环 组
		/*
		 * Main函数开始的位置定义了两个工作线程，一个命名为WorkerGroup，另一个命名为BossGroup。
		 * 都是实例化NioEventLoopGroup。
		 * EventLoopGroup，它用于管理Channel连接的。
		 */
		//NioEventLoopGroup可以理解为一个线程池，内部维护了一组线程，每个线程负责处理多个Channel上的事件，
		// 而一个Channel只对应于一个线程，这样可以回避多线程下的数据同步问题。

		// Boss线程：由这个线程池提供的线程是boss种类的，用于创建、连接、绑定socket， （有点像门卫）然后把这些socket传给worker线程池。
                   // 在服务器端每个监听的socket都有一个boss线程来处理。在客户端，只有一个boss线程来处理所有的socket。
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);

		// Worker线程：Worker线程执行所有的异步I/O，即处理操作,Worker线程用于管理线程为Boss线程服务。
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
//			启动NIO服务的辅助启动类,负责初始话netty服务器，并且开始监听端口的socket请求
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)// 设置非阻塞,用它来建立新accept的连接,用于构造serversocketchannel的工厂类
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new WebSocketServerInitializer()); ///通道的初始化程序, 对出入的数据进行的业务操作,其继承ChannelInitializer

			Channel ch = bootstrap.bind(port).sync().channel();
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
