package cn.edu.tsinghua.tsfile.timeseries.write.schema.converter;

import cn.edu.tsinghua.tsfile.common.conf.TSFileDescriptor;
import cn.edu.tsinghua.tsfile.common.constant.JsonFormatConstant;
import cn.edu.tsinghua.tsfile.encoding.encoder.TSEncodingBuilder;
import cn.edu.tsinghua.tsfile.file.metadata.enums.CompressionType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSDataType;
import cn.edu.tsinghua.tsfile.file.metadata.enums.TSEncoding;
import cn.edu.tsinghua.tsfile.timeseries.write.desc.MeasurementDescriptor;
import cn.edu.tsinghua.tsfile.timeseries.write.exception.InvalidJsonSchemaException;
import cn.edu.tsinghua.tsfile.timeseries.write.schema.FileSchema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * JsonConverter is used to convert JsonObject to TSFile Schema which is a java class defined in tsfile
 * project. the main function of this converter is to receive a json object of schema and register
 * all measurements.
 * </p>
 * <p>
 * The format of JSON schema is as follow:
 *
 * <pre>
 *  {
 *     "schema": [
 *        {
 *          "measurement_id": "s1",
 *          "data_type": "INT32",
 *          "encoding": "RLE",
 *          "compressor": "SNAPPY"
 *         },
 *         {
 *             "measurement_id": "s3",
 *             "data_type": "ENUMS",
 *             "encoding": "BITMAP",
 *             "compressor": "SNAPPY",
 *             "enum_values":["MAN","WOMAN"],
 *             "max_error":12,
 *             "max_point_number":3
 *         },
 *         ...
 *     ]
 * }
 *
 * </pre>
 *
 * @author kangrong
 * @see TSEncodingBuilder TSEncodingBuilder
 */
