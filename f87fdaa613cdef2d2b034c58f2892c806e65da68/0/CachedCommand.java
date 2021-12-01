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
package com.amihaiemil.charles.github;

import java.io.IOException;

/**
 * A command with cached values, to avoid many round-trips to the Github server.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: d46b55f80045de6571d720b64b749c4658630bb9 $
 * @since 1.0.1
 */
public final class CachedCommand extends Command {

    /**
     * Cached agentLogin.
     */
    private String agentLogin;

    /**
     * Cached author email address.
     */
    private String authorEmail;
    
    /**
     * Cached command.
     */
    private Command cached;

    /**
     * Ctor.
     * @param com Command with some values cached.
     */
    public CachedCommand(Command com) {
        super(com.issue(), com.json());
        this.cached = com;
    }

    @Override
    public String agentLogin() throws IOException {
        if(this.agentLogin == null) {
            this.agentLogin = this.cached.agentLogin();
        }
        return this.agentLogin;
    }

    @Override
    public String authorEmail() throws IOException {
        if(this.authorEmail == null) {
            this.authorEmail = this.cached.authorEmail();
        }
        return this.authorEmail;
    }
}
