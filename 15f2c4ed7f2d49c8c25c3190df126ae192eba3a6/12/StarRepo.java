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

import org.slf4j.Logger;

import com.jcabi.github.Repo;

/**
 * A Github-hosted site repository has to be starred once indexed.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 397bdac78929557968fb4f96a4ee0a3e14322221 $
 * @since 1.0.0
 *
 */
public class StarRepo extends IntermediaryStep {

    /**
     * Repository to be starred.
     */
    private Repo repo;

    /**
     * Action logger.
     */
    private Logger logger;
    
    /**
     * Constructor.
     * @param repo Given repo.
     * @param logger Action's logger.
     * @param next Next step to perform.
     */
    public StarRepo(Repo repo, Logger logger, Step next) {
        super(next);
        this.repo = repo;
        this.logger = logger;
    }
    
    /**
     * Star the repository.
     * @return Always returns true, since it's not a critical step.
     */
    public void perform() {
        try {
            this.logger.info("Starring repository...");
            if(!this.repo.stars().starred()) {
                this.repo.stars().star();
            }
            this.logger.info("Repository starred!");
        } catch (IOException e) {
            this.logger.error("Error when starring repository: " + e.getMessage(), e);
            //We do not throw IllegalStateException here since starring the repo is not
            //a critical matter
        }
        this.next().perform();
    }
}
