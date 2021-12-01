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
package org.apache.metron.solr.writer;

import com.google.common.collect.Iterables;
import org.apache.metron.solr.SolrConstants;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.CollectionParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MetronSolrClient extends CloudSolrClient {

  private static final Logger LOG = LoggerFactory
          .getLogger(MetronSolrClient.class);


  public MetronSolrClient(String zkHost) {
    super(zkHost);
  }

  public MetronSolrClient(String zkHost, Map<String, Object> solrHttpConfig) {
    super(zkHost, HttpClientUtil.createClient(toSolrProps(solrHttpConfig)));
  }

  public static SolrParams toSolrProps(Map<String, Object> config) {
    if(config == null || config.isEmpty()) {
      return null;
    }

    ModifiableSolrParams ret = new ModifiableSolrParams();
    for(Map.Entry<String, Object> kv : config.entrySet()) {
      Object v = kv.getValue();
      if(v instanceof Boolean) {
        ret.set(kv.getKey(), (Boolean)v);
      }
      else if(v instanceof Integer) {
        ret.set(kv.getKey(), (Integer)v);
      }
      else if(v instanceof Iterable) {
        Iterable vals = (Iterable)v;
        String[] strVals = new String[Iterables.size(vals)];
        int i = 0;
        for(Object o : (Iterable)v) {
          strVals[i++] = o.toString();
        }
      }
    }
    return ret;
  }

  public void createCollection(String name, int numShards, int replicationFactor) throws IOException, SolrServerException {
    if (!listCollections().contains(name)) {
      request(getCreateCollectionsRequest(name, numShards, replicationFactor));
    }
  }

  public QueryRequest getCreateCollectionsRequest(String name, int numShards, int replicationFactor) {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(SolrConstants.REQUEST_ACTION, CollectionParams.CollectionAction.CREATE.name());
    params.set(SolrConstants.REQUEST_NAME, name);
    params.set(SolrConstants.REQUEST_NUM_SHARDS, numShards);
    params.set(SolrConstants.REQUEST_REPLICATION_FACTOR, replicationFactor);
    params.set(SolrConstants.REQUEST_COLLECTION_CONFIG_NAME, name);
    QueryRequest request = new QueryRequest(params);
    request.setPath(SolrConstants.REQUEST_COLLECTIONS_PATH);
    return request;
  }

  @SuppressWarnings("unchecked")
  public List<String> listCollections() throws IOException, SolrServerException {
    NamedList<Object> response = request(getListCollectionsRequest(), null);
    return (List<String>) response.get(SolrConstants.RESPONSE_COLLECTIONS);
  }

  public QueryRequest getListCollectionsRequest() {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(SolrConstants.REQUEST_ACTION, CollectionParams.CollectionAction.LIST.name());
    QueryRequest request = new QueryRequest(params);
    request.setPath(SolrConstants.REQUEST_COLLECTIONS_PATH);
    return request;
  }
}
