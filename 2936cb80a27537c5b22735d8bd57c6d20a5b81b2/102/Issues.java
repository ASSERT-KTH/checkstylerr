/**
 * Copyright (c) 2020-2021, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.api;

import javax.json.JsonObject;

/**
 * Issues in a repository.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Issues extends Iterable<Issue> {

    /**
     * Get an Issue.
     * @param issueId Issue's ID.
     * @return Issue or null if it's not found.
     */
    Issue getById(final String issueId);

    /**
     * Get an Issue from an existing JsonObject which
     * Self may receive as part of an event sent
     * by the Provider.
     * @param issue The issue's JSON representation.
     * @return Issue.
     */
    Issue received(final JsonObject issue);

    /**
     * Open a new Issue.
     * @param title Title of the Issue.
     * @param body Text body.
     * @param labels Labels to attach to the Issue.
     * @return The opened Issue.
     */
    Issue open(
        final String title,
        final String body,
        final String... labels
    );

    /**
     * Search some issues after text and labels.
     * @param text Search text.
     * @param labels Labels that the issue should have.
     * @return Issues.
     */
    Issues search(final String text, final String... labels);
}
