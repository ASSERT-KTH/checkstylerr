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

import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;

/**
 * A signed request made to AWS.
 * @param <T> Response type.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 65de660f62ffa63d84a70ba2a78de04f4678c43c $
 * @since 1.0.0
 * @see <a href="http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html"> AWS Docs </a>
 * @see <a href="https://blogs.aws.amazon.com/security/post/Tx3VP208IBVASUQ/How-to-Control-Access-to-Your-Amazon-Elasticsearch-Service-Domain">Aws blog post</a>
 *
 */
public class SignedRequest<T> extends AwsHttpRequest<T> {

    /**
     * Base request.
     */
    private AwsHttpRequest<T> base;

    /**
     * Ctor.
     * @param req Request to sign.
     */
    public SignedRequest(AwsHttpRequest<T> req) {
        this.base = req;
    }

    @Override
    public T perform() {
        AWS4Signer signer = new AWS4Signer();
        String region = System.getProperty("aws.es.region");
        if(region == null || region.isEmpty()) {
            throw new IllegalStateException("Mandatory sys property aws.es.region not specified!");
        }
        signer.setRegionName(region.trim());
        signer.setServiceName(this.base.request().getServiceName());
        signer.sign(this.base.request(), new AwsCredentialsFromSystem());
        return this.base.perform();
    }

    @Override
    Request<Void> request() {
        return this.base.request();
    }
    
    /**
     * AWS credentials (aws access key id and aws secret key from the system properties).
     */
    private static class AwsCredentialsFromSystem implements AWSCredentials {

        @Override
        public String getAWSAccessKeyId() {
            String accessKeyId = System.getProperty("aws.accessKeyId");
            if(accessKeyId == null || accessKeyId.isEmpty()) {
                throw new IllegalStateException("Mandatory sys property aws.accessKeyId not specified!");
            }
            return accessKeyId.trim();
        }

        @Override
        public String getAWSSecretKey() {
            String secretKey = System.getProperty("aws.secretKey");
            if(secretKey == null || secretKey.isEmpty()) {
                throw new IllegalStateException("Mandatory sys property aws.secretKey not specified!");
            }
            return secretKey.trim();
        }
        
    }

}
