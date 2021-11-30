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

package com.sun.enterprise.v3.server;

import org.glassfish.hk2.api.Factory;
import org.jvnet.hk2.annotations.Service;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton executor service factory.
 *
 * @author Jerome Dochez
 */
@Service
public class ExecutorServiceFactory implements Factory<ExecutorService> {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#provide()
     */
    @Override
    public ExecutorService provide() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        }
        );
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#dispose(java.lang.Object)
     */
    @Override
    public void dispose(ExecutorService instance) {
        // TODO Auto-generated method stub

    }
}
