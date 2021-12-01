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
 * A user's favorites. This isn't the public endpoint, since it isn't
 * paginated. The public one will be {@link FavoritesPage}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 241ca26a657f70fc1f3f0edd569e7e6af2f6f100 $
 * @since 1.0.0
 * @todo #51:30min/DEV Implement and unit-test FavoritesPage (see how
 *  CommentsPage is done).
 */
interface Favorites {
    
    /**
     * Fetch the list of favorites from a given page.
     * @param page Page.
     * @return List of Favorite.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    List<Favorite> fetch(final int page) throws IOException;
    
    /**
     * Fetch informations about a given page.
     * @param page Page number.
     * @return Paging.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    Paging paging(final int page) throws IOException;
    
    /**
     * Fetch the user data.
     * @return User data.
     * @throws IOException If there is something wrong with the HTTP call.
     */
    UserData userData() throws IOException;
}
