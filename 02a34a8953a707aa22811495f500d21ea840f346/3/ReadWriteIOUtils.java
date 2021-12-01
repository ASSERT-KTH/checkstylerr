package cn.edu.tsinghua.tsfile.common.utils;

import cn.edu.tsinghua.tsfile.file.metadata.*;
import cn.edu.tsinghua.tsfile.file.metadata.enums.CompressionType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSEncoding;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSFreqType;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * ConverterUtils is a utility class. It provide conversion between normal datatype and byte array.
 *
 * @author East
 */
public class ReadWriteIOUtils {

    public static int SHORT_LEN = 2;
    public static int INT_LEN = 4;
    public static int LONG_LEN = 8;
    public static int DOUBLE_LEN = 8;
    public static int FLOAT_LEN = 4;
    public static int BOOLEAN_LEN = 1;


    public static int write(Boolean flag, OutputStream outputStream) throws IOException {
        if(flag)outputStream.write(1);
        else outputStream.write(0);
        return 1;
    }
    public static int write(Boolean flag, ByteBuffer buffer) throws IOException {
        byte a;
        if(flag)a = 1;
        else a = 0;

        buffer.put(a);
        return 1;
    }

    public static boolean readBool(InputStream inputStream) throws IOException {
        int flag = inputStream.read();
        return flag == 1;
    }
    public static boolean readBool(ByteBuffer buffer) throws IOException {
        byte a = buffer.get();
        return a == 1;
    }

    public static int writeIsNull(Object object, OutputStream outputStream) throws IOException {
        return write(object != null, outputStream);
    }
    public static int writeIsNull(Object object, ByteBuffer buffer) throws IOException {
        return write(object != null, buffer);
    }

    public static boolean readIsNull(InputStream inputStream) throws IOException {
        return readBool(inputStream);
    }
    public static boolean readIsNull(ByteBuffer buffer) throws IOException {
        return readBool(buffer);
    }

    public static int write(short n, OutputStream outputStream) throws IOException {
        byte[] bytes = BytesUtils.shortToBytes(n);
        outputStream.write(bytes);
        return bytes.length;
    }
    public static int write(short n, ByteBuffer buffer) throws IOException {
        buffer.putShort(n);
        return SHORT_LEN;
    }

    public static short readShort(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[SHORT_LEN];
        inputStream.read(bytes);
        return BytesUtils.bytesToShort(bytes);
    }
    public static short readShort(ByteBuffer buffer) throws IOException {
        short n = buffer.getShort();
        return n;
    }

    public static int write(int n, OutputStream outputStream) throws IOException {
        byte[] bytes = BytesUtils.intToBytes(n);
        outputStream.write(bytes);
        return bytes.length;
    }
    public static int write(int n, ByteBuffer buffer) throws IOException {
        buffer.putInt(n);
        return INT_LEN;
    }

    public static int write(float n, OutputStream outputStream) throws IOException {
        byte[] bytes= BytesUtils.floatToBytes(n);
        outputStream.write(bytes);
        return FLOAT_LEN;
    }

    public static float readFloat(InputStream inputStream) throws IOException {
        byte[] bytes= new byte[FLOAT_LEN];
        inputStream.read(bytes);
        return BytesUtils.bytesToFloat(bytes);
    }

    public static int write(double n, OutputStream outputStream) throws IOException {
        byte[] bytes= BytesUtils.doubleToBytes(n);
        outputStream.write(bytes);
        return DOUBLE_LEN;
    }

    public static double readDouble(InputStream inputStream) throws IOException {
        byte[] bytes= new byte[DOUBLE_LEN];
        inputStream.read(bytes);
        return BytesUtils.bytesToDouble(bytes);
    }

    public static int readInt(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[INT_LEN];
        inputStream.read(bytes);
        return BytesUtils.bytesToInt(bytes);
    }
    public static int readInt(ByteBuffer buffer) throws IOException {
        int n = buffer.getInt();
        return n;
    }

    public static int write(long n, OutputStream outputStream) throws IOException {
        byte[] bytes = BytesUtils.longToBytes(n);
        outputStream.write(bytes);
        return bytes.length;
    }
    public static int write(long n, ByteBuffer buffer) throws IOException {
        buffer.putLong(n);
        return LONG_LEN;
    }

