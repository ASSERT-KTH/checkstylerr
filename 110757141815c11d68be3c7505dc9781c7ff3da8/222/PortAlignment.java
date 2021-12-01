/*******************************************************************************
 * Copyright (c) 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.options;

/**
 * Defines the distribution of ports.
 *
 * @author csp
 */
public enum PortAlignment {

    /** Ports are evenly distributed, with the same amount of space between and around them. */
    DISTRIBUTED,
    
    /** Ports are justified and use up all the space except for the surrounding ports spacing. */
    JUSTIFIED,

    /** Ports are placed at the most top respectively left position with minimal spacing. */
    BEGIN,

    /** Ports are centered with minimal spacing. */
    CENTER,

    /** Ports are placed at the most top respectively left position with minimal spacing. */
    END;

}
