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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.util.*;

/**
 *
 * @author Mahesh Meswani
 */
public class Provider {
    private String moduleProviderName = null;
    private String moduleName = null;
    private String probeProviderName = null;
    private String probeProviderClass = null;
    private List<XmlProbe> probes = null;

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleProviderName() {
        return moduleProviderName;
    }

    public String getProbeProviderName() {
        return probeProviderName;
    }

    public String getProbeProviderClass() {
        return probeProviderClass;
    }

    public List<XmlProbe> getProbes() {
        return probes;
    }

    public Provider(String moduleProviderName, String moduleName,
                    String probeProviderName, String providerClass,
                    List<XmlProbe> probes) {
        this.moduleProviderName = moduleProviderName;
        this.moduleName = moduleName;
        this.probeProviderName = probeProviderName;
        this.probeProviderClass = providerClass;
        this.probes = probes;

    }

    @Override
    public String toString() {
        StringBuilder probeStr = new StringBuilder();
        probeStr.append("moduelProviderName=")
                .append(moduleProviderName)
                .append(" moduleName=")
                .append(moduleName)
                .append(" probeProvidername=")
                .append(probeProviderName)
                .append(" probeProviderClass=")
                .append(probeProviderClass);
        for (XmlProbe probe : probes) {
            probeStr.append("\n    ").append(probe.toString());
        }
        return (probeStr.toString());
    }
}
