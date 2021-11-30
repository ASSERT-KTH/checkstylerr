/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.ejb;

import org.glassfish.ejb.api.EjbContainerServices;
import org.jboss.weld.ejb.api.SessionObjectReference;

/**
 */
public class SessionObjectReferenceImpl implements SessionObjectReference {
    /**
     *
     */
    private static final long serialVersionUID = 731211515531676936L;
    private EjbContainerServices ejbContainerServices;
    private Object ejbRef;

    public SessionObjectReferenceImpl(EjbContainerServices s, Object ref) {
        ejbContainerServices = s;
        ejbRef = ref;
    }

    @Override
    public <S> S getBusinessObject(java.lang.Class<S> sClass) {

        return ejbContainerServices.getBusinessObject(ejbRef, sClass);

    }

    @Override
    public void remove() {

        ejbContainerServices.remove(ejbRef);

    }

    @Override
    public boolean isRemoved() {
        return ejbContainerServices.isRemoved(ejbRef);
    }

}
