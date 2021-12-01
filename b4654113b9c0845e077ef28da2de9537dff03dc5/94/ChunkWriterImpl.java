package cn.edu.tsinghua.tsfile.timeseries.write.page;

import cn.edu.tsinghua.tsfile.common.utils.PublicBAOS;
import cn.edu.tsinghua.tsfile.compress.Compressor;
import cn.edu.tsinghua.tsfile.file.header.PageHeader;
import cn.edu.tsinghua.tsfile.file.metadata.enums.CompressionType;
import cn.edu.tsinghua.tsfile.file.metadata.statistics.Statistics;
import cn.edu.tsinghua.tsfile.timeseries.write.desc.MeasurementDescriptor;
import cn.edu.tsinghua.tsfile.timeseries.write.exception.PageException;
import cn.edu.tsinghua.tsfile.timeseries.write.io.TsFileIOWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * a implementation of {@linkplain IChunkWriter IChunkWriter}
 *
 * @author kangrong
 * @see IChunkWriter IChunkWriter
 */
public class ChunkWriterImpl implements IChunkWriter {
    private static Logger LOG = LoggerFactory.getLogger(ChunkWriterImpl.class);
    private final Compressor compressor;
    private final MeasurementDescriptor desc;

    /**
     * all pages of this column
     */
    private PublicBAOS pageBuffer;

    private long totalValueCount;
    private long maxTimestamp;
    private long minTimestamp = -1;
    private ByteBuffer compressedData;//DirectByteBuffer

    public ChunkWriterImpl(MeasurementDescriptor desc) {
        this.desc = desc;
        this.compressor = desc.getCompressor();
        this.pageBuffer = new PublicBAOS();
    }


    /**
     * write the page header and data into the PageWriter's outputstream
     * @param data the data of the page
     * @param valueCount    - the amount of values in that page
     * @param statistics    - the statistics for that page
     * @param maxTimestamp  - timestamp maximum in given data
     * @param minTimestamp  - timestamp minimum in given data
     * @return  byte size of the page header and uncompressed data in the page body.
     * @throws PageException
     */
    @Override
    public int writePageHeaderAndDataIntoBuff(ByteBuffer data, int valueCount, Statistics<?> statistics,
                                              long maxTimestamp, long minTimestamp) throws PageException {
        // 1. update time statistics
        if (this.minTimestamp == -1)
            this.minTimestamp = minTimestamp;
        if(this.minTimestamp==-1){
        	LOG.error("Write page error, {}, minTime:{}, maxTime:{}",desc,minTimestamp,maxTimestamp);
        }
        this.maxTimestamp = maxTimestamp;
        int uncompressedSize = data.remaining();
        int maxSize=compressor.getMaxBytesForCompression(uncompressedSize);
        int compressedSize=0;
        int compressedPosition=0;
        byte[] compressedBytes = null;

        if(compressor.getCodecName().equals(CompressionType.UNCOMPRESSED)) {
            compressedSize=data.remaining();
        }else{
            if (data.isDirect()) {
                if (compressedData == null || compressedData.remaining() < maxSize) {
                    if (compressedData != null) {
                        ((DirectBuffer) compressedData).cleaner().clean();
                    }
                    compressedData = ByteBuffer.allocateDirect(maxSize);
                }
                try {
                    compressedSize = compressor.compress(data, compressedData);
                } catch (IOException e) {
                    throw new PageException(
                            "Error when writing a page, " + e.getMessage());
                }
            } else {
                if (compressedBytes == null || compressedBytes.length < compressor.getMaxBytesForCompression(uncompressedSize)) {
                    compressedBytes = new byte[compressor.getMaxBytesForCompression(uncompressedSize)];
                }
                try {
                    compressedPosition = 0;
                    compressedSize = compressor.compress(data.array(), data.position(), data.remaining(), compressedBytes);
                } catch (IOException e) {
                    throw new PageException(
                            "Error when writing a page, " + e.getMessage());
                }
            }
        }

        int headerSize=0;

        // write the page header to IOWriter
        try {
            PageHeader header=new PageHeader(uncompressedSize, compressedSize, valueCount, statistics, maxTimestamp, minTimestamp);
            headerSize=header.getSerializedSize();
            LOG.debug("start to flush a page header into buffer, buffer position {} ", pageBuffer.size());
            header.serializeTo(pageBuffer);
            LOG.debug("finish to flush a page header {} of {} into buffer, buffer position {} ", header, desc.getMeasurementId(), pageBuffer.size());

        } catch (IOException e) {
            resetTimeStamp();
            throw new PageException(
                    "IO Exception in writeDataPageHeader,ignore this page,error message:" + e.getMessage());
        }

        // update data point num
        this.totalValueCount += valueCount;

        // write page content to temp PBAOS
        try {
            LOG.debug("start to flush a page data into buffer, buffer position {} ", pageBuffer.size());
            if(compressor.getCodecName().equals(CompressionType.UNCOMPRESSED)){
                WritableByteChannel channel = Channels.newChannel(pageBuffer);
                channel.write(data);
            }else {
                if (data.isDirect()) {
                    WritableByteChannel channel = Channels.newChannel(pageBuffer);
                    channel.write(compressedData);
                } else {
                    pageBuffer.write(compressedBytes, compressedPosition, compressedSize);
                }
            }
            LOG.debug("start to flush a page data into buffer, buffer position {} ", pageBuffer.size());
        } catch (IOException e) {
            throw new PageException("meet IO Exception in buffer append,but we cannot understand it:" + e.getMessage());
        }
        return headerSize+uncompressedSize;
    }

    private void resetTimeStamp() {
        if (totalValueCount == 0)
            minTimestamp = -1;
    }


    @Override
    public long writeAllPagesOfSeriesToTsFile(TsFileIOWriter writer, Statistics<?> statistics, int numOfPages) throws IOException {
    	if(minTimestamp==-1){
    		LOG.error("Write page error, {}, minTime:{}, maxTime:{}",desc,minTimestamp,maxTimestamp);
    	}

    	// start to write this column chunk
        int headerSize=writer.startFlushChunk(desc, compressor.getCodecName(), desc.getType(), desc.getEncodingType(),statistics, maxTimestamp, minTimestamp, pageBuffer.size(), numOfPages);

        long totalByteSize = writer.getPos();
        LOG.debug("start writing pages of {} into file, position {}", desc.getMeasurementId(), writer.getPos());

        // write all pages of this column
        writer.writeBytesToStream(pageBuffer);
        LOG.debug("finish writing pages of {} into file, position {}", desc.getMeasurementId(), writer.getPos());

        long size = writer.getPos() - totalByteSize;
        assert  size == pageBuffer.size();

        writer.endChunk(size + headerSize, totalValueCount);
        return headerSize + size;
    }

    @Override
    public void reset() {
        minTimestamp = -1;
        pageBuffer.reset();
        totalValueCount = 0;
    }

    @Override
    public long estimateMaxPageMemSize() {
        // return size of buffer + page max size;
        return pageBuffer.size() + estimateMaxPageHeaderSize();
    }

    private int estimateMaxPageHeaderSize() {
        int digestSize = (totalValueCount == 0) ? 0 : desc.getTypeLength() * 2;
        return PageHeader.calculatePageHeaderSize(desc.getType());
    }

    @Override
    public long getCurrentDataSize(){
        return pageBuffer.size();
    }

}
