package com.hlj.netty.websocket;

import com.hlj.netty.websocket.executor.AsyncExecutor;
import com.hlj.netty.websocket.server.TransferServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/9
 * version：1.0.0
 */
@SpringBootApplication
@ImportResource(value = "classpath:applicationContext.xml")
@EnableAsync
public class ComHljNettyWebsocketApplication implements CommandLineRunner {

	@Value("${server.ssl:false}")
	private boolean ssl;

	@Value("${server.port2}")
	private int port;

	public static void main(String[] args) {
		SpringApplication.run(ComHljNettyWebsocketApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		// start async executor
		AsyncExecutor.start();

		// start websocket
		final SslContext sslCtx;//如果是https 的，添加 ，如果没有则删除相关代码即可
		if (ssl) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new TransferServerInitializer(sslCtx));

			Channel ch = bootstrap.bind(port).sync().channel();
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}


