/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.java.query.dsl.path.index;

import static com.couchbase.client.java.query.dsl.Expression.x;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.element.WhereElement;
import com.couchbase.client.java.query.dsl.path.AbstractPath;

/**
 * See {@link WherePath}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceStability.Experimental
@InterfaceAudience.Private
public class DefaultWherePath extends DefaultUsingWithPath implements WherePath {

    protected DefaultWherePath(AbstractPath parent) {
        super(parent);
    }

    @Override
    public UsingWithPath where(Expression filterExpression) {
        element(new WhereElement(filterExpression));
        return new DefaultUsingWithPath(this);
    }

    @Override
    public UsingWithPath where(String filterExpression) {
        return where(x(filterExpression));
    }
}
