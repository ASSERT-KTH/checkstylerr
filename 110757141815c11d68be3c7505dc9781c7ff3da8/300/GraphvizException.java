/*******************************************************************************
 * Copyright (c) 2011, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.graphviz.layouter;

/**
 * Represents errors that occurs while calling the Graphviz command-line tool for
 * automatic layout..
 *
 * @author msp
 */
public class GraphvizException extends RuntimeException {

    /** the serial version UID. */
    private static final long serialVersionUID = 8555047018449649407L;

    /**
     * Create a Graphviz exception with a message.
     * 
     * @param message a message
     */
    public GraphvizException(final String message) {
        super(message);
    }
    
    /**
     * Create a Graphviz exception with a message and a cause.
     * 
     * @param message a message
     * @param cause a cause
     */
    public GraphvizException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
