/**
 * Copyright (C) 2015 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.java.query.dsl.path.index;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.path.Path;

/**
 * On path in the Index creation DSL.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceStability.Experimental
@InterfaceAudience.Public
public interface OnPath extends Path {

    /**
     * Describes the target of a secondary N1QL index.
     *
     * @param keyspace the keyspace (bucket name, it will be automatically escaped).
     * @param expression the base expression to be indexed (mandatory).
     * @param additionalExpressions additional expressions to be indexed (optional).
     */
    WherePath on(String keyspace, Expression expression, Expression... additionalExpressions);

    /**
     * Describes the target of a secondary N1QL index.
     *
     * @param namespace optional prefix for the keyspace (it will be automatically escaped).
     * @param keyspace the keyspace (bucket name, it will be automatically escaped).
     * @param expression the base expression to be indexed (mandatory).
     * @param additionalExpressions additional expressions to be indexed (optional).
     */
    WherePath on(String namespace, String keyspace, Expression expression, Expression... additionalExpressions);



}
