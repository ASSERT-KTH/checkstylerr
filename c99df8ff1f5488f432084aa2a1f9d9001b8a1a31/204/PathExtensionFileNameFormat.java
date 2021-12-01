/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.writer.hdfs;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;

import java.util.Map;

public class PathExtensionFileNameFormat implements FileNameFormat {
  FileNameFormat delegate;
  String pathExtension;
  public PathExtensionFileNameFormat(String pathExtension, FileNameFormat delegate) {
    this.delegate = delegate;
    this.pathExtension = pathExtension;
  }

  @Override
  public void prepare(Map map, TopologyContext topologyContext) {
    this.delegate.prepare(map, topologyContext);
  }

  @Override
  public String getName(long rotation, long l1) {
    return delegate.getName(rotation, l1);
  }

  @Override
  public String getPath() {
    return delegate.getPath() + "/" + pathExtension;
  }
}
