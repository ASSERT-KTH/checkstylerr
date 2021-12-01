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

import com.amihaiemil.charles.aws.AmazonEsRepository;

/**
 * Step that deletes the index from AWS es.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version  $Id: 8b63173f9e97f6539a8c38a805883751e7ff5aa4 $
 * @since 1.0.0
 *
 */
public class DeleteIndex  extends IntermediaryStep {

    /**
     * Command.
     */
    private Command com;

    /**
     * Action's logger.
     */
    private Logger logger;

    /**
     * Constructor.
     * @param com Command
     * @param logger The action's logger
     * @param next The next step to take
     */
    public DeleteIndex(Command com, Logger logger, Step next) {
        super(next);
        this.com = com;
        this.logger = logger;
    }
    
    @Override
    public void perform() {
        this.logger.info("Starting index deletion...");
        try {
            new AmazonEsRepository(com.indexName()).delete();
        } catch (IOException e) {
            logger.error("Exception while deleting the index!", e);
            throw new IllegalStateException("Exception while deleting the index!" , e);
        }
        this.logger.info("Index successfully deleted!");
        this.next().perform();
    }

}
