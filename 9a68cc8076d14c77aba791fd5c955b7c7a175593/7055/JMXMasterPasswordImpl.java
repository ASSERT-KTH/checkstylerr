/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver.ssl;

import com.sun.enterprise.security.store.IdentityManagement;
import com.sun.enterprise.security.store.PasswordAdapter;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.glassfish.security.common.MasterPassword;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Created by IntelliJ IDEA.
 * User: naman
 * Date: 13 Jan, 2011
 * Time: 11:46:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="JMX SSL Password Provider Service")
@Singleton
public class JMXMasterPasswordImpl implements MasterPassword {

    @Inject @Optional IdentityManagement idm;

    @Override
    public PasswordAdapter getMasterPasswordAdapter()
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        char pw[] = idm == null ? null : idm.getMasterPassword();
        return new PasswordAdapter(pw);
    }

    public char[] getMasterPassword() {
        return idm == null ? null : idm.getMasterPassword();
    }
}
