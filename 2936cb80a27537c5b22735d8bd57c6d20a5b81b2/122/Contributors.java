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

import com.selfxdsd.api.storage.Paged;

/**
 * Contributors in Self.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Contributors extends Iterable<Contributor>, Paged {

    /**
     * Register a new contributor in Self.
     * @param username Username.
     * @param provider Password.
     * @return Contributor.
     */
    Contributor register(final String username, final String provider);

    /**
     * Get a Contributor by his id.
     * @param username Username.
     * @param provider Provider name.
     * @return Contributor or null if not found.
     */
    Contributor getById(final String username, final String provider);

    /**
     * Get the Contributors of a given Project.
     * @param repoFullName Full name of the Repo that the Project represents.
     * @param repoProvider Provider of the Repo that the Project represents.
     * @return Contributors.
     */
    Contributors ofProject(
        final String repoFullName,
        final String repoProvider
    );

    /**
     * Get the Contributors registered with provider (Github, Gitlab etc).
     * @param provider Provider
     * @return Contributors.
     */
    Contributors ofProvider(final String provider);

    /**
     * Get the Contributors at the provided Page.
     * @param page Page number.
     * @return Contributors in a page.
     */
    Contributors page(final Paged.Page page);

    /**
     * Elect a Contributor for the given task.
     * @param task Task that requires a new assignee.
     * @return Contributor or null if none is found.
     */
    Contributor elect(final Task task);

}
