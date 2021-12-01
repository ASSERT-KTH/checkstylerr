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
import com.couchbase.client.java.document.json.JsonObject;

import java.io.Serializable;

/**
 * A class that represents N1QL metrics.
 *
 * Note that the server could omit the metrics or part of the metrics,
 * in which case an {@link #EMPTY_METRICS} will be returned.
 *
 * @author Simon Baslé
 * @since 2.1
 */
@InterfaceStability.Experimental
@InterfaceAudience.Public
public class N1qlMetrics implements Serializable{

    private static final long serialVersionUID = -1955101433653293743L;

    /**
     * The empty metrics object. All numerical values will be 0 and human-readable times
     * will be {@link #NO_TIME}.
     */
    public static final N1qlMetrics EMPTY_METRICS = new N1qlMetrics();

    /**
     * Human-readable representation of the absence of duration, as "0s".
     */
    public static final String NO_TIME = "0s";

    private final JsonObject rawMetrics;

    private final int resultCount;
    private final int errorCount;
    private final int warningCount;
    private final int mutationCount;
    private final int sortCount;
    private final long resultSize;
    private final String elapsedTime;
    private final String executionTime;

    private N1qlMetrics() {
        this(JsonObject.empty());
    }

    public N1qlMetrics(JsonObject rawMetrics) {
        this.rawMetrics = rawMetrics;

        if (rawMetrics.getString("elapsedTime") == null) {
            this.elapsedTime = NO_TIME;
        } else {
            this.elapsedTime = rawMetrics.getString("elapsedTime");
        }

        if (rawMetrics.getString("executionTime") == null) {
            this.executionTime = NO_TIME;
        } else {
            this.executionTime = rawMetrics.getString("executionTime");
        }

        Integer resultCount = rawMetrics.getInt("resultCount");
        this.resultCount = resultCount == null ? 0 : resultCount;

        Integer errorCount = rawMetrics.getInt("errorCount");
        this.errorCount = errorCount == null ? 0 : errorCount;

        Integer warningCount = rawMetrics.getInt("warningCount");
        this.warningCount = warningCount == null ? 0 : warningCount;

        Integer mutationCount = rawMetrics.getInt("mutationCount");
        this.mutationCount = mutationCount == null ? 0 : mutationCount;

        Integer sortCount = rawMetrics.getInt("sortCount");
        this.sortCount = sortCount == null ? 0 : sortCount;

        Long resultSize = rawMetrics.getLong("resultSize");
        this.resultSize = resultSize == null ? 0L : resultSize;
    }

    /**
     * @return The total time taken for the request, that is the time from when the
     * request was received until the results were returned, in a human-readable
     * format (eg. 123.45ms for a little over 123 milliseconds).
     */
    public String elapsedTime() {
        return elapsedTime;
    }

    /**
     * @return The time taken for the execution of the request, that is the time from
     * when query execution started until the results were returned, in a human-readable
     * format (eg. 123.45ms for a little over 123 milliseconds).
     */
    public String executionTime() {
        return executionTime;
    }

    /**
     * @return the total number of results selected by the engine before restriction
     * through LIMIT clause.
     */
    public int sortCount() {
        return sortCount;
    }

    /**
     * @return The total number of objects in the results.
     */
    public int resultCount() {
        return resultCount;
    }

    /**
     * @return The total number of bytes in the results.
     */
    public long resultSize() {
        return resultSize;
    }

    /**
     * @return The number of mutations that were made during the request.
     */
    public int mutationCount() {
        return mutationCount;
    }
    /**
     * @return The number of errors that occurred during the request.
     */
    public int errorCount() {
        return errorCount;
    }

    /**
     * @return The number of warnings that occurred during the request.
     */
    public int warningCount() {
        return warningCount;
    }

    /**
     * Exposes the underlying raw form of the metrics, as a {@link JsonObject}.
     *
     * Note that values exposed as methods are cached at instantiation, so this
     * object is not backed by the returned JsonObject.
     *
     * @return the underlying raw form of the metrics.
     */
    public JsonObject asJsonObject() {
        return rawMetrics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("N1qlMetrics{");
        sb.append(", resultCount=").append(resultCount);
        sb.append(", errorCount=").append(errorCount);
        sb.append(", warningCount=").append(warningCount);
        sb.append(", mutationCount=").append(mutationCount);
        sb.append(", sortCount=").append(sortCount);
        sb.append(", resultSize=").append(resultSize);
        sb.append(", elapsedTime='").append(elapsedTime).append('\'');
        sb.append(", executionTime='").append(executionTime).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
