/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.alg;

/**
 * Classes that implement this interface can create layout processors.
 * 
 * <p>
 * The usual way is to have an enumeration of all available processors implement this interface and instantiate the
 * correct one depending on which enumeration constant the method was called on.
 * </p>
 * 
 * @param <G>
 *            type of the graph the created processor will operate on.
 * @see ILayoutProcessor
 */
public interface ILayoutProcessorFactory<G> {

    /**
     * Returns an implementation of {@link ILayoutProcessor}. The actual implementation returned depends on the actual
     * type that implements this method.
     * 
     * @return new layout processor.
     */
    ILayoutProcessor<G> create();

}
