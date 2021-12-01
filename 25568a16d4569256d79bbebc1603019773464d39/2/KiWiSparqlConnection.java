/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.persistence;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iterations;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.sparql.builder.ProjectionType;
import org.apache.marmotta.kiwi.sparql.builder.SQLBuilder;
import org.apache.marmotta.kiwi.sparql.builder.SQLVariable;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Provide improved SPARQL support by evaluating certain common compley SPARQL constructs directly on the
 * database (e.g. JOIN over pattern queries).
 * <p/>
 * Implemented using a decorator pattern (i.e. wrapping the KiWiConnection).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiSparqlConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiSparqlConnection.class);

    private KiWiConnection parent;
    private KiWiValueFactory valueFactory;

    private ExecutorService executorService;

    public KiWiSparqlConnection(KiWiConnection parent, KiWiValueFactory valueFactory) throws SQLException {
        this.parent = parent;
        this.valueFactory = valueFactory;

        // interruptible queries run in a separate thread
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Evaluate a statement pattern join or filter on the database by translating it into an appropriate SQL statement.
     * Copied and adapted from KiWiReasoningConnection.query()
     *
     * @param join
     * @param dataset
     * @return
     */
    public CloseableIteration<BindingSet, SQLException> evaluateNative(TupleExpr join, final BindingSet bindings, final Dataset dataset) throws SQLException, InterruptedException {

        try {
            final SQLBuilder builder = new SQLBuilder(join, bindings, dataset, valueFactory, parent.getDialect());

            final PreparedStatement queryStatement = parent.getJDBCConnection().prepareStatement(builder.build());
            if (parent.getDialect().isCursorSupported()) {
                queryStatement.setFetchSize(parent.getConfiguration().getCursorSize());
            }

            Future<ResultSet> queryFuture =
                    executorService.submit(new Callable<ResultSet>() {
                                               @Override
                                               public ResultSet call() throws Exception {
                                                   try {
                                                       return queryStatement.executeQuery();
                                                   } catch (SQLException ex) {
                                                       if (Thread.interrupted()) {
                                                           log.info("SQL query execution cancelled; not returning result (Thread={})", Thread.currentThread());
                                                           throw new InterruptedException("SPARQL query execution cancelled");
                                                       } else {
                                                           throw ex;
                                                       }
                                                   }
                                               }
                                           }
                    );

            try {
                ResultSet result = queryFuture.get();

                ResultSetIteration<BindingSet> it = new ResultSetIteration<BindingSet>(result, true, new ResultTransformerFunction<BindingSet>() {
                    @Override
                    public BindingSet apply(ResultSet row) throws SQLException {
                        MapBindingSet resultRow = new MapBindingSet();

                        List<SQLVariable> vars = new ArrayList<>(builder.getVariables().values());

                        long[] nodeIds = new long[vars.size()];
                        for(int i=0; i<vars.size(); i++) {
                            SQLVariable sv = vars.get(i);
                            if(sv.getProjectionType() == ProjectionType.NODE) {
                                nodeIds[i] = row.getLong(sv.getName());
                            }
                        }
                        KiWiNode[] nodes = parent.loadNodesByIds(nodeIds);

                        for (int i = 0; i < vars.size(); i++) {
                            SQLVariable sv = vars.get(i);
                            if(nodes[i] != null) {
                                // resolved node
                                resultRow.addBinding(sv.getSparqlVariable().getName(), nodes[i]);
                            } else if(sv.getProjectionType() != ProjectionType.NONE) {
                                // literal value
                                String value = row.getString(sv.getName());
                                if(value != null) {
                                    switch (sv.getProjectionType()) {
                                        case URI:
                                            resultRow.addBinding(sv.getSparqlVariable().getName(), new URIImpl(value));
                                            break;
                                        case INT:
                                            resultRow.addBinding(sv.getSparqlVariable().getName(), new LiteralImpl(value, XSD.Integer));
                                            break;
                                        case DOUBLE:
                                            resultRow.addBinding(sv.getSparqlVariable().getName(), new LiteralImpl(value, XSD.Double));
                                            break;
                                        case STRING:
                                            resultRow.addBinding(sv.getSparqlVariable().getName(), new LiteralImpl(value));
                                            break;
                                        default:
                                            resultRow.addBinding(sv.getSparqlVariable().getName(), new LiteralImpl(value));
                                            break;
                                    }
                                }
                            }
                        }


                        if (bindings != null) {
                            for (Binding binding : bindings) {
                                resultRow.addBinding(binding);
                            }
                        }
                        return resultRow;
                    }
                });

                // materialize result to avoid having more than one result set open at the same time
                return new CloseableIteratorIteration<BindingSet, SQLException>(Iterations.asList(it).iterator());
            } catch (InterruptedException | CancellationException e) {
                log.info("SPARQL query execution cancelled");
                queryFuture.cancel(true);
                queryStatement.cancel();
                queryStatement.close();

                throw new InterruptedException("SPARQL query execution cancelled");
            } catch (ExecutionException e) {
                log.error("error executing SPARQL query", e.getCause());
                if (e.getCause() instanceof SQLException) {
                    throw (SQLException) e.getCause();
                } else if (e.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) e.getCause();
                } else {
                    throw new SQLException("error executing SPARQL query", e);
                }
            }
        } catch (UnsatisfiableQueryException ex) {
            return new EmptyIteration<>();
        }
    }

    public KiWiDialect getDialect() {
        return parent.getDialect();
    }
}
