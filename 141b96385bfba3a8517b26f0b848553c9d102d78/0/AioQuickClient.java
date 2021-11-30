/*******************************************************************************
 * Copyright (c) 2017-2019, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: AioQuickClient.java
 * Date: 2019-12-31
 * Author: sandao (zhengjunweimail@163.com)
 *
 ******************************************************************************/


package org.smartboot.socket.transport;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.buffer.BufferFactory;
import org.smartboot.socket.buffer.BufferPagePool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * AIO实现的客户端服务。
 *
 *
 * <h2>示例：</h2>
 * <p>
 * <pre>
 * public class IntegerClient {
 *      public static void main(String[] args) throws Exception {
 *          IntegerClientProcessor processor = new IntegerClientProcessor();
 *          AioQuickClient<Integer> aioQuickClient = new AioQuickClient<Integer>("localhost", 8888, new IntegerProtocol(), processor);
 *          aioQuickClient.start();
 *          processor.getSession().write(1);
 *          processor.getSession().close(false);//待数据发送完毕后关闭
 *          aioQuickClient.shutdown();
 *      }
 * }
 * </pre>
 * </p>
 *
 * @param <T> 消息对象类型
 * @author 三刀
 * @version V1.0.0
 */
public final class AioQuickClient<T> {
    /**
     * 客户端服务配置。
     * <p>调用AioQuickClient的各setXX()方法，都是为了设置config的各配置项</p>
     */
    private IoServerConfig<T> config = new IoServerConfig<>();
    /**
     * 网络连接的会话对象
     *
     * @see TcpAioSession
     */
    private TcpAioSession<T> session;
    /**
     * 内存池
     */
    private BufferPagePool bufferPool = null;

    private BufferPagePool innerBufferPool = null;
    /**
     * IO事件处理线程组。
     * <p>
     * 作为客户端，该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需。
     * </p>
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    /**
     * 绑定本地地址
     */
    private SocketAddress localAddress;

    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * 当前构造方法设置了启动Aio客户端的必要参数，基本实现开箱即用。
     *
     * @param host             远程服务器地址
     * @param port             远程服务器端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioQuickClient(String host, int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        config.setHost(host);
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
    }

    /**
     * 启动客户端。
     * <p>
     * 在与服务端建立连接期间，该方法处于阻塞状态。直至连接建立成功，或者发生异常。
     * </p>
     * <p>
     * 该start方法支持外部指定AsynchronousChannelGroup，实现多个客户端共享一组线程池资源，有效提升资源利用率。
     * </p>
     *
     * @param asynchronousChannelGroup IO事件处理线程组
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     */
    public AioSession<T> start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        AsynchronousSocketChannel socketChannel = null;
        try {
            socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
            if (bufferPool == null) {
                bufferPool = config.getBufferFactory().create();
                this.innerBufferPool = bufferPool;
            }
            //set socket options
            if (config.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                    socketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            //bind host
            if (localAddress != null) {
                socketChannel.bind(localAddress);
            }
            Future<Void> future = socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort()));
            if (connectTimeout > 0) {
                future.get(connectTimeout, TimeUnit.MILLISECONDS);
            } else {
                future.get();
            }

            AsynchronousSocketChannel connectedChannel = socketChannel;
            if (config.getMonitor() != null) {
                connectedChannel = config.getMonitor().shouldAccept(socketChannel);
            }
            if (connectedChannel == null) {
                IOUtil.close(socketChannel);
                shutdownNow();
                throw new RuntimeException("NetMonitor refuse channel");
            }
            //连接成功则构造AIOSession对象
            session = new TcpAioSession<T>(connectedChannel, config, new ReadCompletionHandler<T>(), new WriteCompletionHandler<T>(), bufferPool.allocateBufferPage());
            session.initSession();
            return session;
        } catch (Exception e) {
            if (socketChannel != null) {
                IOUtil.close(socketChannel);
            }
            shutdownNow();
            throw new IOException(e);
        }
    }

    /**
     * 启动客户端。
     *
     * <p>
     * 本方法会构建线程数为2的{@code asynchronousChannelGroup}，并通过调用{@link AioQuickClient#start(AsynchronousChannelGroup)}启动服务。
     * </p>
     *
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AioQuickClient#start(AsynchronousChannelGroup)
     */
    public final AioSession<T> start() throws IOException {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        return start(asynchronousChannelGroup);
    }

    /**
     * 停止客户端服务.
     * <p>
     * 调用该方法会触发AioSession的close方法，并且如果当前客户端若是通过执行{@link AioQuickClient#start()}方法构建的，同时会触发asynchronousChannelGroup的shutdown动作。
     * </p>
     */
    public final void shutdown() {
        showdown0(false);
    }

    /**
     * 立即关闭客户端
     */
    public final void shutdownNow() {
        showdown0(true);
    }

    /**
     * 停止client
     *
     * @param flag 是否立即停止
     */
    private void showdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
        if (innerBufferPool != null) {
            innerBufferPool.release();
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient<T> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置
     * <p>
     * AIO客户端的有效可选范围为：<br/>
     * 1. StandardSocketOptions.SO_SNDBUF<br/>
     * 2. StandardSocketOptions.SO_RCVBUF<br/>
     * 3. StandardSocketOptions.SO_KEEPALIVE<br/>
     * 4. StandardSocketOptions.SO_REUSEADDR<br/>
     * 5. StandardSocketOptions.TCP_NODELAY
     * </p>
     *
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          泛型
     * @return 当前AIOQuickClient对象
     */
    public final <V> AioQuickClient<T> setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 绑定本机地址、端口用于连接远程服务
     *
     * @param local 若传null则由系统自动获取
     * @param port  若传0则由系统指定
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient<T> bindLocal(String local, int port) {
        localAddress = local == null ? new InetSocketAddress(port) : new InetSocketAddress(local, port);
        return this;
    }

    /**
     * 设置内存池。
     * 通过该方法设置的内存池，在AioQuickClient执行shutdown时不会触发内存池的释放。
     * 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient<T> setBufferPagePool(BufferPagePool bufferPool) {
        this.bufferPool = bufferPool;
        this.config.setBufferFactory(BufferFactory.DISABLED_BUFFER_FACTORY);
        return this;
    }

    /**
     * 设置内存池的构造工厂。
     * 通过工厂形式生成的内存池会强绑定到当前AioQuickClient对象，
     * 在AioQuickClient执行shutdown时会释放内存池。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferFactory 内存池工厂
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient<T> setBufferFactory(BufferFactory bufferFactory) {
        this.config.setBufferFactory(bufferFactory);
        this.bufferPool = null;
        return this;
    }

    /**
     * 设置输出缓冲区容量
     *
     * @param bufferSize     单个内存块大小
     * @param bufferCapacity 内存块数量上限
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient<T> setWriteBuffer(int bufferSize, int bufferCapacity) {
        config.setWriteBufferSize(bufferSize);
        config.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 客户端连接超时时间，单位:毫秒
     *
     * @param timeout 超时时间
     * @return
     */
    public final AioQuickClient<T> connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

}