    public static long readLong(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[LONG_LEN];
        inputStream.read(bytes);
        return BytesUtils.bytesToLong(bytes);
    }
    public static long readLong(ByteBuffer buffer) throws IOException {
        long n = buffer.getLong();
        return n;
    }

    public static int write(String s, OutputStream outputStream) throws IOException {
        int len = 0;
        len += write(s.length(), outputStream);
        byte[] bytes = s.getBytes();
        outputStream.write(bytes);
        len += bytes.length;
        return len;
    }
    public static int write(String s, ByteBuffer buffer) throws IOException {
        int len = 0;
        len += write(s.length(), buffer);
        byte[] bytes = s.getBytes();
        buffer.put(bytes);
        len += bytes.length;
        return len;
    }

    public static String readString(InputStream inputStream) throws IOException {
        int sLength = readInt(inputStream);
        byte[] bytes = new byte[sLength];
        inputStream.read(bytes, 0, sLength);
        return new String(bytes, 0, sLength);
    }
    public static String readString(ByteBuffer buffer) throws IOException {
        int sLength = readInt(buffer);
        byte[] bytes = new byte[sLength];
        buffer.get(bytes, 0, sLength);
        return new String(bytes, 0, sLength);
    }

    public static int write(ByteBuffer byteBuffer, OutputStream outputStream) throws IOException {
        int len = 0;
        len += write(byteBuffer.capacity(), outputStream);
        byte[] bytes = byteBuffer.array();
        outputStream.write(bytes);
        len += bytes.length;
        return len;
    }
    public static int write(ByteBuffer byteBuffer, ByteBuffer buffer) throws IOException {
        int len = 0;
        len += write(byteBuffer.capacity(), buffer);
        byte[] bytes = byteBuffer.array();
        buffer.put(bytes);
        len += bytes.length;
        return len;
    }


    /**
     * unlike InputStream.read(bytes), this method makes sure that you can read length bytes or reach to the end of the stream.
     * @param inputStream
     * @param length
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream inputStream, int length) throws IOException {
        byte[] bytes=new byte[length];
        int offset=0;
        int len=0;
        while(bytes.length-offset>0 && (len=inputStream.read(bytes,offset, bytes.length-offset))!=-1){
            offset+=len;
        }
        return bytes;
    }

    /**
     * unlike InputStream.read(bytes), this method makes sure that you can read length bytes or reach to the end of the stream.
     * @throws IOException
     */
    public static byte[] readBytesWithSelfDescriptionLength(InputStream inputStream) throws IOException {
        int length=readInt(inputStream);
        return readBytes(inputStream,length);
    }


