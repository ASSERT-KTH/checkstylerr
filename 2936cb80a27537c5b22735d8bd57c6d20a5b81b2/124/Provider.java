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

/**
 * A Provider is a platform against which the User is authenticated and
 * which holds the User's repos (Github, Gitlab, Bitbucket etc).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Provider {

    /**
     * Name of this provider.
     * @return String.
     */
    String name();

    /**
     * Get a Repo.
     * @param owner Login of the User owner or Organization name.
     * @param name Simple name of the repo.
     * @return Repo.
     */
    Repo repo(final String owner, final String name);

    /**
     * Get the invitations for the authenticated user.
     * @return Invitations.
     */
    Invitations invitations();

    /**
     * Get the organizations of which the authenticated User has admin rights.
     * @return Organizations.
     */
    Organizations organizations();

    /**
     * The authenticated User will follow the given provider user.
     * @param username Username of the user to follow.
     * @return True if successful, false otherwise.
     */
    boolean follow(final String username);

    /**
     * Return a Provider which has an access token.
     * @param accessToken Access token to make authorized requests with.
     * @return Provider.
     */
    Provider withToken(final String accessToken);

    /**
     * Names of possible providers.
     */
    final class Names {
        /**
         * Hidden ctor.
         */
        private Names(){ }

        /**
         * Github provider.
         */
        public static final String GITHUB = "github";

        /**
         * Gitlab provider.
         */
        public static final String GITLAB = "gitlab";

        /**
         * Bitbucket provider.
         */
        public static final String BITBUCKET = "bitbucket";
    }
}
