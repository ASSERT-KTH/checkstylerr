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

import org.apache.http.HttpStatus;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

/**
 * An aws response handler that checks if the resource exists or not (status
 * code should be either 404 or 200). If the status is neither 404 nor 200,
 * {@link AmazonServiceException} is thrown.
 * 
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: cb4374e1218f03d99f73ad9865f426a8a134b26c $
 * @since 1.0.0
 *
 */
public class BooleanAwsResponseHandler implements
        HttpResponseHandler<Boolean> {

    @Override
    public Boolean handle(HttpResponse response) {
        int status = response.getStatusCode();
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NOT_FOUND) {
            AmazonServiceException ase = new AmazonServiceException(
                    "Unexpected status: " + status);
            ase.setStatusCode(status);
            throw ase;
        }
        return status == HttpStatus.SC_OK ? true : false;
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return false;
    }

}
