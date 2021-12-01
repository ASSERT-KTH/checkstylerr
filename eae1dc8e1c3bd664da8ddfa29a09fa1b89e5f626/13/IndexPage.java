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
import com.amihaiemil.charles.aws.AmazonEsRepository;
import com.amihaiemil.charles.aws.StAccessKeyId;
import com.amihaiemil.charles.aws.StEsEndPoint;
import com.amihaiemil.charles.aws.StRegion;
import com.amihaiemil.charles.aws.StSecretKey;

/**
 * Step to index a single page.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: f9d8f0d512754003564e1bccb4baa5d67bd94620 $
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
             logger.info("Crawling the page...");
             WebDriver driver = this.phantomJsDriver();
             driver.get(link);
             WebPage snapshot = new SnapshotWebPage(new LiveWebPage(driver));
             logger.info("Page crawled. Sending to aws...");
             new AmazonEsRepository(
                 command.indexName(),
                 new StAccessKeyId(),
                 new StSecretKey(),
                 new StRegion(),
                 new StEsEndPoint()
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
