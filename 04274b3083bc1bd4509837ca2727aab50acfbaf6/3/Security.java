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
 * VersionEye Security API. It only offers search after the programming
 * language. If you are interested to check if a certain project has
 * vulnerabilities, you can do that via <b>Product#vulnerabilities()</b>.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: e90ad6bd416587cf3b27f8fd53e11009bead1dd1 $
 * @since 1.0.0
 * @todo #93:30min/DEV Implement a way of fetching the vulnerabilities for a
 *  given Product. This is pending the implementation of the Products/Product
 *  Search API.
 */
public interface Security {

    /**
     * Fetch the vulnerabilities from a given page.
     * @param page Page number.
     * @return List of vulnerabilities.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    List<Vulnerability> vulnerabilities(final int page) throws IOException;

    /**
     * Fetch informations about a given page.
     * @param page Page number.
     * @return Paging.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    Paging paging(final int page) throws IOException;

    /**
     * Paginated vulnerabilities.
     * @return Page which can be iterated,
     *  each element representing a page of vulnerabilities.
     */
    Page<Vulnerability> paginated();
}
