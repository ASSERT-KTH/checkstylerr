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
package com.amihaiemil.charles.aws;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

/**
 * A simple aws response handler that only checks that the http status is within the 200 range.
 * If not, {@link AmazonServiceException} is thrown.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 4f0a4f95799dbc7c897e9f03978af47e395f2742 $
 * @since 1.0.0
 *
 */
public class SimpleAwsResponseHandler implements
    HttpResponseHandler<HttpResponse> {

    /**
     * See {@link HttpResponseHandler}, method needsConnectionLeftOpen()
     */
    private boolean needsConnectionLeftOpen;

    /**
     * Ctor.
     * @param connectionLeftOpen Should the connection be closed immediately or not?
     */
    public SimpleAwsResponseHandler(boolean connectionLeftOpen) {
        this.needsConnectionLeftOpen = connectionLeftOpen;
    }

    @Override
    public HttpResponse handle(HttpResponse response) {

        int status = response.getStatusCode();
        if(status < 200 || status >= 300) {
            String content;
            final StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(response.getContent(), writer, "UTF-8");
                content = writer.toString();
            } catch (final IOException e) {
            	content = "Couldn't get response content!";
            }
            AmazonServiceException ase = new AmazonServiceException(content);
            ase.setStatusCode(status);
            throw ase;
        }

        return response;
        
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return this.needsConnectionLeftOpen;
    }

}
