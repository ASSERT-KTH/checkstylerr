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
package org.apache.marmotta.platform.sparql.api.sparql;

import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriter;

import java.util.List;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public interface SparqlService {

    /**
     * Evaluate a SPARQL query on the KiWi TripleStore. Writes the query results 
     * to the output stream passed as argument os in the output format specified 
     * as argument outputFormat.
     *
     * see http://www.w3.org/TR/sparql11-query/
     *
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     * @param tupleWriter   the writer to use to write tuple query results
     * @param booleanWriter the writer to use to write boolean query results
     * @param graphWriter
     * @throws InvalidArgumentException if the output format or query language are undefined
     * @throws org.apache.marmotta.platform.core.exception.MarmottaException if the query evaluation fails
     */
	public void query(QueryLanguage queryLanguage, String query, TupleQueryResultWriter tupleWriter, BooleanQueryResultWriter booleanWriter, SPARQLGraphResultWriter graphWriter) throws InvalidArgumentException, MarmottaException, MalformedQueryException, QueryEvaluationException;

    /**
     * Evaluate a SPARQL ASK query on the KiWi TripleStore
     *
     * see http://www.w3.org/TR/sparql11-query/
     *
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     * @throws org.apache.marmotta.platform.core.exception.MarmottaException if the query evaluation fails
     */
	public boolean ask(QueryLanguage queryLanguage, String query) throws MarmottaException;

    /**
     * Evaluate a SPARQL query on the LMF TripleStore. Returns the results as a list of result maps, each element
     * a KiWiNode.
     *
     * see http://www.w3.org/TR/sparql11-query/
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     */
    public List<Map<String,Value>> query(QueryLanguage queryLanguage, String query) throws MarmottaException;

    /**
     * Execute a SPARQL update on the LMF TripleStore. Throws a KiWiException in case the update execution fails.
     *
     * see http://www.w3.org/TR/sparql11-update/
     *
     * @param queryLanguage
     * @param query  a string representing the update query in SPARQL Update 1.1 syntax
     * @throws Exception
     */
    public void update(QueryLanguage queryLanguage, String query) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException;

}
