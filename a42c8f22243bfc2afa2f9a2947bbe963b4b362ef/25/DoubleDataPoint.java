package cn.edu.tsinghua.tsfile.timeseries.write.record.datapoint;

import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.timeseries.write.series.IChunkWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * a subclass for Double data type extends DataPoint
 *
 * @author kangrong
 * @see DataPoint DataPoint
 */
public class DoubleDataPoint extends DataPoint {
    private static final Logger LOG = LoggerFactory.getLogger(DoubleDataPoint.class);
    /** actual value **/
    private double value;

    /**
     * constructor of DoubleDataPoint, the value type will be set automatically
     * @param measurementId measurement id
     * @param v value
     */
    public DoubleDataPoint(String measurementId, double v) {
        super(TSDataType.DOUBLE, measurementId);
        this.value = v;
    }

    @Override
    public void writeTo(long time, IChunkWriter writer) throws IOException {
        if (writer == null) {
            LOG.warn("given IChunkWriter is null, do nothing and return");
            return;
        }
        writer.write(time, value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setDouble(double value) {
        this.value = value;
    }
}
