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

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;

/**
 * Http request sent to AWS.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: c7add2d9eadd1066a4c56919cda115339385377e $
 * @since 1.0.0
 *
 */
public abstract class AwsHttpRequest<T> {

    /**
     * Perform this request.
     */
    public abstract T perform();

    /**
     * Get the aws base request.
     * @return Request.
     */
    abstract Request<Void> request();

    /**
     * Fake AwsHttpRequest for unit testing.
     * @author Mihai Andronache (amihaiemil@gmail.com)
     * @version $Id: c7add2d9eadd1066a4c56919cda115339385377e $
     * @since 1.0.0
     */
    static class FakeAwsHttpRequest extends AwsHttpRequest<String>{

        private Request<Void> fakeRq = new DefaultRequest<>("fake");

        @Override
        public String perform() {
            return "performed fake request";
        }

        @Override
        public Request<Void> request() {
            return this.fakeRq;
        }

    }
}
