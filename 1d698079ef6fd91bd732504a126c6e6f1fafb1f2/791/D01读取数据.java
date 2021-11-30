package com.hlj.nio.D01标准Nio.D01读取数据;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/22  下午12:06.
 * 类描述：

  1、流程
     1、	从FileInputStream获取Channel
     2、	创建Buffer
     3、	从Channel读取数据到Buffer
     4、切换到读取模式
     5、读取数据
     6、

 */
public class D01读取数据 {



    @Test
    public void readFileByChannelToBuffer(){

        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/java/com/hlj/nio/D01标准Nio/D01读取数据/read.txt") ;
            // 1、获取通道
            FileChannel channel = fileInputStream.getChannel();

            // 2、创建缓冲区，就表示创建了一个容量为1024字节的ByteBuffer对象
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //3、读取数据到缓冲区
            int n  =  channel.read(buffer); //返回的是缓冲区里面的字节数

            //4、读取模式
            buffer.flip();

//            返回的 array 长度为 ByteBuffer allocate的长度，并不是里面所含的内容的长度
//            byte[] data = buffer.array();
//            String msg = new String(data).trim();
//            System.out.println(msg);

            //5、按照缓冲区字节读取
            while (buffer.remaining()>0) {
                byte b = buffer.get();
                System.out.print(((char)b)); //读取中文会乱码哦
            }

            //6、读取完成清空缓冲区
            buffer.clear();

            //7、关闭channel
            channel.close();
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
