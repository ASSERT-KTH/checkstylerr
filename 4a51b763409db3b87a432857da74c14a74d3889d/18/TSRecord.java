package cn.edu.tsinghua.tsfile.timeseries.write.record;

import cn.edu.tsinghua.tsfile.timeseries.utils.StringContainer;
import cn.edu.tsinghua.tsfile.timeseries.write.record.datapoint.BooleanDataPoint;
import cn.edu.tsinghua.tsfile.timeseries.write.record.datapoint.FloatDataPoint;
import cn.edu.tsinghua.tsfile.timeseries.write.record.datapoint.IntDataPoint;
import cn.edu.tsinghua.tsfile.timeseries.write.record.datapoint.StringDataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * TSRecord is a kind of format that TsFile receives.TSRecord contains timestamp, deltaObjectId and
 * a list of data points.
 *
 * @author kangrong
 */
public class TSRecord {
    /** timestamp of this TSRecord **/
    public long time;
    /** deltaObjectId of this TSRecord **/
    public String deltaObjectId;
    /** all value of this TSRecord **/
    public List<DataPoint> dataPointList = new ArrayList<>();

    /**
     * constructor of TSRecord
     * @param timestamp timestamp of this TSRecord
     * @param deltaObjectId deltaObjectId of this TSRecord
     */
    public TSRecord(long timestamp, String deltaObjectId) {
        this.time = timestamp;
        this.deltaObjectId = deltaObjectId;
    }

    public void setTime(long timestamp) {
        this.time = timestamp;
    }

    /**
     * add one data point to this TSRecord
     * @param tuple data point to be added
     */
    public void addTuple(DataPoint tuple) {
        this.dataPointList.add(tuple);
    }

    /**
     * output this TSRecord in String format.For example:
     * {delta object id: d1 time: 123456 ,data:[
     *      {measurement id: s1 type: INT32 value: 1 }
     *      {measurement id: s2 type: FLOAT value: 11.11 }
     *      {measurement id: s3 type: BOOLEAN value: true }
     * ]}
     * @return the String format of this TSRecord
     */
    public String toString() {
        StringContainer sc = new StringContainer(" ");
        sc.addTail("{delta object id:", deltaObjectId, "time:", time, ",data:[");
        for (DataPoint tuple : dataPointList) {
            sc.addTail(tuple);
        }
        sc.addTail("]}");
        return sc.toString();
    }
}
