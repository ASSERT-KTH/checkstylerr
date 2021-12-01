package cn.edu.tsinghua.tsfile.file.metadata;

import cn.edu.tsinghua.tsfile.common.utils.ReadWriteIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * For more information, see TimeSeriesChunkMetaData in cn.edu.thu.tsfile.format package
 */
public class TimeSeriesChunkMetaData {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesChunkMetaData.class);

    private String measurementUID;

    /**
     * Byte offset of the corresponding data in the file
     * Notice:  include the chunk header
     */
    private long fileOffsetOfCorrespondingData;

    private long numOfPoints;

    private long startTime;

    private long endTime;


    /**
     * The maximum time of the tombstones that take effect on this chunk. Only data with larger timestamps than this
     * should be exposed to user.
     */
    private long maxTombstoneTime;

    private TsDigest valuesStatistics;//TODO 谁赋值的？？

    //private TSEncoding dataEncoding;//FIXME put me to TimeSeriesMetaData

    public int getSerializedSize(){
        //6 * Long.BYTES: fileOffsetOfCorrespondingData, tsDigestOffset, numOfPoints, totalByteSizeOfPagesOnDisk, startTime, endTime
        return (Integer.BYTES + measurementUID.length()) + 5 * Long.BYTES  + (valuesStatistics==null? TsDigest.getNullDigestSize():valuesStatistics.getSerializedSize());

    }


    private TimeSeriesChunkMetaData(){}

    public TimeSeriesChunkMetaData(String measurementUID, long fileOffset,  long startTime, long endTime) {
        this.measurementUID = measurementUID;
        this.fileOffsetOfCorrespondingData = fileOffset;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("numPoints %d", numOfPoints);
    }

    public long getNumOfPoints() {
        return numOfPoints;
    }

    public void setNumOfPoints(long numRows) {
        this.numOfPoints = numRows;
    }

    /**
     * @return Byte offset of the corresponding data in the file (not include the chunk header)
     */
    public long getFileOffsetOfCorrespondingData() {
        return fileOffsetOfCorrespondingData;
    }

    public String getMeasurementUID() {
        return measurementUID;
    }

    public TsDigest getDigest() {
        return valuesStatistics;
    }

    public void setDigest(TsDigest digest) {
        this.valuesStatistics = digest;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    public int serializeTo(OutputStream outputStream) throws IOException {
        int byteLen = 0;

        byteLen += ReadWriteIOUtils.write(measurementUID, outputStream);

        byteLen += ReadWriteIOUtils.write(fileOffsetOfCorrespondingData, outputStream);


        byteLen += ReadWriteIOUtils.write(numOfPoints, outputStream);
        byteLen += ReadWriteIOUtils.write(startTime, outputStream);
        byteLen += ReadWriteIOUtils.write(endTime, outputStream);

        if(valuesStatistics==null) byteLen += TsDigest.serializeNullTo(outputStream);
        else byteLen += ReadWriteIOUtils.write(valuesStatistics, outputStream);

        assert  byteLen == getSerializedSize();
        return byteLen;
    }

    public int serializeTo(ByteBuffer buffer) throws IOException {
        int byteLen = 0;

        byteLen += ReadWriteIOUtils.write(measurementUID, buffer);

        byteLen += ReadWriteIOUtils.write(fileOffsetOfCorrespondingData, buffer);


        byteLen += ReadWriteIOUtils.write(numOfPoints, buffer);
        byteLen += ReadWriteIOUtils.write(startTime, buffer);
        byteLen += ReadWriteIOUtils.write(endTime, buffer);


        if(valuesStatistics==null) byteLen += TsDigest.serializeNullTo(buffer);
        else byteLen += ReadWriteIOUtils.write(valuesStatistics, buffer);

        assert  byteLen == getSerializedSize();
        return byteLen;
    }

    public static TimeSeriesChunkMetaData deserializeFrom(InputStream inputStream) throws IOException {
        TimeSeriesChunkMetaData timeSeriesChunkMetaData = new TimeSeriesChunkMetaData();

        timeSeriesChunkMetaData.measurementUID = ReadWriteIOUtils.readString(inputStream);

        timeSeriesChunkMetaData.fileOffsetOfCorrespondingData = ReadWriteIOUtils.readLong(inputStream);


        timeSeriesChunkMetaData.numOfPoints = ReadWriteIOUtils.readLong(inputStream);
        timeSeriesChunkMetaData.startTime = ReadWriteIOUtils.readLong(inputStream);
        timeSeriesChunkMetaData.endTime = ReadWriteIOUtils.readLong(inputStream);


        timeSeriesChunkMetaData.valuesStatistics = ReadWriteIOUtils.readDigest(inputStream);


        return timeSeriesChunkMetaData;
    }

    public static TimeSeriesChunkMetaData deserializeFrom(ByteBuffer buffer) throws IOException {
        TimeSeriesChunkMetaData timeSeriesChunkMetaData = new TimeSeriesChunkMetaData();

        timeSeriesChunkMetaData.measurementUID = ReadWriteIOUtils.readString(buffer);

        timeSeriesChunkMetaData.fileOffsetOfCorrespondingData = ReadWriteIOUtils.readLong(buffer);

        timeSeriesChunkMetaData.numOfPoints = ReadWriteIOUtils.readLong(buffer);
        timeSeriesChunkMetaData.startTime = ReadWriteIOUtils.readLong(buffer);
        timeSeriesChunkMetaData.endTime = ReadWriteIOUtils.readLong(buffer);

        timeSeriesChunkMetaData.valuesStatistics = ReadWriteIOUtils.readDigest(buffer);


        return timeSeriesChunkMetaData;
    }

    public long getMaxTombstoneTime() {
        return maxTombstoneTime;
    }

    public void setMaxTombstoneTime(long maxTombstoneTime) {
        this.maxTombstoneTime = maxTombstoneTime;
    }

}
