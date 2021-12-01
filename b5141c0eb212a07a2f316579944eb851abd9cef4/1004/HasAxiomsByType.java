/* This file is part of the OWL API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright 2014, The University of Manchester
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. */
package org.semanticweb.owlapi.model;

import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.Set;
import java.util.stream.Stream;

/**
 * An interface to an object that contains axioms and can provide subsets of
 * these axioms by the type of axiom.
 * 
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics
 *         Research Group
 * @since 3.4.6
 */
@FunctionalInterface
public interface HasAxiomsByType {

    /**
     * Compare the axioms inside this object with the axioms inside the other
     * object, shortcutting by type - won't merge all axioms in a large lump for
     * comparisons only
     * 
     * @param o
     *        other object to compare
     * @return true if the two objects contain the same axioms
     */
    default boolean equalAxioms(HasAxiomsByType o) {
        // using collect(toSet()) to avoid ordering issues and avoid
        // LinkedHashSet cost
        return AxiomType.AXIOM_TYPES.stream().allMatch(
            t -> axioms(t).collect(toSet()).equals(o.axioms(t).collect(toSet())));
    }

    /**
     * Gets the axioms which are of the specified type.
     * 
     * @param axiomType
     *        The type of axioms to be retrieved.
     * @param <T>
     *        axiom type
     * @return A set containing the axioms which are of the specified type. The
     *         set that is returned is a copy of the axioms in this object.
     *         Modifications to the returned set will not be reflected in this
     *         object.
     * @deprecated use the stream method
     */
    @Deprecated
    default <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
        return asSet(axioms(axiomType));
    }

    /**
     * Filter axioms according to the specified type.
     * 
     * @param axiomType
     *        The type of axioms to be retrieved.
     * @param <T>
     *        axiom type
     * @return Stream of axioms of the specified type.
     */
    <T extends OWLAxiom> Stream<T> axioms(AxiomType<T> axiomType);
}
