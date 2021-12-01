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
package com.selfxdsd.core;

import com.selfxdsd.api.*;
import com.selfxdsd.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.net.URI;

/**
 * A Commit in Gitlab repo.
 * @author Ali Fellahi (fellahi.ali@gmail.com)
 * @version $Id$
 * @since 0.0.44
 */
final class GitlabCommit implements Commit {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        GitlabCommit.class
    );

    /**
     * Commit base uri.
     */
    private final URI commitUri;

    /**
     * Commit JSON as returned by Gitlab's API.
     */
    private final JsonObject json;

    /**
     * Self storage, in case we want to store something.
     */
    private final Storage storage;

    /**
     * Repo Collaborators.
     */
    private final Collaborators collaborators;

    /**
     * Gitlab's JSON Resources.
     */
    private final JsonResources resources;

    /**
     * Ctor.
     * @param commitUri Commit base URI.
     * @param json Json Commit as returned by Gitlab's API.
     * @param collaborators Repo Collaborators.
     * @param storage Storage.
     * @param resources Gitlab's JSON Resources.
     */
    GitlabCommit(
        final URI commitUri,
        final JsonObject json,
        final Collaborators collaborators,
        final Storage storage,
        final JsonResources resources
    ) {
        this.commitUri = commitUri;
        this.json = json;
        this.collaborators = collaborators;
        this.storage = storage;
        this.resources = resources;
    }


    @Override
    public Comments comments() {
        String commentsUri = this.commitUri.toString();
        if (commentsUri.endsWith("/")) {
            commentsUri += "comments";
        } else {
            commentsUri += "/comments";
        }
        return new GitlabCommitComments(
            URI.create(commentsUri),
            this.resources
        );
    }

    /**
     * {@inheritDoc}
     * <br/>
     * We have to iterate over Collaborators to get the author's userId,
     * because it is not contained in the Commit JSON.
     * More <a href="https://gitlab.com/gitlab-org/gitlab/-/issues/20924">here</a>.
     */
    @Override
    public String author() {
        final String authorName = this.json.getString("author_name", "");
        String author = "";
        for(final Collaborator collaborator : this.collaborators) {
            if(authorName.equalsIgnoreCase(collaborator.name())) {
                author = collaborator.username();
                break;
            }
        }
        return author;
    }

    @Override
    public String shaRef() {
        return this.json.getString("id");
    }

    @Override
    public JsonObject json() {
        return this.json;
    }
}
