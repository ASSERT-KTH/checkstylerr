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

package org.apache.marmotta.kiwi.sparql.builder;

import org.openrdf.query.algebra.ValueExpr;

import java.util.Set;

/**
 * Common fields and methods for all subqueries.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class SQLAbstractSubquery extends SQLClause {

    protected String alias;

    public SQLAbstractSubquery(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * Return the SQL variables used by the subquery; we need this to do proper mapping in the parent query.
     * @return
     */
    public abstract Set<SQLVariable> getQueryVariables();

    /**
     * Return the projection type of an expression in this subquery. Needed for propagation up to the parent.
     * @param expr
     * @return
     */
    protected abstract ProjectionType getProjectionType(ValueExpr expr);
}
