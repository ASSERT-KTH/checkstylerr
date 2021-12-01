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
package com.couchbase.client.java.query;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;

/**
 * The payload necessary to perform a {@link PreparedN1qlQuery}, as returned when
 * issuing a {@link PrepareStatement}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceAudience.Private
@InterfaceStability.Experimental
public class PreparedPayload implements SerializableStatement {

    private static final long serialVersionUID = -5950676152745796617L;

    private final String preparedName;
    private final SerializableStatement originalStatement;
    private final String encodedPlan;

    public PreparedPayload(Statement originalStatement, String preparedName, String encodedPlan) {
        this.originalStatement = originalStatement instanceof SerializableStatement
                ? (SerializableStatement) originalStatement
                : new N1qlQuery.RawStatement(originalStatement.toString());
        this.preparedName = preparedName;
        this.encodedPlan = encodedPlan;
    }

    public String payload() {
        return preparedName;
    }

    public String encodedPlan() {
        return encodedPlan;
    }

    public String preparedName() {
        return preparedName;
    }

    public SerializableStatement originalStatement() {
        return originalStatement;
    }

    @Override
    public String toString() {
        return payload();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreparedPayload that = (PreparedPayload) o;

        if (preparedName != null ? !preparedName.equals(that.preparedName) : that.preparedName != null)
            return false;
        if (originalStatement != null ? !originalStatement.equals(that.originalStatement) : that.originalStatement != null)
            return false;
        return !(encodedPlan != null ? !encodedPlan.equals(that.encodedPlan) : that.encodedPlan != null);

    }

    @Override
    public int hashCode() {
        int result = preparedName != null ? preparedName.hashCode() : 0;
        result = 31 * result + (originalStatement != null ? originalStatement.hashCode() : 0);
        result = 31 * result + (encodedPlan != null ? encodedPlan.hashCode() : 0);
        return result;
    }
}