public class JsonConverter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConverter.class);

    /**
     * input a FileSchema and a jsonObject to be converted,
     *
     * @param jsonSchema the whole schema in type of JSONObject
     * @return converted measurement descriptors
     * @throws InvalidJsonSchemaException throw exception when json schema is not valid
     */

    public static Map<String, MeasurementDescriptor> converterJsonToMeasurementDescriptors(
            JSONObject jsonSchema) throws InvalidJsonSchemaException {
        Map<String, MeasurementDescriptor> result = new HashMap<>();
        if (!jsonSchema.has(JsonFormatConstant.JSON_SCHEMA))
            throw new InvalidJsonSchemaException("missing fields:" + JsonFormatConstant.JSON_SCHEMA);

        /**
         * get schema of all measurements in JSONArray from JSONObject
         *
         * "schema": [
         *         {
         *             "measurement_id": "s1",
         *             "data_type": "INT32",
         *             "encoding": "RLE"
         *         },
         *         {
         *             "measurement_id": "s2",
         *             "data_type": "INT64",
         *             "encoding": "TS_2DIFF"
         *         }...
         *  ]
         */
        JSONArray schemaArray = jsonSchema.getJSONArray(JsonFormatConstant.JSON_SCHEMA);
        for (int i = 0; i < schemaArray.length(); i++) {
            MeasurementDescriptor mDescriptor = convertJsonToMeasureMentDescriptor(
                    schemaArray.getJSONObject(i));
            result.put(mDescriptor.getMeasurementId(), mDescriptor);
        }
        return result;
    }


    /**
     * convert the input JSONObject to MeasurementDescriptor
     * @param measurementObj properties of one measurement
     *
     *  an example:
     *
     *  {
     *             "measurement_id": "s3",
     *             "data_type": "ENUMS",
     *             "encoding": "BITMAP",
     *
     *             // some measurement may have some properties
     *
     *             "compressor": "SNAPPY",
     *             "enum_values":["MAN","WOMAN"],
     *             "max_error":12,
     *             "max_point_number":3
     *  }
     *
     * @return converted MeasurementDescriptor
     */
    public static MeasurementDescriptor convertJsonToMeasureMentDescriptor(
            JSONObject measurementObj) {
        if (!measurementObj.has(JsonFormatConstant.MEASUREMENT_UID)
                && !measurementObj.has(JsonFormatConstant.DATA_TYPE)
                && !measurementObj.has(JsonFormatConstant.MEASUREMENT_ENCODING)
                && !measurementObj.has(JsonFormatConstant.COMPRESS_TYPE)) {
            LOG.warn(
                    "The format of given json is error. Give up to register this measurement. Given json:{}",
                    measurementObj);
            return null;
        }
        // get measurementID
        String measurementId = measurementObj.getString(JsonFormatConstant.MEASUREMENT_UID);
        // get data type information
        TSDataType type = TSDataType.valueOf(measurementObj.getString(JsonFormatConstant.DATA_TYPE));
        // get encoding information
        TSEncoding encoding = TSEncoding
                .valueOf(measurementObj.getString(JsonFormatConstant.MEASUREMENT_ENCODING));
        CompressionType compressionType = measurementObj.has(JsonFormatConstant.COMPRESS_TYPE) ?
                CompressionType.valueOf(measurementObj.getString(JsonFormatConstant.COMPRESS_TYPE)) :
                CompressionType.valueOf(TSFileDescriptor.getInstance().getConfig().compressor);
        // all information of one series
        Map<String, String> props = new HashMap<>();
        for (Object key : measurementObj.keySet()) {
            String value = measurementObj.get(key.toString()).toString();
            props.put(key.toString(), value);
        }
        return new MeasurementDescriptor(measurementId, type, encoding, compressionType, props);
    }

    public static long convertJsonToRowGroupSize(JSONObject jsonSchema) {
        if (jsonSchema.has(JsonFormatConstant.ROW_GROUP_SIZE)) {
            return jsonSchema.getLong(JsonFormatConstant.ROW_GROUP_SIZE);
        }
        return 128 * 1024 * 1024;
    }

    /**
     * given a FileSchema and convert it into a JSONObject
     *
     * @param fileSchema the given schema in type of {@linkplain FileSchema FileSchema}
     * @return converted File Schema in type of JSONObject
     */
    public static JSONObject converterFileSchemaToJson(
            FileSchema fileSchema) {
        /** JSONObject form of FileSchema **/
        JSONObject ret = new JSONObject();
        /** JSONObject form of all MeasurementDescriptors in fileSchema **/
        JSONArray jsonSchema = new JSONArray();

        for (MeasurementDescriptor measurementDescriptor : fileSchema.getDescriptor().values()) {
            jsonSchema.put(convertMeasurementDescriptorToJson(measurementDescriptor));
        }

        ret.put(JsonFormatConstant.JSON_SCHEMA, jsonSchema);
        return ret;
    }

    /**
     * given a MeasurementDescriptor and convert it to a JSONObject
     * @param measurementDescriptor the given descriptor in type of {@linkplain MeasurementDescriptor MeasurementDescriptor}
     * @return converted MeasurementDescriptor in form of JSONObject
     *
     *  an example:
     *
     *  {
     *             "measurement_id": "s3",
     *             "data_type": "ENUMS",
     *             "encoding": "BITMAP",
     *
     *             // some measurement may have some properties
     *
     *             "compressor": "SNAPPY",
     *             "enum_values":["MAN","WOMAN"],
     *             "max_error":12,
     *             "max_point_number":3
     *  }
     */
    private static JSONObject convertMeasurementDescriptorToJson(
            MeasurementDescriptor measurementDescriptor) {
        JSONObject measurementObj = new JSONObject();
        // put measurementID, data type, encoding info and properties into result JSONObject
        measurementObj.put(JsonFormatConstant.MEASUREMENT_UID, measurementDescriptor.getMeasurementId());
        measurementObj.put(JsonFormatConstant.DATA_TYPE, measurementDescriptor.getType().toString());
        measurementObj.put(JsonFormatConstant.MEASUREMENT_ENCODING, measurementDescriptor.getEncodingType().toString());
        measurementDescriptor.getProps().forEach(measurementObj::put);
        return measurementObj;
    }

}