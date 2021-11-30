/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.security.jauth;

import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.MessagePolicy.ProtectionPolicy;
import jakarta.security.auth.message.MessagePolicy.TargetPolicy;

/*
 * This class is used to define the message authentication policy that informs
 * the actions of AuthModules.
 *
 * <p> This class is used to define source and recipient authentication
 * policies.  Source authentication is used to establish the identity of
 * either the message sender or the party that established the message contents.
 * Recipient authentication is used to establish the identity of the receiver
 * of the message before it is sent.
 *
 * <p> This class is used used by the AuthConfig class to define the request and
 * response authentication policies associated with Client and Server
 * AuthModules.
 *
 * @version %I%, %G%
 * @see AuthConfig
 * @see ClientAuthModule
 * @see ServerAuthModule
 */

public class AuthPolicy {

    public static final int SOURCE_AUTH_NONE = 0;
    public static final int SOURCE_AUTH_SENDER = 1;
    public static final int SOURCE_AUTH_CONTENT = 2;

    public static final String SENDER = "sender";
    public static final String CONTENT = "content";
    public static final String BEFORE_CONTENT = "before-content";
    public static final String AFTER_CONTENT = "after-content";

    private int authenticateSource = SOURCE_AUTH_NONE;
    private boolean authenticateRecipient = false;
    private boolean recipientBeforeContent = false;

    private void setAuthenticationType(int sourceAuthType) {
        switch (sourceAuthType) {
        case SOURCE_AUTH_NONE:
        case SOURCE_AUTH_SENDER:
        case SOURCE_AUTH_CONTENT:
            this.authenticateSource = sourceAuthType;
            break;
        default:
            break;
        }
    }

    public AuthPolicy() {
    }

    public AuthPolicy(int sourceAuthenticationType, boolean authenticateRecipient, boolean beforeContent) {
        setAuthenticationType(sourceAuthenticationType);
        this.authenticateRecipient = authenticateRecipient;
        this.recipientBeforeContent = beforeContent;
    }

    public AuthPolicy(MessagePolicy messagePolicy) {
        if (messagePolicy != null) {
            TargetPolicy[] targetPolicies = messagePolicy.getTargetPolicies();
            if (targetPolicies != null && targetPolicies.length > 0) {
                int contentInd = -1;
                int recipientInd = -1;
                for (int i = 0; i < targetPolicies.length; i++) {
                    ProtectionPolicy pp = targetPolicies[i].getProtectionPolicy();

                    if (ProtectionPolicy.AUTHENTICATE_RECIPIENT.equals(pp.getID())) {
                        recipientInd = i;
                        this.authenticateRecipient = true;
                    } else if (ProtectionPolicy.AUTHENTICATE_SENDER.equals(pp.getID())) {
                        contentInd = i;
                        setAuthenticationType(SOURCE_AUTH_SENDER);
                    } else if (ProtectionPolicy.AUTHENTICATE_CONTENT.equals(pp.getID())) {
                        contentInd = i;
                        setAuthenticationType(SOURCE_AUTH_CONTENT);
                    }
                }

                if (authenticateRecipient && contentInd >= 0) {
                    this.recipientBeforeContent = recipientInd < contentInd;
                }
            }
        }
    }

    public void setSourceAuth(int sourceAuthenticationType) {
        setAuthenticationType(sourceAuthenticationType);
    }

    /*
     * Set the source of the message content authentication policy.
     *
     * @param required boolean value. When true authentication of the source of the message content is required. When false,
     * content authentication will not be required and if authentication of the message sender is required it will remain
     * so.
     */
    public void setContentAuth(boolean required) {
        if (required) {
            this.setSourceAuth(SOURCE_AUTH_CONTENT);
        } else if (!isSenderAuthRequired()) {
            this.setSourceAuth(SOURCE_AUTH_NONE);
        }
    }

    /*
     * Set the message sender authentication policy.
     *
     * @param required boolean value. When true authentication of the message sender is required. When false, sender
     * authentication will not be required and if authentication of the message content is required it will remain so.
     */
    public void setSenderAuth(boolean required) {
        if (required) {
            this.setSourceAuth(SOURCE_AUTH_SENDER);
        } else if (!isContentAuthRequired()) {
            this.setSourceAuth(SOURCE_AUTH_NONE);
        }
    }

    public void setRecipientAuth(boolean required, boolean beforeContent) {
        this.authenticateRecipient = required;
        this.recipientBeforeContent = beforeContent;
    }

    public int getSourceAuth() {
        return this.authenticateSource;
    }

    public boolean authRequired() {
        return this.isSourceAuthRequired() || this.isRecipientAuthRequired();
    }

    public boolean isSourceAuthRequired() {
        return this.authenticateSource == 0 ? false : true;
    }

    public boolean isSenderAuthRequired() {
        return this.isSourceAuthRequired() ? this.getSourceAuth() == SOURCE_AUTH_SENDER ? true : false : false;
    }

    public boolean isContentAuthRequired() {
        return this.isSourceAuthRequired() ? this.getSourceAuth() == SOURCE_AUTH_CONTENT ? true : false : false;
    }

    public boolean isRecipientAuthRequired() {
        return this.authenticateRecipient;
    }

    // This method interprets order from the perspective of the
    // message sender. The value returned by this method, is only
    // relevant when recipientAuth is required.
    public boolean isRecipientAuthBeforeContent() {
        return this.recipientBeforeContent;
    }

    // When orderForValidation is true, returns true if validator must
    // validate recipient auth (e.g. decrypt) before content auth (e.g. verify
    // signature); in which case msg sender did content auth before recipient auth.
    // Behaves same as noArg variant when orderForValidation is false. In either
    // case, the returned value is only relevant when recipientAuth is required.
    public boolean isRecipientAuthBeforeContent(boolean orderForValidation) {
        return orderForValidation ? !this.recipientBeforeContent : this.recipientBeforeContent;
    }

    @Override
    public String toString() {

        // wait for 1.5
        // StringBuilder sb = new StringBuilder();
        StringBuffer sb = new StringBuffer();
        switch (authenticateSource) {
        case SOURCE_AUTH_NONE:
            sb.append("source-auth-type = SOURCE_AUTH_NONE");
            break;
        case SOURCE_AUTH_SENDER:
            sb.append("source-auth-type = SOURCE_AUTH_SENDER");
            break;
        case SOURCE_AUTH_CONTENT:
            sb.append("source-auth-type = SOURCE_AUTH_CONTENT");
            break;
        default:
            break;
        }

        if (authenticateRecipient) {
            sb.append("\n\tauthenticate-recipient=true" + "\n\tbeforeContent=" + recipientBeforeContent);
        } else {
            sb.append("\n\tauthenticate-recipient=false");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AuthPolicy)) {
            return false;
        }

        AuthPolicy that = (AuthPolicy) o;
        if (this.authenticateSource == that.authenticateSource && this.authenticateRecipient == that.authenticateRecipient
                && this.recipientBeforeContent == that.recipientBeforeContent) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return authenticateSource + (authenticateRecipient ? 5 : 0) + (recipientBeforeContent ? 10 : 0);
    }
}
