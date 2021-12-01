package cn.edu.tsinghua.tsfile.timeseries.write.schema;

import cn.edu.tsinghua.tsfile.file.metadata.enums.CompressionType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSEncoding;
import cn.edu.tsinghua.tsfile.timeseries.write.desc.MeasurementDescriptor;

import java.util.Map;

/**
 * This class is used to build FileSchema of tsfile
 *
 * @author qiaojialin
 */
public class SchemaBuilder {
    /** the FileSchema which is being built **/
    private FileSchema fileSchema;

    /**
     * init schema by default value
     */
    public SchemaBuilder() {
        fileSchema = new FileSchema();
    }

    /**
     * add one series to TsFile schema
     *
     * @param measurementId (not null) id of the series
     * @param dataType      (not null) series data type
     * @param tsEncoding    (not null) encoding method you specified
     * @param props         information in encoding method.
     *                      For RLE, Encoder.MAX_POINT_NUMBER
     *                      For PLAIN, Encoder.MAX_STRING_LENGTH
     * @return this
     */
    public SchemaBuilder addSeries(String measurementId, TSDataType dataType, TSEncoding tsEncoding, CompressionType type,
                                   Map<String, String> props) {
        MeasurementDescriptor md = new MeasurementDescriptor(measurementId, dataType, tsEncoding, type, props);
        fileSchema.registerMeasurement(md);
        return this;
    }

    /**
     * add one series to tsfile schema
     *
     * @param measurementId (not null) id of the series
     * @param dataType      (not null) series data type
     * @param tsEncoding    (not null) encoding method you specified
     * @return this
     */
    public SchemaBuilder addSeries(String measurementId, TSDataType dataType, TSEncoding tsEncoding ) {
        MeasurementDescriptor md = new MeasurementDescriptor(measurementId, dataType, tsEncoding);
        fileSchema.registerMeasurement(md);
        return this;
    }

    /**
     * MeasurementDescriptor is the schema of one series
     *
     * @param descriptor series schema
     * @return schema builder
     */
    public SchemaBuilder addSeries(MeasurementDescriptor descriptor) {
        fileSchema.registerMeasurement(descriptor);
        return this;
    }

    /**
     * get file schema after adding all series and properties
     *
     * @return constructed file schema
     */
    public FileSchema build() {
        return this.fileSchema;
    }
}
