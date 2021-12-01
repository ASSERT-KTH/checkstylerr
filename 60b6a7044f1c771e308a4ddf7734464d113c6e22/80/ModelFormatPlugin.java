/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.easy.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.drill.common.exceptions.ExecutionSetupException;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.logical.FormatPluginConfig;
import org.apache.drill.common.logical.StoragePluginConfig;
import org.apache.drill.exec.ExecConstants;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.physical.base.AbstractGroupScan;
import org.apache.drill.exec.physical.base.ScanStats;
import org.apache.drill.exec.physical.base.ScanStats.GroupScanProperty;
import org.apache.drill.exec.planner.physical.PlannerSettings;
import org.apache.drill.exec.proto.ExecProtos.FragmentHandle;
import org.apache.drill.exec.proto.UserBitShared.CoreOperatorType;
import org.apache.drill.exec.server.DrillbitContext;
import org.apache.drill.exec.store.RecordReader;
import org.apache.drill.exec.store.RecordWriter;
import org.apache.drill.exec.store.dfs.DrillFileSystem;
import org.apache.drill.exec.store.dfs.FileSelection;
import org.apache.drill.exec.store.dfs.FileSystemConfig;
import org.apache.drill.exec.store.dfs.easy.EasyFormatPlugin;
import org.apache.drill.exec.store.dfs.easy.EasyGroupScan;
import org.apache.drill.exec.store.dfs.easy.EasyWriter;
import org.apache.drill.exec.store.dfs.easy.FileWork;
import org.apache.drill.exec.store.schedule.CompleteFileWork;
import org.apache.drill.exec.store.model.DrillModelReader;
import org.apache.drill.exec.store.model.DrillModelWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileSplit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ModelFormatPlugin extends EasyFormatPlugin<ModelFormatPlugin.ModelFormatConfig> {
	  private final static String DEFAULT_NAME = "model";


  public ModelFormatPlugin(String name, DrillbitContext context, Configuration fsConf, StoragePluginConfig storageConfig) {
    super(name, context, fsConf, storageConfig, new ModelFormatConfig(), true, false, false, false, Collections.<String>emptyList(), DEFAULT_NAME);
  }

  public ModelFormatPlugin(String name, DrillbitContext context,  Configuration fsConf, StoragePluginConfig config, ModelFormatConfig formatPluginConfig) {
    super(name, context, fsConf, config, formatPluginConfig, true, false, false, false, formatPluginConfig.getExtensions(), DEFAULT_NAME);
  }


  @Override
  public RecordReader getRecordReader(FragmentContext context, DrillFileSystem dfs, FileWork fileWork,
      List<SchemaPath> columns) throws ExecutionSetupException {
//    Path path = dfs.makeQualified(new Path(fileWork.getPath()));
//    FileSplit split = new FileSplit(path, fileWork.getStart(), fileWork.getLength(), new String[]{""});
//    Preconditions.checkArgument(((ModelFormatConfig)formatConfig).getDelimiter().length() == 1, "Only single character delimiter supported");
//    return new DrillModelReader(split, context, ((ModelFormatConfig) formatConfig).getDelimiter().charAt(0), columns);
//    return new DrillModelReader(split, context, columns);
    return new DrillModelReader(context, fileWork.getPath(), dfs, columns);
  }

 
  
  @Override
  public AbstractGroupScan getGroupScan(String userName, FileSelection selection, List<SchemaPath> columns) throws IOException {
    return new EasyGroupScan(userName, selection, this, columns, selection.selectionRoot); //TODO : textformat supports project?
  }
  
  @Override
  protected ScanStats getScanStats(final PlannerSettings settings, final EasyGroupScan scan) {
    long data = 0;
    for (final CompleteFileWork work : scan.getWorkIterable()) {
      data += work.getTotalBytes();
    }
    final double estimatedRowSize = settings.getOptions().getOption(ExecConstants.TEXT_ESTIMATED_ROW_SIZE);
    final double estRowCount = data / estimatedRowSize;
    return new ScanStats(GroupScanProperty.NO_EXACT_ROW_COUNT, (long) estRowCount, 1, data);
  }

  @Override
  public RecordWriter getRecordWriter(FragmentContext context, EasyWriter writer) throws IOException {
    Map<String, String> options = Maps.newHashMap();

    options.put("location", writer.getLocation());

    FragmentHandle handle = context.getHandle();
    String fragmentId = String.format("%d_%d", handle.getMajorFragmentId(), handle.getMinorFragmentId());
    options.put("prefix", fragmentId);

    options.put("separator", ((ModelFormatConfig)getConfig()).getDelimiter());
    options.put(FileSystem.FS_DEFAULT_NAME_KEY, ((FileSystemConfig)writer.getStorageConfig()).connection);

    options.put("extension", ((ModelFormatConfig)getConfig()).getExtensions().get(0));

    RecordWriter recordWriter = new DrillModelWriter(/*context.getAllocator()*/);
    recordWriter.init(options);

    return recordWriter;
  }

  @JsonTypeName("model") 
  public static class ModelFormatConfig implements FormatPluginConfig {

    public List<String> extensions;
    public String delimiter = "";
//    public String lineDelimiter = "";
//    public char fieldDelimiter = "";
//    public char quote = '"';
//    public char escape = '"';
//    public char comment = '#';
//    public boolean skipFirstLine = false;


    public List<String> getExtensions() {
      return extensions;
    }

    public String getDelimiter() {
      return delimiter;
    }

    @Override
    public int hashCode() {
      return -1;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj == null) {
        return false;
      } else if (!(obj instanceof ModelFormatConfig)) {
        return false;
      }

      ModelFormatConfig that = (ModelFormatConfig) obj;
      if (this.delimiter.equals(that.delimiter)) {
        return true;
      }
      return false;
    }

  }

  @Override
  public int getReaderOperatorType() {
    return CoreOperatorType.MODEL_SCAN_VALUE;
  }

  @Override
  public int getWriterOperatorType() {
    return CoreOperatorType.MODEL_WRITER_VALUE;
  }

  @Override
  public boolean supportsPushDown() {
    return true;
  }

}
