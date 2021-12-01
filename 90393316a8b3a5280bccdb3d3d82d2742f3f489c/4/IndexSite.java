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

import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.GraphCrawl;
import com.amihaiemil.charles.IgnoredPatterns;
import com.amihaiemil.charles.RetriableCrawl;
import com.amihaiemil.charles.WebCrawl;
import com.amihaiemil.charles.aws.AmazonEsRepository;

/**
 * Step to index a website.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: d7302d7310f83fcbe8f3d8dbefccdc1d682745df $
 * @since 1.0.0
 *
 */
public class IndexSite extends IndexStep {

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
    public IndexSite(Command com, Logger logger, Step next) {
        super(next);
        this.com = com;
        this.logger = logger;
    }

    @Override
    public void perform() {
        try {
        	logger.info("Starting to index the whole site...");
            this.graphCrawl().crawl();
        	logger.info("Indexing finished successfully!");
        } catch (
            DataExportException |
            IOException |
            RuntimeException e 
        ) {
            logger.error("Exception while indexing the website!", e);
            throw new IllegalStateException("Exception while indexing the website", e);
        }
        this.next().perform();
    }

    /**
     * Builds a retriable graph crawl.
     * @return RetriableWebCrawl
     * @throws IOException
     */
    public WebCrawl graphCrawl() throws IOException {
        String repoName = this.com.repo().name();
        String siteIndexUrl;
        if(this.com.repo().hasGhPagesBranch()) {
            siteIndexUrl = "http://" + this.com.repo().ownerLogin() + ".github.io/" + repoName;
        } else {
            siteIndexUrl = "http://" + repoName;
        }
        logger.info("Graph-crawling, starting from " + siteIndexUrl
        + " .The website will be crawled as a graph, going in-depth from the index page.");
        WebCrawl siteCrawl = new GraphCrawl(
            siteIndexUrl, this.phantomJsDriver(), new IgnoredPatterns(),
            new AmazonEsRepository(this.com.indexName()), 20
        );
        return new RetriableCrawl(siteCrawl, 5);
    }

}
