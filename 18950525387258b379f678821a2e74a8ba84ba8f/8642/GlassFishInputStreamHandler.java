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

package com.sun.enterprise.container.common.spi.util;

import java.io.IOException;

import org.jvnet.hk2.annotations.Contract;

/**
 * A class that is used to restore state  during deserialization
 *
 * @author Mahesh Kannan
 */
@Contract
public interface GlassFishInputStreamHandler {

        public static final Object NULL_OBJECT = new Object();

    /**
     * Called from JavaEEIOUtils' replaceObject. The implementation
     *  must return the object that needs to be written out to the
     *  stream OR null if it cannot handle the serialization of this
     *  object
     *
     */
    public Object resolveObject(Object obj) throws IOException;


}
