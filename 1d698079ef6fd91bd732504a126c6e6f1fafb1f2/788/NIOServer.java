package com.hlj.nio.D02Nio网络编程;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/28  下午3:14.
 */



public class NIOServer {

    // 通道管理器
    private Selector selector;

    public void initServer(int port) throws Exception {
        // 1、获得通道管理器
        this.selector = Selector.open();

        // 2、获得一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        //    2.1、设置通道为 非阻塞 ，注册的Channel 必须设置成异步模式 才可以,，否则异步IO就无法工作，
        //                  这就意味着我们不能把一个FileChannel注册到Selector，因为`FileChannel`没有异步模式，
        //                  但是网络编程中的`SocketChannel`是可以的。
        serverChannel.configureBlocking(false);

        //    2.2、将该通道对于的serverSocket绑定到port端口
        ServerSocket serverSocket =  serverChannel.socket() ;
        serverSocket.bind(new InetSocketAddress(port));

        // 3、channel注册到selector
        //      第二个参数为，selector对那些事件感兴趣 ,即指定我们想要监听accept事件，也就是新的连接发生时所产生的事件，对于ServerSocketChannel通道来说，我们唯一可以指定的参数就是OP_ACCEPT。
        //  将通道管理器和该通道绑定，并为该通道注册selectionKey.OP_ACCEPT事件
        //  注册该事件后，当事件到达的时候，selector.select()会返回， 如果事件没有到达selector.select()会一直阻塞

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    // 采用轮训的方式监听selector上是否有需要处理的事件，如果有，进行处理
    public void listen() throws Exception {

        //启动服务器
        System.out.println("start server");

        // 轮询访问selector
        while (true) {
            // 当注册事件到达时，方法返回，否则该方法会一直阻塞，阻塞到至少有一个通道在你注册的事件上就绪，方法将返回所发生的事件的数量。
            selector.select();
            // 获得selector中选中的相的迭代器，选中的相为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已使用过的key ，ite删除的是上一个key 以防重负处理
                ite.remove();
                // 客户端请求连接事件成功
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获得客户端连接的通道
                    SocketChannel channel = server.accept();
                    // 设置成非阻塞
                    channel.configureBlocking(false);
                    // 在这里可以发送消息给客户端
                    channel.write(ByteBuffer.wrap(new String("hello client").getBytes()));
                    // 在客户端 连接成功之后，注册到 服务端的通道管理器中，接收到客户端的信息，第二个是监听读的兴趣，
                    channel.register(this.selector, SelectionKey.OP_READ);
                    // 获得了可读的事件

                 //获取客户端的channel的数据
                } else if (key.isReadable()) {
                    read(key);
                }

            }
        }
    }

    // 处理 读取客户端发来的信息事件
    private void read(SelectionKey key) throws Exception {
        // 服务器可读消息，得到事件发生的socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 穿件读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        channel.read(buffer);

        byte[] data = buffer.array();
        String msg = new String(data).trim();
        System.out.println("server receive from client: " + msg);

        //服务端给客户端回复内容
        ByteBuffer outBuffer = ByteBuffer.wrap(("来自于服务端的回复"+msg).getBytes());
        channel.write(outBuffer);
    }

    @Test
    public void startServer(){
        try {
            initServer(8989);
            listen();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}