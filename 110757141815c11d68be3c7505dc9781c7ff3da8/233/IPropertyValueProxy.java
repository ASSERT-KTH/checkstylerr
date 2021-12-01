/*******************************************************************************
 * Copyright (c) 2009, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.graph.properties;

/**
 * A proxy object for properties that are resolved lazily.
 *
 * @author msp
 */
public interface IPropertyValueProxy {
    
    /**
     * Resolve the value associated with the given property.
     * 
     * @param <T> property type
     * @param property a property
     * @return the corresponding value, or {@code null} if the value cannot be resolved
     */
    <T> T resolveValue(IProperty<T> property);

}
