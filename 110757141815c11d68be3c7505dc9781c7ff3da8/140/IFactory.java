/*******************************************************************************
 * Copyright (c) 2011, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.util;

/**
 * Interface for factories of class instances.
 *
 * @param <T> type of instances that are created by this factory
 * @author msp
 */
public interface IFactory<T> {

    /**
     * Create an instance of the type that is managed by this factory.
     * 
     * @return a new instance
     */
    T create();
    
    /**
     * Destroy a given instance by freeing all resources that are contained.
     * 
     * @param obj the instance to destroy
     */
    void destroy(T obj);
    
}
