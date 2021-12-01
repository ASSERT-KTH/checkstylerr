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
package com.amihaiemil.charles.rest;

import java.io.File;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource for fetching the logs of each Action.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: a49a92bb3849e22398f218a5c0f1d8154bb2fd38 $
 * @since 1.0.0
 */
@Path("/logs")
public class LogsResource extends JsonResource {


    public LogsResource() {
        super(
            Json.createObjectBuilder()
                .add("getActionLogs", "/api/logs/{log_file_name}")
                .build()
        );
    }

    /**
     * Fetch the log file of an Action by name.
     * @param name Log file name.
     * @return HTTP Response.
     */
    @Path("/{name}")
    @GET
    public Response getActionLogs(@PathParam("name") String name) {
        String logroot = System.getProperty("LOG_ROOT");
        if(logroot != null) {
            File log = new File(logroot + "/charles-rest/ActionsLogs/" + name);
            if(log.exists()) {
                return Response.ok()
                    .entity(log)
                    .header("Content-Type", "text/plain; charset=UTF-8").build();
            }
        }
        return Response.noContent().build();
    }

    /**
     * This JAX-RS resource in Json format.
     * @return Response.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response json() {
        return Response.ok().entity(this.toString()).build();
    }
}
