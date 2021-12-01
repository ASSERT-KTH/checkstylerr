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

package com.couchbase.client.java.document.subdoc;

import java.util.Arrays;
import java.util.List;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.java.document.JsonDocument;

/**
 * Compilation of {@link LookupResult} each corresponding to a {@link LookupSpec} in a given
 * {@link JsonDocument}.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceStability.Experimental
@InterfaceAudience.Public
public class MultiLookupResult {

    private final String documentId;

    private final List<LookupSpec> specs;
    private final List<LookupResult> results;
    private final boolean hasSuccess;
    private final boolean hasFailure;

    /**
     * Construct a new {@link MultiLookupResult} for a given document, specs and associated results.
     *
     * @param documentId the document that was looked up.
     * @param specs the subdocument {@link LookupSpec} that targeted the document.
     * @param results the {@link LookupResult} associated to each spec, denoting individual success or error.
     */
    public MultiLookupResult(String documentId, List<LookupSpec> specs,
            List<LookupResult> results) {
        this.documentId = documentId;
        this.specs = specs;
        this.results = results;

        boolean hasSuccess = false;
        boolean hasFailure = false;
        for (LookupResult r : results) {
            if (r.status().isSuccess()) {
                hasSuccess = true;
            } else {
                hasFailure = true;
            }
        }
        this.hasFailure = hasFailure;
        this.hasSuccess = hasSuccess;
    }

    /**
     * Construct a new {@link MultiLookupResult} for a given document, specs and associated results.
     *
     * @param documentId the document that was looked up.
     * @param specs the subdocument {@link LookupSpec} that targeted the document (as an array, convenience for varargs).
     * @param results the {@link LookupResult} associated to each spec, denoting individual success or error.
     */
    public MultiLookupResult(String documentId, LookupSpec[] specs, List<LookupResult> results) {
        this(documentId, Arrays.asList(specs), results);
    }

    /**
     * @return the id of the {@link JsonDocument} targeted by the lookup.
     */
    public String documentId() {
        return documentId;
    }

    /**
     * @return the list of {@link LookupSpec} that was run on the document.
     */
    public List<LookupSpec> specs() {
        return specs;
    }

    /**
     * Returns the list of {@link LookupResult} corresponding (in same order) to the {@link #specs()}.
     * Each individual {@link LookupResult} denotes success or error of its associated {@link LookupSpec}.
     *
     * @return the list of LookupResult.
     */
    public List<LookupResult> results() {
        return results;
    }

    /**
     * @return true if at least one of the results is a success.
     * @see #hasFailure()
     * @see #isTotalSuccess()
     */
    public boolean hasSuccess() {
        return hasSuccess;
    }

    /**
     * @return true if at least one of the results is a failure.
     * @see #hasSuccess()
     * @see #isTotalFailure()
     */
    public boolean hasFailure() {
        return hasFailure;
    }

    /**
     * @return true if ALL the results are successes.
     */
    public boolean isTotalSuccess() {
        return !hasFailure;
    }

    /**
     * @return true if ALL the results are failures.
     */
    public boolean isTotalFailure() {
        return !hasSuccess;
    }
}
