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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Matthew Horridge, The University Of Manchester, Medical Informatics
 *         Group
 * @since 2.0.0
 */
public interface SWRLBuiltInAtom extends SWRLAtom {

    @Override
    default Stream<?> components() {
        return Stream.of(arguments(), getPredicate());
    }

    @Override
    default int hashIndex() {
        return 659;
    }

    @Override
    default int typeIndex() {
        return 6005;
    }

    @Override
        IRI getPredicate();

    /**
     * @return list of arguments
     */
    default List<SWRLDArgument> getArguments() {
        return asList(arguments());
    }

    /**
     * @return list of arguments
     */
    Stream<SWRLDArgument> arguments();

    /**
     * Determines if the predicate of this atom is is a core builtin.
     * 
     * @return {@code true} if this is a core builtin, otherwise {@code false}
     */
    boolean isCoreBuiltIn();

    @Override
    default void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(SWRLObjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(SWRLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }
}
