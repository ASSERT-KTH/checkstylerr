package cn.edu.tsinghua.tsfile.timeseries.readV2.controller;

import cn.edu.tsinghua.tsfile.file.metadata.TsFileMetaData;
import cn.edu.tsinghua.tsfile.timeseries.readV2.common.EncodedSeriesChunkDescriptor;
import cn.edu.tsinghua.tsfile.timeseries.readV2.common.Path;

import java.io.IOException;
import java.util.List;

/**
 * Created by zhangjinrui on 2017/12/25.
 */
public interface MetadataQuerier {

    List<EncodedSeriesChunkDescriptor> getSeriesChunkDescriptorList(Path path) throws IOException;
    public TsFileMetaData getWholeFileMetadata();

}
