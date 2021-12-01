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
package org.apache.ratis.server.storage;

import org.apache.ratis.proto.RaftProtos.LogEntryProto;
import org.apache.ratis.server.impl.RaftConfiguration;
import org.apache.ratis.server.impl.RaftServerConstants;
import org.apache.ratis.server.impl.ServerProtoUtils;
import org.apache.ratis.server.storage.RaftStorageDirectory.StorageState;
import org.apache.ratis.statemachine.SnapshotInfo;
import org.apache.ratis.statemachine.StateMachineStorage;
import org.apache.ratis.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class RaftStorage implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(RaftStorage.class);

  // TODO support multiple storage directories
  private final RaftStorageDirectory storageDir;
  private final StorageState state;
  private volatile MetaFile metaFile;
  private StateMachineStorage stateMachineStorage;

  public RaftStorage(File dir, RaftServerConstants.StartupOption option)
      throws IOException {
    storageDir = new RaftStorageDirectory(dir);
    if (option == RaftServerConstants.StartupOption.FORMAT) {
      if (storageDir.analyzeStorage(false) == StorageState.NON_EXISTENT) {
        throw new IOException("Cannot format " + storageDir);
      }
      storageDir.lock();
      format();
      state = storageDir.analyzeStorage(false);
      Preconditions.assertTrue(state == StorageState.NORMAL);
    } else {
      state = analyzeAndRecoverStorage(true); // metaFile is initialized here
      if (state != StorageState.NORMAL) {
        storageDir.unlock();
        throw new IOException("Cannot load " + storageDir
            + ". Its state: " + state);
      }
    }
  }

  StorageState getState() {
    return state;
  }

  private void format() throws IOException {
    storageDir.clearDirectory();
    metaFile = writeMetaFile(MetaFile.DEFAULT_TERM, MetaFile.EMPTY_VOTEFOR);
    LOG.info("Storage directory " + storageDir.getRoot()
        + " has been successfully formatted.");
  }

  private MetaFile writeMetaFile(long term, String votedFor) throws IOException {
    MetaFile metaFile = new MetaFile(storageDir.getMetaFile());
    metaFile.set(term, votedFor);
    return metaFile;
  }

  private void cleanMetaTmpFile() throws IOException {
    Files.deleteIfExists(storageDir.getMetaTmpFile().toPath());
  }

  private StorageState analyzeAndRecoverStorage(boolean toLock)
      throws IOException {
    StorageState storageState = storageDir.analyzeStorage(toLock);
    if (storageState == StorageState.NORMAL) {
      metaFile = new MetaFile(storageDir.getMetaFile());
      Preconditions.assertTrue(metaFile.exists(),
          () -> "Meta file " + metaFile + " does not exists.");
      metaFile.readFile();
      // Existence of raft-meta.tmp means the change of votedFor/term has not
      // been committed. Thus we should delete the tmp file.
      cleanMetaTmpFile();
      return StorageState.NORMAL;
    } else if (storageState == StorageState.NOT_FORMATTED &&
        storageDir.isCurrentEmpty()) {
      format();
      return StorageState.NORMAL;
    } else {
      return storageState;
    }
  }

  public RaftStorageDirectory getStorageDir() {
    return storageDir;
  }

  @Override
  public void close() throws IOException {
    storageDir.unlock();
  }

  MetaFile getMetaFile() {
    return metaFile;
  }

  public void writeRaftConfiguration(LogEntryProto conf) {
    File confFile = storageDir.getMetaConfFile();
    try (FileOutputStream fio = new FileOutputStream(confFile)) {
      conf.writeTo(fio);
    } catch (Exception e) {
      LOG.error("Failed writing configuration to file:" + confFile, e);
    }
  }

  public RaftConfiguration readRaftConfiguration() {
    File confFile = storageDir.getMetaConfFile();
    try (FileInputStream fio = new FileInputStream(confFile)) {
      LogEntryProto confProto = LogEntryProto.newBuilder().mergeFrom(fio).build();
      return ServerProtoUtils.toRaftConfiguration(confProto);
    } catch (Exception e) {
      LOG.error("Failed reading configuration from file:" + confFile, e);
      return null;
    }
  }

  public SnapshotInfo getLatestSnapshot() throws IOException {
    return getStateMachineStorage().getLatestSnapshot();
  }

  /**
   * Called by the state machine after it has initialized the StateMachineStorage.
   */
  public void setStateMachineStorage(StateMachineStorage smStorage) {
    this.stateMachineStorage = smStorage;
  }

  public StateMachineStorage getStateMachineStorage() {
    return stateMachineStorage;
  }

  @Override
  public String toString() {
    return getStorageDir() + "";
  }
}
