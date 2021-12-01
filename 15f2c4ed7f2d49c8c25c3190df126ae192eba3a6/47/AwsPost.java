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

import java.io.InputStream;

import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;


/**
 * Http POST request sent to AWS.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 97ea1f8cdce127b2fada39287252e358e60dc98a $
 * @since 1.0.0
 *
 */
public final class AwsPost<T> extends AwsHttpRequest<T> {

    /**
     * Base request.
     */
    private AwsHttpRequest<T> base;

    /**
     * Ctor.
     * @param req Base AwsHttpRequest.
     * @param content InputStream containing this request's content.
     */
    public AwsPost(AwsHttpRequest<T> req, InputStream content) {
    	this.base = req;
        this.base.request().setHttpMethod(HttpMethodName.POST);
    	this.base.request().setContent(content);
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
