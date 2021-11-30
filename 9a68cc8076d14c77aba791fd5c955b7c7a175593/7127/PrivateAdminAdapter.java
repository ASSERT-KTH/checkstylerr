/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import org.glassfish.internal.api.Privacy;
import org.glassfish.internal.api.Private;
import org.glassfish.internal.api.Visibility;
import org.glassfish.api.admin.AdminCommand;
import org.jvnet.hk2.annotations.Service;

/**
 * Admin adapter for private glassfish commands.
 *
 * @author Jerome Dochez
 */
@Service
public class PrivateAdminAdapter extends AdminAdapter {

    public final static String VS_NAME="__private_asadmin";
    public final static String PREFIX_URI = "/" + VS_NAME;

    public PrivateAdminAdapter() {
        super(Private.class);
    }

    protected boolean validatePrivacy(AdminCommand command) {
        Visibility visibility = command.getClass().getAnnotation(Visibility.class);
        return (visibility==null?false:visibility.value().equals(Private.class));
    }

    public String getContextRoot() {
        return PREFIX_URI;
    }
}
