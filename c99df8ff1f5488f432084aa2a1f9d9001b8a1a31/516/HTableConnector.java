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

package org.apache.metron.hbase;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import org.apache.storm.generated.Bolt;

/**
 * HTable connector for Storm {@link Bolt}
 * <p>
 * The HBase configuration is picked up from the first <tt>hbase-site.xml</tt> encountered in the
 * classpath
 */
@SuppressWarnings("serial")
public class HTableConnector extends Connector implements Serializable{
  private static final Logger LOG = Logger.getLogger(HTableConnector.class);
  private Configuration conf;
  protected HTableInterface table;
  private String tableName;
  private String connectorImpl;


  /**
   * Initialize HTable connection
   * @param conf The {@link TupleTableConfig}
   * @throws IOException
   */
  public HTableConnector(final TableConfig conf, String _quorum, String _port) throws IOException {
    super(conf, _quorum, _port);
    this.connectorImpl = conf.getConnectorImpl();
    this.tableName = conf.getTableName();
    this.conf = HBaseConfiguration.create();
    
    if(_quorum != null && _port != null)
    {
    	this.conf.set("hbase.zookeeper.quorum", _quorum);
    	this.conf.set("hbase.zookeeper.property.clientPort", _port);
    }

    LOG.info(String.format("Initializing connection to HBase table %s at %s", tableName,
      this.conf.get("hbase.rootdir")));

    try {
      this.table = getTableProvider().getTable(this.conf, this.tableName);
    } catch (IOException ex) {
      throw new IOException("Unable to establish connection to HBase table " + this.tableName, ex);
    }

    if (conf.isBatch()) {
      // Enable client-side write buffer
      this.table.setAutoFlush(false, true);
      LOG.info("Enabled client-side write buffer");
    }

    // If set, override write buffer size
    if (conf.getWriteBufferSize() > 0) {
      try {
        this.table.setWriteBufferSize(conf.getWriteBufferSize());

        LOG.info("Setting client-side write buffer to " + conf.getWriteBufferSize());
      } catch (IOException ex) {
        LOG.error("Unable to set client-side write buffer size for HBase table " + this.tableName,
          ex);
      }
    }

    // Check the configured column families exist
    for (String cf : conf.getColumnFamilies()) {
      if (!columnFamilyExists(cf)) {
        throw new RuntimeException(String.format(
          "HBase table '%s' does not have column family '%s'", conf.getTableName(), cf));
      }
    }
  }

  protected TableProvider getTableProvider() throws IOException {
    if(connectorImpl == null || connectorImpl.length() == 0 || connectorImpl.charAt(0) == '$') {
      return new HTableProvider();
    }
    else {
      try {
        Class<? extends TableProvider> clazz = (Class<? extends TableProvider>) Class.forName(connectorImpl);
        return clazz.getConstructor().newInstance();
      } catch (InstantiationException e) {
        throw new IOException("Unable to instantiate connector.", e);
      } catch (IllegalAccessException e) {
        throw new IOException("Unable to instantiate connector: illegal access", e);
      } catch (InvocationTargetException e) {
        throw new IOException("Unable to instantiate connector", e);
      } catch (NoSuchMethodException e) {
        throw new IOException("Unable to instantiate connector: no such method", e);
      } catch (ClassNotFoundException e) {
        throw new IOException("Unable to instantiate connector: class not found", e);
      }
    }
  }

  /**
   * Checks to see if table contains the given column family
   * @param columnFamily The column family name
   * @return boolean
   * @throws IOException
   */
  private boolean columnFamilyExists(final String columnFamily) throws IOException {
    return this.table.getTableDescriptor().hasFamily(Bytes.toBytes(columnFamily));
  }

  /**
   * @return the table
   */
  public HTableInterface getTable() {
    return table;
  }

  @Override
  public void put(Put put) throws IOException {
      table.put(put);
  }

  /**
   * Close the table
   */
  @Override
  public void close() {
    try {
      this.table.close();
    } catch (IOException ex) {
      LOG.error("Unable to close connection to HBase table " + tableName, ex);
    }
  }
}
