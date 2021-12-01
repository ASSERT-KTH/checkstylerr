/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.util;

import org.eclipse.elk.core.AbstractLayoutProvider;

/**
 * A generic factory for layout algorithms.
 */
public class AlgorithmFactory implements IFactory<AbstractLayoutProvider> {
    
    /** The class for which instances shall be created. */
    private final Class<? extends AbstractLayoutProvider> clazz;
    /** The parameter used for initialization of layout providers. */
    private final String parameter;
    
    /**
     * Creates an instance factory for the given layout provider class.
     * 
     * @param theclazz the class for which instances shall be created
     */
    public AlgorithmFactory(final Class<? extends AbstractLayoutProvider> theclazz) {
        this(theclazz, null);
    }
    
    /**
     * Creates an instance factory for the given layout provider class, initialized with a parameter.
     * 
     * @param theclazz the class for which instances shall be created
     * @param theparameter the parameter used for initialization of layout providers
     */
    public AlgorithmFactory(final Class<? extends AbstractLayoutProvider> theclazz, final String theparameter) {
        this.clazz = theclazz;
        this.parameter = theparameter;
    }
    
    @Override
    public AbstractLayoutProvider create() {
        try {
            AbstractLayoutProvider algorithm = clazz.newInstance();
            algorithm.initialize(parameter);
            return algorithm;
        } catch (InstantiationException exception) {
            throw new WrappedException(exception);
        } catch (IllegalAccessException exception) {
            throw new WrappedException(exception);
        }
    }

    @Override
    public void destroy(final AbstractLayoutProvider obj) {
        obj.dispose();
    }

}
