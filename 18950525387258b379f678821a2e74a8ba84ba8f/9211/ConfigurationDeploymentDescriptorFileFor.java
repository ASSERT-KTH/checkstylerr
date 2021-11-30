/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.io;

import org.glassfish.hk2.api.Metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Normally goes with {@link org.jvnet.hk2.annotations.Service} annotation,
 * and this annotation must be placed on a class that extends
 * {@link com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile}.
 */
@Qualifier
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationDeploymentDescriptorFileFor {
    /** Used as the metadata key */
    public final static String DESCRIPTOR_FOR = "DescriptorFor";

    /**
     * the value of the annotation should represent the area
     * this configuration deployment descriptor file is for
     */
    @Metadata(DESCRIPTOR_FOR)
    String value();
}
