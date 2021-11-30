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
 * Volumes API.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @author Boris Kuzmic (boris.kuzmic@gmail.com)
 * @version $Id: c39eca1d1fe5f0bdf4a85a47fe925ecfe0c4de25 $
 * @since 0.0.1
 */
public interface Volumes extends Iterable<Volume> {

    /**
     * Create a volume.
     * @param name Name of the volume.
     * @throws IOException If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     * @return The created volume.
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/VolumeCreate">Create a volume</a>
     */
    Volume create(final String name)
        throws IOException, UnexpectedResponseException;

    /**
     * Create a volume with options.
     * @param name Name of the volume.
     * @param parameters Volume parameters (driver, driver options, labels).
     * @throws IOException If something goes wrong.
     * @throws UnexpectedResponseException If the status response is not
     *  the expected one (200 OK).
     * @return The created volume.
     * @see <a href="https://docs.docker.com/engine/api/v1.35/#operation/VolumeCreate">Create a volume</a>
     */
    Volume create(final String name, final JsonObject parameters)
        throws IOException, UnexpectedResponseException;

    /**
     * Deletes unused volumes.
     * @throws IOException If an I/O error occurs.
     * @throws UnexpectedResponseException If the API responds with an
     *  unexpected status.
     */
    void prune() throws IOException, UnexpectedResponseException;

    /**
     * Return the Docker engine where these Images came from.
     * @return Docker.
     */
    Docker docker();

}
