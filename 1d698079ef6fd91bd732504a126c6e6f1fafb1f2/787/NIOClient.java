package com.hlj.nio.D02Nio网络编程;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/28  下午3:14.
 */

public class NIOClient {

    // 通道管理器
    private Selector selector;

    /**
     * 初始化客户端
     * @param ip 客户端Ip
     * @param port 服务端暴露出的端口
     */
    public void initClient(String ip, int port) throws IOException { // 获得一个Socket通道

        // 1、获得通道管理器
        this.selector = Selector.open(); // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen()方法中调

        //2、客户端获得通道
        SocketChannel channel = SocketChannel.open();
        // 设置通道为非阻塞
        channel.configureBlocking(false);

        // 3、连接服务端 ，能够得到服务器的响应，isConnectionPending 正在链接
        //  此时还没有完全建立 连接，相当于和服务器打了个招呼，说要建立链接
        //  后面使用，用channel.finishConnect();才能完成连接，
        channel.connect(new InetSocketAddress(ip, port));

        // 4、将该通道绑定的到通道管理器selector  ，注册的事件为连接事件（连接到服务器成功）
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    @SuppressWarnings("unchecked")
    public void listen() throws Exception { // 轮询访问selector
        while (true) {
            // 当注册事件到达时，方法返回，否则该方法会一直阻塞，阻塞到至少有一个通道在你注册的事件上就绪，方法将返回所发生的事件的数量。
            selector.select();
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已使用过的key ，ite删除的是上一个key 以防重负处理
                ite.remove();
                if (key.isConnectable()) { //表示已经连接上服务器了
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 如果正在连接，则完成连接，完成真正的连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    } // 设置成非阻塞
                    channel.configureBlocking(false);
                    // 在这里可以给服务端发送信息哦
                    channel.write(ByteBuffer.wrap(new String("hello server!").getBytes()));
                    // 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
                    channel.register(this.selector, SelectionKey.OP_READ); // 获得了可读的事件
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        // 穿件读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        channel.read(buffer);
        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("client receive msg from server:" + msg);
        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
        channel.write(outBuffer);

    }


    /**
     * 启动客户端测试
     */
    @Test
    public void startServer(){
        try {
            initClient("localhost",8989);
            listen();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}