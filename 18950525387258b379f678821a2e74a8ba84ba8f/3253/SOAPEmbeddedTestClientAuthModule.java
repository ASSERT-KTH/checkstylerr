/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.soapembedded;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.module.ClientAuthModule;
import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;

public class SOAPEmbeddedTestClientAuthModule implements ClientAuthModule {
    private CallbackHandler handler = null;

    public void initialize(MessagePolicy requestPolicy,
               MessagePolicy responsePolicy,
               CallbackHandler handler,
               Map options)
               throws AuthException {
        this.handler = handler;
    }

    public Class[] getSupportedMessageTypes() {
        return new Class[] { SOAPMessage.class };
    }

    public AuthStatus secureRequest(MessageInfo messageInfo,
            Subject clientSubject) throws AuthException {
        SOAPMessage reqMessage = (SOAPMessage)messageInfo.getRequestMessage();
        QName serviceName = (QName)messageInfo.getMap().get(
                jakarta.xml.ws.Endpoint.WSDL_SERVICE);
        System.out.println("serviceName = " + serviceName);
        if (serviceName == null) {
            throw new AuthException("serviceName is null");
        } else if (!(serviceName instanceof javax.xml.namespace.QName)) {
            throw new AuthException("serviceName is not an instanceof javax.xml.namespace.QName");
        }
        try {
            Util.prependSOAPMessage(reqMessage, "SecReq ");
        } catch(Exception ex) {
            AuthException aex = new AuthException();
            aex.initCause(ex);
            throw aex;
        }
        return AuthStatus.SUCCESS;
    }

    public AuthStatus validateResponse(MessageInfo messageInfo,
            Subject clientSubject, Subject serviceSubject)
            throws AuthException {
        SOAPMessage respMessage = (SOAPMessage)messageInfo.getResponseMessage();
        try {
            String value = Util.getValue(respMessage);
            if (value == null || !value.startsWith("SecResp ") ||
                    (value.indexOf("ValReq SecReq ") == -1)) {
                return AuthStatus.FAILURE;
            }
            Util.prependSOAPMessage(respMessage, "ValResp ");
        } catch(Exception ex) {
            AuthException aex = new AuthException();
            aex.initCause(ex);
            throw aex;
        }
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject)
        throws AuthException {
    }
}
