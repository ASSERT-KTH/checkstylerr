/**
 * Copyright (c) 2018-2019, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1)Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3)Neither the name of docker-java-api nor the names of its
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
package com.amihaiemil.docker;

import java.io.IOException;
import javax.json.JsonObject;

/**
 * A docker network to which containers can be attached.
 * 
 * @author George Aristy (george.aristy@gmail.com)
 * @version $Id: 43f24dc3d9f4cbd59877610bf2fc750b4413806f $
 * @see <a href="https://docs.docker.com/engine/api/v1.35/#tag/Network">Docker API v1.35</a>
 * @since 0.0.4
 */
public interface Network extends JsonObject {

    /**
     * Return detailed information about this network.
     * @return JsonObject information.
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/NetworkInspect">Inspect a network</a>
     * @throws IOException If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     */
    JsonObject inspect() throws IOException, UnexpectedResponseException;

    /**
     * Remove a network.
     * @throws IOException If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/NetworkDelete">Remove a network</a>
     */
    void remove() throws IOException, UnexpectedResponseException;

    /**
     * Connect container to network.
     * @param containerId The ID or name of the container to connect to the
     *  network.
     * @throws IOException  If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/NetworkConnect">Connect a container to a network</a>
     */
    void connect(final String containerId)
        throws IOException, UnexpectedResponseException;

    /**
     * Disconnect container from network.
     * @param containerId The ID or name of the container to disconnect from the
     *  network.
     * @throws IOException  If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/NetworkDisconnect">Disconnect a container from a network</a>
     */
    void disconnect(final String containerId)
        throws IOException, UnexpectedResponseException;

}
