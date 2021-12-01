/*
 * Copyright (C) 2016 Couchbase, Inc.
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

package com.couchbase.client.java.subdoc;

import java.util.Collections;
import java.util.List;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.core.message.kv.subdoc.multi.Mutation;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.subdoc.SubDocumentException;

/**
 * A fragment of a {@link JsonDocument JSON Document}, that is to say one or several JSON values from the document
 * (including String, {@link JsonObject}, {@link JsonArray}, etc...), as returned and used in the sub-document API.
 *
 * @author Michael Nitschinger
 * @author Simon Baslé
 * @since 2.2
 * @param <OPERATION> the broad type of subdocument operation, either {@link Lookup} or {@link Mutation}.
 */
@InterfaceStability.Committed
@InterfaceAudience.Public
public class DocumentFragment<OPERATION> {

    private final String id;
    private final long cas;
    private final MutationToken mutationToken;

    private final List<SubdocOperationResult<OPERATION>> resultList;

    public DocumentFragment(String id, long cas, MutationToken mutationToken, List<SubdocOperationResult<OPERATION>> resultList) {
        this.id = id;
        this.cas = cas;
        this.mutationToken = mutationToken;

        this.resultList = resultList == null ? Collections.<SubdocOperationResult<OPERATION>>emptyList() : resultList;
    }

    /**
     * @return the {@link JsonDocument#id() id} of the enclosing JSON document in which this fragment belongs.
     */
    public String id() {
        return this.id;
    }

    /**
     * The CAS (Create-and-Set) is set by the SDK when mutating, reflecting the new CAS from the enclosing JSON document.
     *
     * @return the CAS value related to the enclosing JSON document.
     */
    public long cas() {
        return this.cas;
    }

    /**
     * @return the updated {@link MutationToken} related to the enclosing JSON document after a mutation.
     */
    public MutationToken mutationToken() {
        return this.mutationToken;
    }

    /**
     * @return the number of lookup or mutation specifications that were performed, which is also the number
     * of results.
     */
    public int size() {
        return resultList.size();
    }

    /**
     * Attempt to get the value corresponding to the first operation that targeted the given path, as a specific class.
     * If the operation was successful, the value will be returned. Otherwise the adequate {@link SubDocumentException}
     * will be thrown (mostly in the case of multiple lookups).
     *
     * If multiple operations targeted the same path, this method only considers the first one (see
     * {@link #content(int, Class)} to get a result by index).
     *
     * @param path the path to look for.
     * @param targetClass the expected type of the content.
     * @return the content if one could be retrieved and no error occurred.
     */
    public <T> T content(String path, Class<T> targetClass) {
        if (path == null) {
            return null;
        }
        for (SubdocOperationResult<OPERATION> result : resultList) {
            if (path.equals(result.path())) {
                return interpretResult(result);
            }
        }
        return null;
    }

    /**
     * Attempt to get the value corresponding to the first operation that targeted the given path, as an Object.
     * If the operation was successful, the value will be returned. Otherwise the adequate {@link SubDocumentException}
     * will be thrown (mostly in the case of multiple lookups).
     *
     * If multiple operations targeted the same path, this method only considers the first one (see
     * {@link #content(int)} to get a result by index).
     *
     * @param path the path to look for.
     * @return the content if one could be retrieved and no error occurred.
     */
    public Object content(String path) {
        return this.content(path, Object.class);
    }

    /**
     * Attempt to get the value corresponding to the n-th operation, as a specific class.
     * If the operation was successful, the value will be returned. Otherwise the adequate {@link SubDocumentException}
     * will be thrown (mostly in the case of multiple lookups).
     *
     * @param index the 0-based index of the operation to look for.
     * @param targetClass the expected type of the content.
     * @return the content if one could be retrieved and no error occurred.
     */
    public <T> T content(int index, Class<T> targetClass) {
        return interpretResult(resultList.get(index));
    }

    /**
     * Attempt to get the value corresponding to the n-th operation, as an Object.
     * If the operation was successful, the value will be returned. Otherwise the adequate {@link SubDocumentException}
     * will be thrown (mostly in the case of multiple lookups).
     *
     * @param index the 0-based index of the operation to look for.
     * @return the content if one could be retrieved and no error occurred.
     */
    public Object content(int index) {
        return this.content(index, Object.class);
    }

    private <T> T interpretResult(SubdocOperationResult<OPERATION> result) {
        if (result.status() == ResponseStatus.FAILURE && result.value() instanceof RuntimeException) {
            //case where a fatal error happened while PARSING the response
            throw (RuntimeException) result.value();
        } else if (result.value() instanceof CouchbaseException) {
            //case where the server returned an error for this operation
            throw (CouchbaseException) result.value();
        } else {
            //case where the server returned a value (or null if not applicable) for this operation
            return (T) result.value();
        }
    }

    /**
     * Get the operation status code corresponding to the first operation that targeted the given path.
     *
     * This can be used in place of {@link #content(String)} in order to avoid an {@link CouchbaseException} being thrown.
     *
     * @param path the path of the desired operation.
     * @return the status of the operation.
     */
    public ResponseStatus status(String path) {
        if (path == null) {
            return null;
        }
        for (SubdocOperationResult<OPERATION> result : resultList) {
            if (path.equals(result.path())) {
                return result.status();
            }
        }
        return null;
    }

    /**
     * Get the operation status code corresponding to the n-th operation.
     *
     * This can be used in place of {@link #content(int)} in order to avoid an {@link CouchbaseException} being thrown.
     *
     * @param index the 0-based index of the desired operation.
     * @return the status of the operation.
     */
    public ResponseStatus status(int index) {
        return resultList.get(index).status();
    }

    /**
     * Checks whether the given path is part of this result set, eg. an operation targeted it, and the operation executed successfully.
     *
     * @return true if that path is part of the successful result set, false in any other case.
     */
    public boolean exists(String path) {
        if (path == null) {
            return false;
        }
        for (SubdocOperationResult<OPERATION> result : resultList) {
            if (path.equals(result.path()) && !(result.value() instanceof Exception)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given index is part of this result set and the operation was executed successfully.
     *
     * @return true if that path is part of the successful result set, false in any other case.
     */
    public boolean exists(int specIndex) {
        return specIndex >= 0 && specIndex < resultList.size()
                && !(resultList.get(specIndex).value() instanceof Exception);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DocumentFragment{")
            .append("id='").append(id).append('\'')
                .append(", cas=").append(cas)
                .append(", mutationToken=").append(mutationToken)
                .append('}');
        if (resultList != null && !resultList.isEmpty()) {
            sb.append(resultList);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentFragment<?> that = (DocumentFragment<?>) o;

        if (cas != that.cas) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (mutationToken != null ? !mutationToken.equals(that.mutationToken) : that.mutationToken != null) {
            return false;
        }
        return resultList.equals(that.resultList);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (cas ^ (cas >>> 32));
        result = 31 * result + (mutationToken != null ? mutationToken.hashCode() : 0);
        result = 31 * result + resultList.hashCode();
        return result;
    }
}