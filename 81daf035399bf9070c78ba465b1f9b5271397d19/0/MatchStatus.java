/**
 * Copyright (c) 2019, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1)Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3)Neither the name of zold-java-client nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.zold;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;

import java.net.URI;

/**
 * An Apache ResponseHandler that tries to match the Response's status code
 * with the expected one.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: f3e8f1c8a202c4b4d082ad15ea40a717db6eb474 $
 * @since 0.0.1
 */
final class MatchStatus implements ResponseHandler<HttpResponse> {

    /**
     * Called URI.
     */
    private final URI called;

    /**
     * Expected status.
     */
    private final int expected;

    /**
     * Ctor.
     * @param called Called URI.
     * @param expected Expected Http status code.
     */
    MatchStatus(final URI called, final int expected) {
        this.called = called;
        this.expected = expected;
    }

    @Override
    public HttpResponse handleResponse(final HttpResponse response) {
        final int actual = response.getStatusLine().getStatusCode();
        if(actual != this.expected) {
            throw new UnexpectedResponseException(
                this.called.toString(), actual,
                this.expected, new PayloadOf(response)
            );
        }
        return response;
    }

}
