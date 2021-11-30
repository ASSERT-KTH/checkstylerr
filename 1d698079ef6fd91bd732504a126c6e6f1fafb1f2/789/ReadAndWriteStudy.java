package com.hlj.nio.D01标准Nio.D03读写结合;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/27  下午6:41.
 */
public class ReadAndWriteStudy {



    @Test
    public void readToWrite(){
        String read = "src/main/java/com/hlj/nio/D01标准Nio/D03读写结合/read.txt";
        String write = "src/main/java/com/hlj/nio/D01标准Nio/D03读写结合/write.txt";
        copyFileUseNIO(read,write);
    }

    /**
     * 用java NIO api拷贝文件
     * @param src
     * @param dst
     * @throws IOException
     */
    public  void copyFileUseNIO(String src,String dst){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(src));
            FileOutputStream fileOutputStream=new FileOutputStream(new File(dst));
            //1、获得传输通道channel
            FileChannel readChannel=fileInputStream.getChannel();
            FileChannel wrieteChannel=fileOutputStream.getChannel();

            //2、获得容器buffer，代表每次可以最多写入1024个字节
            ByteBuffer buffer=ByteBuffer.allocate(1024);

            while (true){
                //3、读取数据到缓冲区，每次最多1024个字节
                int n = readChannel.read(buffer);
                if(n==-1){
                    break;
                }

                //3、切换到读模式，这样才能被被channel读取，写入到文件中去
                buffer.flip() ;
                wrieteChannel.write(buffer);

                //4、清空缓冲区，用于 readChannel.read(buffer) 进行写入
                buffer.clear() ;

            }

            readChannel.close();
            wrieteChannel.close();
            fileOutputStream.close();
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
