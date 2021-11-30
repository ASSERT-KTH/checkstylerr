/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.api;

import java.security.SecureRandom;
import org.jvnet.hk2.annotations.Contract;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
/**
 * This class tries to avoid the need for orb-iiop to
 * depend on some security modules for getting SSL related info
 * @author Kumar
 */
@Contract
public interface IIOPSSLUtil {
    public Object getAppClientSSL();
    public void setAppClientSSL(Object ssl);
    public KeyManager[] getKeyManagers(String certNickname);
    public TrustManager[] getTrustManagers();
    public SecureRandom getInitializedSecureRandom();
    public Object getSSLPortsAsSocketInfo(Object ior);
    public TaggedComponent createSSLTaggedComponent(IORInfo iorInfo, Object socketInfos);

}
