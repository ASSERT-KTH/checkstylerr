/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of charles nor the names of its
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
 * ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles;

import org.openqa.selenium.WebDriver;

/**
 * An abstract webcrawl - contains the webdriver and other common data of each
 * crawl.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: fb2aa957baece698419ba185c90f745ebcea39ac $
 * @since 1.0.0
 *
 */
abstract class AbstractWebCrawl implements WebCrawl {

    /**
     * WebDriver.
     */
    private final WebDriver driver;

    /**
     * Ignored pages patterns.
     */
    private final IgnoredPatterns ignoredLinks;

    /**
     * Repo to export the pages to.
     */
    private final Repository repo;

    /**
     * Pages are crawled and exported in batches in order to avoid flooding
     * the memory if there are many pages on a website. Default value is 100.
     */
    private final int batchSize;

    /**
     * Ctor.
     * @param webd Selenium WebDriver.
     * @param igp Ignored patterns.
     * @param repo Repository to export the crawled pages into.
     * @param batch Size of a crawl batch.
     * @checkstyle ParameterNumber (6 lines)
     */
    public AbstractWebCrawl(
        final WebDriver webd, final IgnoredPatterns igp,
        final Repository repo, final int batch
    ) {
        this.driver = webd;
        this.ignoredLinks = igp;
        this.repo = repo;
        this.batchSize = batch;
    }

    @Override
    public abstract void crawl() throws DataExportException;

    /**
     * Fetch the used WebSriver.
     * @return driver Webdriver of this crawl
     */
    public final WebDriver driver() {
        return this.driver;
    }

    /**
     * Fetch the used Repository.
     * @return repo Repository where the pages are sent
     */
    public final Repository repo() {
        return this.repo;
    }

    /**
     * Fetch the ignored links patterns.
     * @return ignoredLinks IgnoredPatterns of this crawl
     */
    public final IgnoredPatterns ignoredPatterns() {
        return this.ignoredLinks;
    }
    
    /**
     * Batch size. How many pages will be crawled at once?
     * @return Integer batch size.
     */
    public final int batchSize() {
        return this.batchSize;
    }
}
