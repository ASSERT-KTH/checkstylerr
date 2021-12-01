/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
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
package com.amihaiemil.charles.aws.requests;

import java.util.Map;
import com.amazonaws.Request;

/**
 * Aws HTTP request with set headers.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 254ed3d9b3d809ab9dbe75aca190e5b0541bd503 $
 * @since 1.0.0
 *
 */
public final class AwsHttpHeaders<T> extends AwsHttpRequest<T> {

    /**
     * Base request.
     */
    private AwsHttpRequest<T> base;

    /**
     * Ctor.
     * @param req Base AwsHttpRequest.
     * @param headers Map of headers to set on the request. Key - headername, value - its value.
     */
    public AwsHttpHeaders(AwsHttpRequest<T> req, Map<String, String> headers) {
        this.base = req;
        this.base.request().getHeaders().putAll(headers);
    }

    @Override
    public T perform() {
        return this.base.perform();
    }

    @Override
    Request<Void> request() {
        return this.base.request();
    }
}
