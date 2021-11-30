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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.web.deployment.runtime.JspConfig;

/**
 * This node is the superclass for all web related runtime nodes
 *
 */
public class JspConfigRuntimeNode extends RuntimeDescriptorNode {
    /**
     * Initialize the child handlers
     */
    public JspConfigRuntimeNode() {

        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY),
                WebPropertyNode.class, "addWebProperty");
    }

    protected JspConfig descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public JspConfig getDescriptor() {
        if (descriptor==null) {
            descriptor = new JspConfig();
        }
        return descriptor;
    }
}
