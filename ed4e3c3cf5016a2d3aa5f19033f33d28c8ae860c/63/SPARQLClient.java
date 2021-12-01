/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
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
package org.apache.marmotta.client.clients;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.model.rdf.BNode;
import org.apache.marmotta.client.model.rdf.Literal;
import org.apache.marmotta.client.model.rdf.RDFNode;
import org.apache.marmotta.client.model.rdf.URI;
import org.apache.marmotta.client.model.sparql.SPARQLResult;
import org.apache.marmotta.client.util.HTTPUtil;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SPARQLClient {

    private static Logger log = LoggerFactory.getLogger(SPARQLClient.class);

    private static final String URL_QUERY_SERVICE  = "/sparql/select";
    private static final String URL_UPDATE_SERVICE = "/sparql/update";

    private ClientConfiguration config;

    public SPARQLClient(ClientConfiguration config) {
        this.config = config;
    }

    /**
     * Run a SPARQL Select query against the Marmotta Server and return the results as SPARQL Result. Results will be
     * transfered and parsed using the SPARQL JSON format.
     * @param query a SPARQL Select query to run on the database
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    @SuppressWarnings("unchecked")
    public SPARQLResult select(String query) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_QUERY_SERVICE + "?query=" + URLEncoder.encode(query, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/sparql-results+json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL Query {} evaluated successfully",query);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    Map<String,Map<String,List<?>>> resultMap =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,Map<String,List<?>>>>(){});

                    if(resultMap.isEmpty()) {
                        return null;
                    } else {
                        List<?> head = resultMap.get("head").get("vars");
                        Set<String> fieldNames = new HashSet<String>();
                        for(Object o : head) {
                            if(o instanceof String) {
                                fieldNames.add((String)o);
                            }
                        }

                        SPARQLResult result = new SPARQLResult(fieldNames);

                        List<?> bindings = resultMap.get("results").get("bindings");
                        for(Object o : bindings) {
                            if(o instanceof Map) {
                                Map<String,RDFNode> row = new HashMap<String, RDFNode>();
                                for(Map.Entry<String,?> entry : ((Map<String,?>)o).entrySet()) {
                                    Map<String,String> nodeDef = (Map<String,String>) entry.getValue();
                                    RDFNode node = null;
                                    if("uri".equalsIgnoreCase(nodeDef.get("type"))) {
                                        node = new URI(nodeDef.get("value"));
                                    } else if("literal".equalsIgnoreCase(nodeDef.get("type")) ||
                                              "typed-literal".equalsIgnoreCase(nodeDef.get("type"))) {
                                        String lang = nodeDef.get("xml:lang");
                                        String datatype = nodeDef.get("datatype");

                                        if(lang != null) {
                                            node = new Literal(nodeDef.get("value"),lang);
                                        } else if(datatype != null) {
                                            node = new Literal(nodeDef.get("value"),new URI(datatype));
                                        } else {
                                            node = new Literal(nodeDef.get("value"));
                                        }
                                    } else if("bnode".equalsIgnoreCase(nodeDef.get("type"))) {
                                        node = new BNode(nodeDef.get("value"));
                                    } else {
                                        log.error("unknown result node type: {}",nodeDef.get("type"));
                                    }
                                    
                                    if(node != null) {
                                        row.put(entry.getKey(),node);
                                    }
                                }
                                result.add(row);
                            }
                        }
                        return result;
                    }
                default:
                    log.error("error evaluating SPARQL Select Query {}: {} {}",new Object[] {query,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL Select Query "+query+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Carry out a SPARQL ASK Query and return either true or false, depending on the query result.
     *
     * @param askQuery
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public boolean ask(String askQuery) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_QUERY_SERVICE + "?query=" + URLEncoder.encode(askQuery, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/sparql-results+json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL ASK Query {} evaluated successfully",askQuery);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    Map<String,Object> resultMap =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,Object>>(){});

                    if(resultMap.isEmpty()) {
                        return false;
                    } else {
                        Boolean result = resultMap.get("boolean") != null && ((String)resultMap.get("boolean")).equalsIgnoreCase("true");
                        return result;
                    }
                default:
                    log.error("error evaluating SPARQL ASK Query {}: {} {}",new Object[] {askQuery,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL ASK Query "+askQuery+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Execute a SPARQL Update query according to the SPARQL 1.1 standard. The query will only be passed to the server,
     * which will react either with ok (in this the method simply returns) or with error (in this case, the method
     * throws an MarmottaClientException).
     *
     * @param updateQuery         the SPARQL Update 1.1 query string
     * @throws IOException        in case a connection problem occurs
     * @throws MarmottaClientException in case the server returned and error and did not execute the update
     */
    public void update(String updateQuery) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_UPDATE_SERVICE + "?update=" + URLEncoder.encode(updateQuery, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        
        try {
                
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL UPDATE Query {} evaluated successfully",updateQuery);
                    break;
                default:
                    log.error("error evaluating SPARQL UPDATE Query {}: {} {}",new Object[] {updateQuery,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL UPDATE Query "+updateQuery +": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }
    
}
