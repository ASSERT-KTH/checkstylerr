/**
 * Copyright (c) 2020-2021, Self XDSD Contributors
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 *
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

import java.time.LocalDateTime;

/**
 * Tokens used to access Self XDSD's RESTful API.
 *
 * @author Andrei Osipov (andreoss@sdf.org)
 * @version $Id$
 * @since 0.0.61
 */
public interface ApiTokens extends Iterable<ApiToken> {

    /**
     * Get an ApiToken by its ID (which is its actual value).
     *
     * @param token String token.
     * @return ApiToken or null if it's not found.
     */
    ApiToken getById(final String token);

    /**
     * API tokens of a user.
     *
     * @param user User.
     * @return ApiTokens.
     */
    ApiTokens ofUser(final User user);

    /**
     * Removes an ApiToken from storage.
     * @param token ApiToken.
     * @return True if token was successfully removed.
     */
    boolean remove(final ApiToken token);

    /**
     * Register a token.
     *
     * @param name Token's name.
     * @param token Token's value.
     * @param expiration Expiration date.
     * @param user User of a token.
     * @return Registered ApiToken.
     */
    ApiToken register(
        final String name,
        final String token,
        final LocalDateTime expiration,
        final User user
    );
}
