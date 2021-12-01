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

import org.slf4j.Logger;

/**
 * One step that has to be performed to
 * fulfill a command/request.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 7deb7a409fa174969b0a2bfdd41bee9ac4b0c42b $
 * @since 1.0.0
 * 
 */
public interface Step {
    void perform();
    
    /**
     * Final step of the action. Just logs a line
     * saying the action was successful.
     */
    public final static class FinalStep implements Step {

        /**
         * Action's logger.
         */
        private Logger logger;

        /**
         * Message to log
         */
        private String message;
        
        /**
         * Ctor without message for successful ending.
         * @param logger Logger
         */
        public FinalStep(Logger logger) {
            this(logger, "Finished action successfully!");
        }
        
        /**
         * Ctor.
         * @param logger Logger.
         * @param message Message to log at the end of the action.
         */
        public FinalStep(Logger logger, String message) {
            this.logger = logger;
            this.message = message;
        }

        public void perform() {
            this.logger.info(message);
        }
    }

    /**
     * Fake for unit tests.
     */
    public final static class Fake implements Step {

        /**
         * Should this step's perform pass or throw an exception?
         */
        private boolean pass;

        /**
         * Ctor.
         * @param pass Should this step pass or throw an exception?
         */
        public Fake(boolean pass) {
            this.pass = pass;
        }

        @Override
        public void perform() {
            if(!pass) {
                throw new IllegalStateException("Step should not have been executed!");
            }
            
        }
    }
}
