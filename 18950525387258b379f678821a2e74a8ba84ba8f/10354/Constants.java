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

package com.sun.enterprise.glassfish.bootstrap;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class Constants {
    public final static String PLATFORM_PROPERTY_KEY = "GlassFish_Platform";
    // bundle containing module startup
    public final static String GF_KERNEL = "org.glassfish.core.kernel";
    public static final String ARGS_PROP = "com.sun.enterprise.glassfish.bootstrap.args";
    public final static String DEFAULT_DOMAINS_DIR_PROPNAME = "AS_DEF_DOMAINS_PATH";
    public static final String ORIGINAL_CP     = "-startup-classpath";
    public static final String ORIGINAL_CN     = "-startup-classname";
    public static final String ORIGINAL_ARGS   = "-startup-args";
    public static final String ARG_SEP         = ",,,";

    public final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot";
    public static final String INSTALL_ROOT_URI_PROP_NAME = "com.sun.aas.installRootURI";
    public static final String INSTANCE_ROOT_URI_PROP_NAME = "com.sun.aas.instanceRootURI";
    public static final String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    public static final String INHABITANTS_CACHE = "inhabitants";
    public static final String BUILDER_NAME_PROPERTY = "GlassFish.BUILDER_NAME";
    public static final String NO_FORCED_SHUTDOWN = "--noforcedshutdown";

    private Constants(){}


    // Supported platform we know about, not limited to.
    public enum Platform {Felix, Knopflerfish, Equinox, Static}
}
