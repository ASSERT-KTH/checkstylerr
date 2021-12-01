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

import java.io.IOException;


/**
 * Steps taken to fulfill a command.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 39cca30254cd3103c72c46828457c4cafcd17a83 $
 * @since 1.0.0
 */
public final class StepsTree implements Steps {

    /**
     * Steps to be performed.
     */
    private Step steps;
    
    /**
     * Initial command.
     */
    private Command com;

    /**
     * Message to send in case some step fails.
     */
    private SendReply failureMessage;

    /**
     * Constructor.
     * @param steps Given steps.
     * @param command Command which triggered the action.
     * @param logs Logs' location.
     */
    public StepsTree(Step steps, Command command, LogsLocation logs) {
        this(
            steps, command,
            new SendReply(
		        new TextReply(
		            command,
		            String.format(
		            	command.language().response("step.failure.comment"),
		                command.authorLogin(), logs.address()
		            )
		        ),
		        new Step.FinalStep("[ERROR] Some step didn't execute properly.")
            )
        );
    }
    
    /**
     * Constructor.
     * @param steps Given steps.
     * @param command Command which triggered the action.
     * @param fm Failure message.
     */
    public StepsTree(Step steps, Command command, SendReply fm) {
        this.steps = steps;
        this.com = command;
        this.failureMessage = fm;
    }
    
    /**
     * Perform all the given steps.
     * @param logger Action logger.
     * @throws IOException If something goes wrong.
     */
    @Override
    public void perform(Logger logger) throws IOException {
        try {
        	String commandBody = this.com.json().getString("body");
            logger.info("Received command: " + commandBody);
            this.steps.perform(this.com, logger);
        } catch (Exception ex) {
            logger.error("An exception occured, sending failure comment...", ex);
            this.failureMessage.perform(this.com, logger);
        }
    }

}
