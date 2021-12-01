/**
 * Copyright (c) 2017, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.versioneye;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;

import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;

/**
 * Real implementation of {@link Security}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: e3340c527ef4913339461728c6fd10755b67d427 $
 * @since 1.0.0
 *
 */
final class RtSecurity implements Security {

    /**
     * HTTP request.
     */
    private Request req;

    /**
     * Programming language filter criterion.
     */
    private String language;
    
    /**
     * Product key.
     */
    private String prodKey;

    /**
     * Ctor.
     * @param entry HTTP Request.
     * @param language Programming language filter criterion.
     */
    RtSecurity(final Request entry, final String language) {
        this(entry, language, "");
    }

    /**
     * Ctor.
     * @param entry HTTP Request.
     * @param language Programming language filter criterion.
     * @param prodKey Product key filter criterion.
     */
    RtSecurity(
        final Request entry, final String language,
        final String prodKey
    ) {
        this.req = entry.uri().path("/security").back();
        this.language = language;
        this.prodKey = prodKey;
    }
    
    @Override
    public List<Vulnerability> vulnerabilities(final int page)
        throws IOException {
        Request request = this.req.uri()
            .queryParam("language", this.language)
            .queryParam("page", String.valueOf(page))
            .back();
        if(!this.prodKey.isEmpty()) {
            request = request.uri()
                .queryParam("prod_Key", this.prodKey)
                .back();
        }
        final JsonArray results = request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class)
            .json()
            .readObject()
            .getJsonArray("results");
        final List<Vulnerability> vulnerabilities = new ArrayList<>();
        for(int idx=0; idx<results.size(); idx++) {
            vulnerabilities.add(
                new JsonVulnerability(results.getJsonObject(idx))
            );
        }
        return vulnerabilities;
    }

    @Override
    public Paging paging(final int page) throws IOException {
        return new JsonPaging(
            this.req.uri()
                .queryParam("page", String.valueOf(page)).back().fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json()
                .readObject()
                .getJsonObject("paging")
        );
    }

    @Override
    public Page<Vulnerability> paginated() {
        return new VulnerabilitiesPage(this);
    }

}
