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
import com.amihaiemil.charles.RetriableCrawl;
import com.amihaiemil.charles.SitemapXmlCrawl;
import com.amihaiemil.charles.WebCrawl;
import com.amihaiemil.charles.aws.AmazonEsRepository;
import com.amihaiemil.charles.sitemap.SitemapXmlOnline;

/**
 * Step to index a website represented by a sitemap.xml file.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 53543f5687baef7d53131ed9c1459ad8c73cb49f $
 * @since 1.0.0
 *
 */
public class IndexSitemap extends IndexStep {

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
    public IndexSitemap(Command com, Logger logger, Step next) {
        super(next);
        this.com = com;
        this.logger = logger;
    }

	@Override
	public void perform() {
		String link = this.getLink();
        try {
        	logger.info("Indexing sitemap " + link + " ...");
            WebCrawl sitemap = new RetriableCrawl(
                new SitemapXmlCrawl(
                    this.phantomJsDriver(),
                    new SitemapXmlOnline(link),
                    new AmazonEsRepository(this.com.indexName()),
                    20
                ),
                5
            );
            sitemap.crawl();
            logger.info("Sitemap indexed successfully!");
       } catch (
           final DataExportException | IOException | RuntimeException e
       ) {
           logger.error("Exception while indexing the page " + link, e);
           throw new IllegalStateException(
               "Exception while indexing the page" + link, e
           );
       }
       this.next().perform();
	}

	/**
     * Get the sitemap's link from the command's text which should
     * be in markdown format, with a link like
     * [this](http://link.com/here/the/ling) .
     * @return String link.
     */
    private String getLink() {
        String body = this.com.json().getString("body");
        return body.substring(body.indexOf('(') + 1,  body.indexOf(')'));
    }
}