    public static ByteBuffer readByteBufferWithSelfDescriptionLength(InputStream inputStream) throws IOException {
        byte[] bytes = readBytesWithSelfDescriptionLength(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    public static ByteBuffer readByteBufferWithSelfDescriptionLength(ByteBuffer buffer) throws IOException {
        int byteLength = readInt(buffer);
        byte[] bytes = new byte[byteLength];
        buffer.get(bytes);
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteLength);
        byteBuffer.put(bytes);
        byteBuffer.flip();

        return byteBuffer;
    }


    public static int readAsPossible(FileChannel channel, long position, ByteBuffer buffer) throws IOException{
        int length=0;
        int read;
        while(buffer.hasRemaining()&& (read=channel.read(buffer,position))!=-1){
            length+=read;
            position+=read;
            read=channel.read(buffer,position);
        }
        return length;
    }

    public static int readAsPossible(FileChannel channel, ByteBuffer buffer) throws IOException{
        int length=0;
        int read;
        while(buffer.hasRemaining()&& (read=channel.read(buffer))!=-1){
            length+=read;
            //read=channel.read(buffer);
        }
        return length;
    }

    public static int readAsPossible(FileChannel channel, ByteBuffer buffer, int len) throws IOException{
        int length=0;
        int limit=buffer.limit();
        buffer.limit(buffer.position()+len);
        int read;
        while(length<len && buffer.hasRemaining()&& (read=channel.read(buffer))!=-1){
            length+=read;
            //read=channel.read(buffer);
        }
        buffer.limit(limit);
        return length;
    }

//    public static int write(List list, TSDataType dataType, OutputStream outputStream) throws IOException {
//        int len = 0;
//
//        len += write(list.size(), outputStream);
//        for(Object one : list){
//            switch (dataType){
//                case INT32:
//                    len += write((int)one, outputStream);
//                    break;
//                case TEXT:
//                    len += write((String)one, outputStream);
//                    break;
//                default:
//                    throw new IOException(String.format("Unsupported data type for {}", dataType.toString()));
//            }
//        }
//
//        return len;
//    }
//    public static int write(List list, TSDataType dataType, ByteBuffer buffer) throws IOException {
//        int len = 0;
//
//        if(list == null){
//            len += write(list.size(), buffer);
//            return len;
//        }
//
//        len += write(list.size(), buffer);
//        for(Object one : list){
//            switch (dataType){
//                case INT32:
//                    len += write((int)one, buffer);
//                    break;
//                case TEXT:
//                    len += write((String)one, buffer);
//                    break;
//                default:
//                    throw new IOException(String.format("Unsupported data type for {}", dataType.toString()));
//            }
//        }
//
//        return len;
//    }

//    public static List<Integer> readIntegerList(InputStream inputStream) throws IOException {
//        int size = readInt(inputStream);
//        if(size <= 0)return null;
//
//        List<Integer> list = new ArrayList<>();
//        for(int i = 0;i < size;i++)
//            list.add(readInt(inputStream));
//
//        return list;
//    }
//    public static List<Integer> readIntegerList(ByteBuffer buffer) throws IOException {
//        int size = readInt(buffer);
//        if(size <= 0)return null;
//
//        List<Integer> list = new ArrayList<>();
//        for(int i = 0;i < size;i++)
//            list.add(readInt(buffer));
//        return list;
//    }
//
//    public static List<String> readStringList(InputStream inputStream) throws IOException {
//        List<String> list = new ArrayList<>();
//        int size = readInt(inputStream);
//
//        for(int i = 0;i < size;i++)
//            list.add(readString(inputStream));
//
//        return list;
//    }
//    public static List<String> readStringList(ByteBuffer buffer) throws IOException {
//        int size = readInt(buffer);
//        if(size <= 0)return null;
//
//        List<String> list = new ArrayList<>();
//        for(int i = 0;i < size;i++)
//            list.add(readString(buffer));
//
//        return list;
//    }


    //TODO 下面的代码都没有意义


    public static int write(CompressionType compressionType, OutputStream outputStream) throws IOException {
        short n = compressionType.serialize();
        int len = write(n, outputStream);
        return len;
    }
    public static int write(CompressionType compressionType, ByteBuffer buffer) throws IOException {
        short n = compressionType.serialize();
        int len = write(n, buffer);
        return len;
    }

    public static CompressionType readCompressionType(InputStream inputStream) throws IOException {
        short n = readShort(inputStream);
        return CompressionType.deserialize(n);
    }
    public static CompressionType readCompressionType(ByteBuffer buffer) throws IOException {
        short n = readShort(buffer);
        return CompressionType.deserialize(n);
    }

    public static int write(TSDataType dataType, OutputStream outputStream) throws IOException {
        short n = dataType.serialize();
        int len = write(n, outputStream);
        return len;
    }
    public static int write(TSDataType dataType, ByteBuffer buffer) throws IOException {
        short n = dataType.serialize();
        int len = write(n, buffer);
        return len;
    }

    public static TSDataType readDataType(InputStream inputStream) throws IOException {
        short n = readShort(inputStream);
        return TSDataType.deserialize(n);
    }
    public static TSDataType readDataType(ByteBuffer buffer) throws IOException {
        short n = readShort(buffer);
        return TSDataType.deserialize(n);
    }

    public static int write(TSEncoding encoding, OutputStream outputStream) throws IOException {
        short n = encoding.serialize();
        int len = write(n, outputStream);
        return len;
    }
    public static int write(TSEncoding encoding, ByteBuffer buffer) throws IOException {
        short n = encoding.serialize();
        int len = write(n, buffer);
        return len;
    }

    public static TSEncoding readEncoding(InputStream inputStream) throws IOException {
        short n = readShort(inputStream);
        return TSEncoding.deserialize(n);
    }
    public static TSEncoding readEncoding(ByteBuffer buffer) throws IOException {
        short n = readShort(buffer);
        return TSEncoding.deserialize(n);
    }

    public static int write(TSFreqType freqType, OutputStream outputStream) throws IOException {
        short n = freqType.serialize();
        int len = write(n, outputStream);
        return len;
    }
    public static int write(TSFreqType freqType, ByteBuffer buffer) throws IOException {
        short n = freqType.serialize();
        int len = write(n, buffer);
        return len;
    }

    public static TSFreqType readFreqType(InputStream inputStream) throws IOException {
        short n = readShort(inputStream);
        return TSFreqType.deserialize(n);
    }
    public static TSFreqType readFreqType(ByteBuffer buffer) throws IOException {
        short n = readShort(buffer);
        return TSFreqType.deserialize(n);
    }

    public static int write(TsDigest digest, OutputStream outputStream) throws IOException {
        return digest.serializeTo(outputStream);
    }
    public static int write(TsDigest digest, ByteBuffer buffer) throws IOException {
        return digest.serializeTo(buffer);
    }

    public static TsDigest readDigest(InputStream inputStream) throws IOException {
        TsDigest tsDigest = TsDigest.deserializeFrom(inputStream);
        return tsDigest;
    }
    public static TsDigest readDigest(ByteBuffer buffer) throws IOException {
        TsDigest tsDigest = TsDigest.deserializeFrom(buffer);
        return tsDigest;
    }

    public static int write(TimeSeriesMetadata timeSeriesMetadata, OutputStream outputStream) throws IOException {
        return timeSeriesMetadata.serialize(outputStream);
    }
    public static int write(TimeSeriesMetadata timeSeriesMetadata, ByteBuffer buffer) throws IOException {
        return timeSeriesMetadata.serialize(buffer);
    }

    public static TimeSeriesMetadata readTimeSeriesMetadata(InputStream inputStream) throws IOException {
        TimeSeriesMetadata timeSeriesMetadata = TimeSeriesMetadata.deserialize(inputStream);
        return timeSeriesMetadata;
    }
    public static TimeSeriesMetadata readTimeSeriesMetadata(ByteBuffer buffer) throws IOException {
        TimeSeriesMetadata timeSeriesMetadata = TimeSeriesMetadata.deserialize(buffer);
        return timeSeriesMetadata;
    }

    public static int write(TimeSeriesChunkMetaData timeSeriesChunkMetaData, OutputStream outputStream) throws IOException {
        return timeSeriesChunkMetaData.serializeTo(outputStream);
    }
    public static int write(TimeSeriesChunkMetaData timeSeriesChunkMetaData, ByteBuffer buffer) throws IOException {
        return timeSeriesChunkMetaData.serializeTo(buffer);
    }

    public static TimeSeriesChunkMetaData readTimeSeriesChunkMetaData(InputStream inputStream) throws IOException {
        TimeSeriesChunkMetaData timeSeriesChunkMetaData = TimeSeriesChunkMetaData.deserializeFrom(inputStream);
        return timeSeriesChunkMetaData;
    }
    public static TimeSeriesChunkMetaData readTimeSeriesChunkMetaData(ByteBuffer buffer) throws IOException {
        TimeSeriesChunkMetaData timeSeriesChunkMetaData = TimeSeriesChunkMetaData.deserializeFrom(buffer);
        return timeSeriesChunkMetaData;
    }

    public static int write(RowGroupMetaData rowGroupMetaData, OutputStream outputStream) throws IOException {
        return rowGroupMetaData.serializeTo(outputStream);
    }
    public static int write(RowGroupMetaData rowGroupMetaData, ByteBuffer buffer) throws IOException {
        return rowGroupMetaData.serializeTo(buffer);
    }

    public static RowGroupMetaData readRowGroupMetaData(InputStream inputStream) throws IOException {
        RowGroupMetaData rowGroupMetaData = RowGroupMetaData.deserializeFrom(inputStream);
        return rowGroupMetaData;
    }
    public static RowGroupMetaData readRowGroupMetaData(ByteBuffer buffer) throws IOException {
        RowGroupMetaData rowGroupMetaData = RowGroupMetaData.deserializeFrom(buffer);
        return rowGroupMetaData;
    }

    public static int write(TsDeltaObjectMetadata deltaObjectMetadata, OutputStream outputStream) throws IOException {
        return deltaObjectMetadata.serializeTo(outputStream);
    }
    public static int write(TsDeltaObjectMetadata deltaObjectMetadata, ByteBuffer buffer) throws IOException {
        return deltaObjectMetadata.serializeTo(buffer);
    }






}
