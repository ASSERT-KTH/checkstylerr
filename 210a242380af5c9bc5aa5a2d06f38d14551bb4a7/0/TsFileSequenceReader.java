package cn.edu.tsinghua.tsfile.timeseries.readV2;

import cn.edu.tsinghua.tsfile.common.conf.TSFileConfig;
import cn.edu.tsinghua.tsfile.common.conf.TSFileDescriptor;
import cn.edu.tsinghua.tsfile.common.utils.ByteBufferUtil;
import cn.edu.tsinghua.tsfile.common.utils.ReadWriteIOUtils;
import cn.edu.tsinghua.tsfile.compress.UnCompressor;
import cn.edu.tsinghua.tsfile.encoding.decoder.Decoder;
import cn.edu.tsinghua.tsfile.file.MetaMarker;
import cn.edu.tsinghua.tsfile.file.footer.RowGroupFooter;
import cn.edu.tsinghua.tsfile.file.header.ChunkHeader;
import cn.edu.tsinghua.tsfile.file.header.PageHeader;
import cn.edu.tsinghua.tsfile.file.metadata.RowGroupMetaData;
import cn.edu.tsinghua.tsfile.file.metadata.TsFileMetaData;
import cn.edu.tsinghua.tsfile.file.metadata.enums.CompressionType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSEncoding;
import cn.edu.tsinghua.tsfile.timeseries.readV2.datatype.TimeValuePair;
import cn.edu.tsinghua.tsfile.timeseries.readV2.reader.impl.PageDataReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TsFileSequenceReader {
    private Path path;
    FileChannel channel;
    private long fileMetadataPos;
    private int fileMetadtaSize;
    private ByteBuffer markerBuffer = ByteBuffer.allocate(Byte.BYTES);

    public TsFileSequenceReader(String file) {
        this.path = Paths.get(file);
    }

    /**
     * After open the file, the reader position is at the end of the  magic string in the header.
     *
     * @return
     * @throws IOException
     */
    public void open() throws IOException {
        channel = FileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer metadataSize = ByteBuffer.allocate(Integer.BYTES);
        channel.read(metadataSize, channel.size() - TSFileConfig.MAGIC_STRING.length() - Integer.BYTES);
        metadataSize.flip();
        fileMetadtaSize = ByteBufferUtil.toInt(metadataSize);//read file metadata size and position
        fileMetadataPos = channel.size() - TSFileConfig.MAGIC_STRING.length() - Integer.BYTES - fileMetadtaSize;
        channel.position(TSFileConfig.MAGIC_STRING.length());//skip the magic header
    }

    /**
     * this function does not modify the position of the file reader.
     *
     * @return
     * @throws IOException
     */
    public String readTailMagic() throws IOException {
        long totalSize = channel.size();
        ByteBuffer magicStringBytes = ByteBuffer.allocate(TSFileConfig.MAGIC_STRING.length());
        channel.read(magicStringBytes, totalSize - TSFileConfig.MAGIC_STRING.length());
        magicStringBytes.flip();
        return ByteBufferUtil.string(magicStringBytes);
    }

    /**
     * this function does not modify the position of the file reader.
     *
     * @return
     * @throws IOException
     */
    public String readHeadMagic() throws IOException {
        ByteBuffer magicStringBytes = ByteBuffer.allocate(TSFileConfig.MAGIC_STRING.length());
        channel.read(magicStringBytes, 0);
        magicStringBytes.flip();
        return ByteBufferUtil.string(magicStringBytes);
    }

    /**
     * this function does not modify the position of the file reader.
     *
     * @return
     * @throws IOException
     */
    public TsFileMetaData readFileMetadata() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(fileMetadtaSize);
        ReadWriteIOUtils.readAsPossible(channel, fileMetadataPos, buffer);
        buffer.flip();
        return TsFileMetaData.deserializeFrom(buffer);
    }

    public boolean hasNextRowGroup() throws IOException {
        return channel.position() < fileMetadataPos;
    }

    public RowGroupFooter readRowGroupFooter() throws IOException {
        return RowGroupFooter.deserializeFrom(Channels.newInputStream(channel), true);
    }

    /**
     * After reading the footer of a RowGroup, call this method to set the file pointer to the start of the data of this
     * RowGroup if you want to read its data next.
     *
     * @param footer
     * @throws IOException
     */
    public void prepareReadRowGroup(RowGroupFooter footer) throws IOException {
        channel.position(channel.position() - footer.getDataSize() - footer.getSerializedSize());
    }

    public ChunkHeader readChunkHeader() throws IOException {
        return ChunkHeader.deserializeFrom(Channels.newInputStream(channel), true);
    }

    /**
     * notice, this function will modify channel's position.
     *
     * @param offset the file offset of this chunk's header
     * @return
     * @throws IOException
     */
    public ChunkHeader readChunkHeader(long offset) throws IOException {
        channel.position(offset);
        return ChunkHeader.deserializeFrom(Channels.newInputStream(channel), true);
    }

    /**
     * notice, the position of the channel MUST be at the end of this header.
     *
     * @param header
     * @return the pages of this chunk
     * @throws IOException
     */
    public ByteBuffer readChunk(ChunkHeader header) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(header.getDataSize());
        ReadWriteIOUtils.readAsPossible(channel, buffer);
        buffer.flip();
        return buffer;
    }

    /**
     * notice, this function will modify channel's position.
     *
     * @param position
     * @return
     * @throws IOException
     */
    public ByteBuffer readChunkAndHeader(long position) throws IOException {
        ChunkHeader header = readChunkHeader(position);
        ByteBuffer buffer = ByteBuffer.allocate(header.getSerializedSize() + header.getDataSize());
        header.serializeTo(buffer);
        ReadWriteIOUtils.readAsPossible(channel, buffer);
        buffer.flip();
        return buffer;
    }

    /**
     * notice, the function will midify channel's position.
     *
     * @param position
     * @param length
     * @param output
     * @return
     * @throws IOException
     */
    public int readRaw(long position, int length, ByteBuffer output) throws IOException {
        channel.position(position);
        return ReadWriteIOUtils.readAsPossible(channel, output, length);
    }

    /**
     * notice, this function will modify channel's position.
     *
     * @param header
     * @return the pages of this chunk
     * @throws IOException
     */
    public ByteBuffer readChunk(ChunkHeader header, long positionOfChunkHeader) throws IOException {
        channel.position(positionOfChunkHeader);
        return readChunk(header);
    }

    public PageHeader readPageHeader(TSDataType type) throws IOException {
        return PageHeader.deserializeFrom(Channels.newInputStream(channel), type);
    }

    /**
     * notice, this function will modify channel's position.
     *
     * @param offset the file offset of this page header's header
     * @return
     * @throws IOException
     */
    public PageHeader readPageHeader(TSDataType type, long offset) throws IOException {
        channel.position(offset);
        return PageHeader.deserializeFrom(Channels.newInputStream(channel), type);
    }

    public ByteBuffer readPage(PageHeader header, CompressionType type) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(header.getCompressedSize());
        ReadWriteIOUtils.readAsPossible(channel, buffer);
        buffer.flip();
        UnCompressor unCompressor = UnCompressor.getUnCompressor(type);
        ByteBuffer uncompressedBuffer = ByteBuffer.allocate(header.getUncompressedSize());
        //unCompressor.uncompress(buffer, uncompressedBuffer);
        //uncompressedBuffer.flip();
        switch (type) {
            case UNCOMPRESSED:
                return buffer;
            default:
                unCompressor.uncompress(buffer.array(), buffer.position(), buffer.remaining(), uncompressedBuffer.array(), 0);
                return uncompressedBuffer;
        }

    }

    public byte readMarker() throws IOException {
        markerBuffer.clear();
        ReadWriteIOUtils.readAsPossible(channel, markerBuffer);
        markerBuffer.flip();
        return markerBuffer.get();
    }


    public void close() throws IOException {
        this.channel.close();
    }

    public static void main(String[] args) throws IOException {
        TsFileSequenceReader reader = new TsFileSequenceReader("test.tsfile");
        reader.open();
        System.out.println("position: " + reader.channel.position());
        System.out.println(reader.readHeadMagic());
        System.out.println(reader.readTailMagic());
        TsFileMetaData metaData = reader.readFileMetadata();
        // Sequential reading of one RowGroup now follows this order:
        // first SeriesChunks (headers and data) in one RowGroup, then the RowGroupFooter
        // Because we do not know how many chunks a RowGroup may have, we should read one byte (the marker) ahead and
        // judge accordingly.
        while (reader.hasNextRowGroup()) {
            byte marker = reader.readMarker();
            switch (marker) {
                case MetaMarker.ChunkHeader:
                    ChunkHeader header = reader.readChunkHeader();
                    System.out.println("position: " + reader.channel.position());
                    System.out.println("chunk: " + header.getMeasurementID());
                    Decoder defaultTimeDecoder = Decoder.getDecoderByType(TSEncoding.valueOf(TSFileDescriptor.getInstance().getConfig().timeSeriesEncoder),
                            TSDataType.INT64);
                    Decoder valueDecoder = Decoder.getDecoderByType(header.getEncodingType(), header.getDataType());
                    for (int j = 0; j < header.getNumOfPages(); j++) {
                        PageHeader pageHeader = reader.readPageHeader(header.getDataType());
                        System.out.println("position: " + reader.channel.position());
                        System.out.println("points in the page: " + pageHeader.getNumOfValues());
                        ByteBuffer pageData = reader.readPage(pageHeader, header.getCompressionType());
                        System.out.println("position: " + reader.channel.position());
                        System.out.println("page data size: " + pageHeader.getUncompressedSize() + "," + pageData.remaining());
                        PageDataReader reader1 = new PageDataReader(pageData, header.getDataType(), valueDecoder, defaultTimeDecoder);
                        while (reader1.hasNext()) {
                            TimeValuePair pair = reader1.next();
                            System.out.println("time, value: " + pair.getTimestamp() + "," + pair.getValue());
                        }
                    }
                    break;
                case MetaMarker.RowGroupFooter:
                    RowGroupFooter rowGroupFooter = reader.readRowGroupFooter();
                    System.out.println("position: " + reader.channel.position());
                    System.out.println("row group: " + rowGroupFooter.getDeltaObjectID());
                    break;
                default:
                    MetaMarker.handleUnexpectedMarker(marker);
            }
        }
        reader.close();
    }

}
