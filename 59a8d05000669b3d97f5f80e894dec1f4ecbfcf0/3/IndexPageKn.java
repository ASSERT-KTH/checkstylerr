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
 * The bot knows how to index a single page from the website.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 0b1c0a024656c96f0a3b49026a94339e701a6586 $
 * @since 1.0.1
 */
public final class IndexPageKn implements Knowledge {

    /**
     * Location of the log file.
     */
    private LogsLocation logsLoc;

    /**
     * What do we do if it's not an 'indexpage' command?
     */
    private Knowledge notIdxPage;

    /**
     * Ctor.
     * @param logsLoc Location of the log file for the bot's action.
     * @param notIdxPage What do we do if it's not an 'indexpage' command?
     */
    public IndexPageKn(final LogsLocation logsLoc, final Knowledge notIdxPage) {
        this.logsLoc = logsLoc;
        this.notIdxPage = notIdxPage;
    }

    @Override
    public Steps handle(final Command com) throws IOException {
        if("indexpage".equalsIgnoreCase(com.type())) {
            return new StepsTree(
            	new PageHostedOnGithubCheck(
	                new GeneralPreconditionsCheck(
	                    new SendReply(
	                        new TextReply(
	                            com,
	                            String.format(
	                                com.language().response("index.start.comment"),
	                                com.authorLogin(),
	                                this.logsLoc.address()
	                            )
	                        ),
	                        new IndexPage(
	                            new StarRepo(
	                                new SendReply(
	                                    new TextReply(
	                                        com,
	                                        String.format(
	                                            com.language().response("index.finished.comment"),
	                                            com.authorLogin(), this.logsLoc.address()
	                                        )
	                                    ),
	                                    new Follow(new Tweet(new Step.FinalStep()))
	                                )
	                            )
	                        )
	                    )
	                ),
	                new SendReply(
	                    new TextReply(
	                        com,
	                        String.format(
	                            com.language().response("denied.badlink.comment"),
	                            com.authorLogin()
	                        )
	                    ),
	                    new Step.FinalStep()
	                )
                ),
                com,
                this.logsLoc
            );
        }
        return this.notIdxPage.handle(com);
    }

}
