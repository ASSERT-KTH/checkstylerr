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
import java.util.Arrays;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.LiveWebPage;
import com.amihaiemil.charles.SnapshotWebPage;
import com.amihaiemil.charles.WebPage;
import com.amihaiemil.charles.aws.AmazonElasticSearch;

/**
 * Step to index a single page.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 3f53255b5c21acf6d08a4b1839c901d3e692c0aa $
 * @since 1.0.0
 *
 */
public class IndexPage extends IndexStep {

    /**
     * Ctor.
     * @param next Next step to take.
     */
    public IndexPage(Step next) {
        super(next);
    }

    @Override
    public void perform(Command command, Logger logger) throws IOException {
         String link = this.getLink(command);
         logger.info("Indexing page " + link + " ...");
         try {
             final String specified = command.repo().charlesYml().driver();
             logger.info("Crawling with the " + specified + " driver.");
             final WebDriver driver;
             if("phantomjs".equalsIgnoreCase(specified)) {
                 driver = this.phantomJsDriver();
             } else {
                 driver = this.chromeDriver();
             }
             logger.info("Crawling the page...");
             driver.get(link);
             WebPage snapshot = new SnapshotWebPage(new LiveWebPage(driver));
             logger.info("Page crawled. Sending to aws...");
             new AmazonElasticSearch(
                 command.indexName()
             ).export(Arrays.asList(snapshot));
             logger.info("Page successfully sent to aws!");
        } catch (
            final DataExportException | RuntimeException e
        ) {
            logger.error("Exception while indexing the page " + link, e);
            throw new IllegalStateException(
                "Exception while indexing the page" + link, e
            );
        }
        this.next().perform(command, logger);
    }

    /**
     * Get the page's link from the command's text which should be in markdown format, with a
     * link like [this](http://link.com/here/the/ling) .
     * @return String link.
     */
    private String getLink(Command command) {
        String body = command.json().getString("body");
        return body.substring(body.indexOf('(') + 1,  body.indexOf(')'));
    }

}
