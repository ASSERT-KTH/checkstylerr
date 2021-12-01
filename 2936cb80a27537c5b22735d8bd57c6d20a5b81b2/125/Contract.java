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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A collaboration Contract between a Project and a Contributor.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Contract {

    /**
     * This contract's ID.
     * @return Contract ID.
     */
    Contract.Id contractId();

    /**
     * The Project.
     * @return Project.
     */
    Project project();

    /**
     * The Contributor.
     * @return Contributor.
     */
    Contributor contributor();

    /**
     * The Contributor's hourly rate in cents.
     * @return BigDecimal.
     */
    BigDecimal hourlyRate();

    /**
     * The Contributor's role (DEV, QA, ARCH etc).
     * @return String.
     */
    String role();

    /**
     * Invoices for this contract, active or inactive.
     * <br>
     * Note that a contract must have at most one active Invoice.
     * @return Iterable of Invoice.
     */
    Invoices invoices();

    /**
     * The tasks assigned to the Contract.
     * @return Tasks.
     */
    Tasks tasks();

    /**
     * This contract's current value. It's the sum of
     * the active tasks' value and the value of the active
     * Invoice, as well as the value of PM's commission.
     * @return Value.
     */
    BigDecimal value();

    /**
     * Revenue. This is the total potential earnings of the Contributor.
     * It's the sum of the active tasks' value and the value of the active
     * Invoice (it's the Contract's value without the PM's commission).
     * @return Revenue.
     */
    BigDecimal revenue();

    /**
     * Time when this Contract has been marked from removal.<br><br>
     * If the contract is marked for removal, no more tasks will be assigned
     * to it and it will be removed after a certain period of time.
     * @return LocalDateTime or null if it's not set.
     */
    LocalDateTime markedForRemoval();

    /**
     * Update the contract's hourly rate.
     * @param hourlyRate New hourly rate.
     * @return Updated contract.
     */
    Contract update(final BigDecimal hourlyRate);

    /**
     * Mark this Contract for removal.
     * @return Contract marked for removal (it will have
     *  the "markedForRemoval" attribute set).
     */
    Contract markForRemoval();

    /**
     * Restore this Contract (remove the markedForRemoval flag).
     * @return Contract restored (it will have the "markedForRemoval"
     *  set to null).
     */
    Contract restore();

    /**
     * Completely remove this Contract. In order for it to work,
     * the Contract has to have been marked for removal more than
     * 30 days ago (markedForRemoval != null and markedForRemoval gt 30 days).
     */
    void remove();

    /**
     * Possible roles in a Contract.
     */
    class Roles {

        /**
         * Hidden ctor.
         */
        private Roles(){ }

        /**
         * Constant for the architect role.
         */
        public static final String ARCH = "ARCH";

        /**
         * Constant for the developer role.
         */
        public static final String DEV = "DEV";

        /**
         * Constant for the reviewer role.
         */
        public static final  String REV = "REV";

        /**
         * Constant for the QA role.
         */
        public static final String QA = "QA";

        /**
         * Constant for the chatbot role.
         */
        public static final String BOT = "BOT";

        /**
         * Constant for the Project Manager role.
         */
        public static final String PM = "PM";

        /**
         * Constant for the Project Owner role.
         */
        public static final String PO = "PO";

        /**
         * Any role.
         */
        public static final String ANY = "ANY";
    }

    /**
     * Contract's compose id.
     */
    class Id {

        /**
         * Full name of the Repo represented by the Project.
         */
        private final String repoFullName;
        /**
         * Contributor's username.
         */
        private final String contributorUsername;
        /**
         * Contributor/Project's provider.
         */
        private final String provider;
        /**
         * Contributor's role.
         */
        private final String role;

        /**
         * Constructor.
         *
         * @param repoFullName Fullname of the Repo represented by the project.
         * @param contributorUsername Contributor's username.
         * @param provider Contributor/Project's provider.
         * @param role Contributor's role.
         */
        public Id(
            final String repoFullName,
            final String contributorUsername,
            final String provider,
            final String role
        ) {
            this.repoFullName = repoFullName;
            this.contributorUsername = contributorUsername;
            this.provider = provider;
            this.role = role;
        }

        /**
         * Full name of the Repo represented by the Project.
         * @return String
         */
        public String getRepoFullName() {
            return repoFullName;
        }

        /**
         * Contributor's username.
         * @return String
         */
        public String getContributorUsername() {
            return contributorUsername;
        }

        /**
         * Contributor/Project's provider.
         * @return String
         */
        public String getProvider() {
            return provider;
        }

        /**
         * Contributor's role.
         * @return String
         */
        public String getRole() {
            return role;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            final Id id = (Id) object;
            //@checkstyle LineLength (5 lines)
            return this.repoFullName.equalsIgnoreCase(id.repoFullName)
                && this.contributorUsername.equalsIgnoreCase(id.contributorUsername)
                && this.provider.equalsIgnoreCase(id.provider)
                && this.role.equalsIgnoreCase(id.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                this.repoFullName,
                this.contributorUsername,
                this.provider,
                this.role
            );
        }

        @Override
        public String toString() {
            return "[" + this.contributorUsername + "-" + this.role
                + "-" + this.repoFullName + "-" + this.provider + "]";
        }

    }
}
