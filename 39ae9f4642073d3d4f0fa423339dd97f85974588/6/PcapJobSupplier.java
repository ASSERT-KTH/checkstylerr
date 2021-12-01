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
package org.apache.metron.rest.config;

import org.apache.hadoop.fs.Path;
import org.apache.metron.job.Finalizer;
import org.apache.metron.job.JobException;
import org.apache.metron.job.RuntimeJobException;
import org.apache.metron.job.Statusable;
import org.apache.metron.pcap.finalizer.PcapFinalizerStrategies;
import org.apache.metron.pcap.finalizer.PcapRestFinalizer;
import org.apache.metron.pcap.mr.PcapJob;
import org.apache.metron.rest.model.pcap.PcapRequest;

import java.util.function.Supplier;

public class PcapJobSupplier implements Supplier<Statusable<Path>> {

  private PcapRequest pcapRequest;

  @Override
  public Statusable<Path> get() {
    try {
      PcapJob<Path> pcapJob = createPcapJob();
      return pcapJob.submit(PcapFinalizerStrategies.REST, pcapRequest);
    } catch (JobException e) {
      throw new RuntimeJobException(e.getMessage());
    }
  }

  public void setPcapRequest(PcapRequest pcapRequest) {
    this.pcapRequest = pcapRequest;
  }

  protected PcapJob createPcapJob() {
    return new PcapJob();
  }

}
