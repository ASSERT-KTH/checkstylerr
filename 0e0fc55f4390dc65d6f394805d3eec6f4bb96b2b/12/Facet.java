/**
 * Copyright (c) 2009-2010 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.xs.facets;

import java.util.List;

import org.genxdm.typed.types.AtomBridge;
import org.genxdm.xs.exceptions.FacetException;
import org.genxdm.xs.types.SimpleType;

/**
 * Common interface for all facets excluding xs:enumeration and xs:pattern.
 */
public interface Facet
{
    /**
     * Determines whether the facet is fixed.
     */
    boolean isFixed();

    FacetKind getKind();

    /**
     * Checks the passed <em>typed value</em> according to this facet.
     * 
     * @param actualValue
     *            The actual value.
     * @param simpleType
     *            The type that validated the actual value.
     */
    <A> void validate(List<? extends A> actualValue, SimpleType simpleType, AtomBridge<A> bridge) throws FacetException;
}
