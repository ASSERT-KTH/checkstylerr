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

package org.glassfish.config.support;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.DomDocument;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Contract defining services capable of persisting the configuration.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ConfigurationPersistence {

    /**
     * callback when the new {@link DomDocument} instance should be saved to an external media like a file
     *
     * @param doc the new document instance
     * @throws IOException if the file cannot be opened/written/closed
     * @throws XMLStreamException if the xml cannot be written out successfully
     */
    public void save(DomDocument doc) throws IOException, XMLStreamException;
}
