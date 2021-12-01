/**
 * Copyright (C) 2014 Couchbase, Inc.
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
package com.couchbase.client.core.message.view;

import com.couchbase.client.core.message.AbstractCouchbaseResponse;
import com.couchbase.client.core.message.CouchbaseRequest;
import com.couchbase.client.core.message.ResponseStatus;
import io.netty.buffer.ByteBuf;
import rx.Observable;

public class ViewQueryResponse extends AbstractCouchbaseResponse {

    private final Observable<ByteBuf> rows;
    private final Observable<ByteBuf> info;
    private final Observable<String> error;
    private final int responseCode;
    private final String responsePhrase;

    public ViewQueryResponse(Observable<ByteBuf> rows, Observable<ByteBuf> info, Observable<String> error, int responseCode,
        String responsePhrase, ResponseStatus status, CouchbaseRequest request) {
        super(status, request);
        this.rows = rows;
        this.info = info;
        this.responseCode = responseCode;
        this.responsePhrase = responsePhrase;
        this.error = error;
    }

    public Observable<ByteBuf> rows() {
        return rows;
    }

    public Observable<ByteBuf> info() {
        return info;
    }

    public Observable<String> error() {
        return error;
    }

    public String responsePhrase() {
        return responsePhrase;
    }

    public int responseCode() {
        return responseCode;
    }
}
