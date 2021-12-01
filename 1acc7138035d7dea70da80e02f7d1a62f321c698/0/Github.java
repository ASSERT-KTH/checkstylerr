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
import java.util.List;

/**
 * VersionEye Github API. 
 * @author Sherif Waly (sherifwaly95@gmail.com)
 * @version $Id: f43326521e715fba1d3a1087142d0da0f4993d31 $
 * @since 1.0.0
 * @todo #81:30min/DEV Complete Github API methods and move fetch/paging to a
 *  repositories interface. You can also filter repositories
 *  by orga_name, prog. language, etc.
 */
public interface Github {

    /**
     * Fetch the repositories from a given page.
     * @param page Page number.
     * @return List of Repository.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    List<Repository> fetch(final int page) throws IOException;
    
    /**
     * Fetch informations about given page.
     * @param page Page number.
     * @return Paging.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    Paging paging(final int page) throws IOException;
    
    /**
     * Paginated repositories.
     * @return Page which can be iterated,
     *  each element representing a page of repositories.
     */
    Page<Repository> paginated();
    
    /**
     * Re-imports all github repositories.
     * @return String "running" or "done".
     * @throws IOException If there is something wrong with the HTTP call.
     */
    String sync() throws IOException;
}
