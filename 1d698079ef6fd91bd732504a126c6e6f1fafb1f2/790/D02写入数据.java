package com.hlj.nio.D01标准Nio.D02写入数据;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/22  下午1:43.
 * 类描述：
 */
public class D02写入数据 {



    @Test
    public void writeFileByChannelToBuffer(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("src/main/java/com/hlj/nio/D01标准Nio/D02写入数据/write.txt") ;
            // 1、获取通道
            FileChannel channel = fileOutputStream.getChannel();

            // 2、创建缓冲区，就表示创建了一个容量为1024字节的ByteBuffer对象
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 3、缓冲区中放入数据
            byte[] text = "ABCDEFG".getBytes() ;
            for (int i=0; i<text.length; ++i) {
                buffer.put( text[i] );
            }

            // 4、切换buffe到读模式，这样才能被channel读取并且写入到文件中
            buffer.flip() ;

            //5、将缓冲区的写入到管道中去
            channel.write(buffer);

            //6、关闭channel
            channel.close();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
