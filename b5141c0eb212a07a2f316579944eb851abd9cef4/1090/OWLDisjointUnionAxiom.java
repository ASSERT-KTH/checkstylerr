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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents a <a href=
 * "http://www.w3.org/TR/owl2-syntax/#Disjoint_Union_of_Class_Expressions" >
 * DisjointUnion</a> axiom in the OWL 2 Specification.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public interface OWLDisjointUnionAxiom extends OWLClassAxiom {

    @Override
        OWLDisjointUnionAxiom getAxiomWithoutAnnotations();

    @Override
    default Stream<?> components() {
        return Stream.of(getOWLClass(), classExpressions(), annotations());
    }

    @Override
    default Stream<?> componentsWithoutAnnotations() {
        return Stream.of(getOWLClass(), classExpressions());
    }

    @Override
    default Stream<?> componentsAnnotationsFirst() {
        return Stream.of(annotations(), getOWLClass(), classExpressions());
    }

    @Override
    default int hashIndex() {
        return 43;
    }

    /**
     * Gets the class which is equivalent to the disjoint union.
     * 
     * @return the class that is equivalent to a disjoint union of other
     *         classes.
     */
    OWLClass getOWLClass();

    /**
     * Gets the class expressions which are operands of the disjoint union.
     * 
     * @return A {@code Set} containing the operands of the disjoint union, note
     *         that this <b>does not</b> include the {@code OWLClass} that is
     *         equivalent to the disjoint union.
     * @deprecated use the stream method
     */
    @Deprecated
    default Set<OWLClassExpression> getClassExpressions() {
        return asSet(classExpressions());
    }

    /**
     * Gets the class expressions which are operands of the disjoint union.
     * 
     * @return A {@code Set} containing the operands of the disjoint union, note
     *         that this <b>does not</b> include the {@code OWLClass} that is
     *         equivalent to the disjoint union.
     */
    Stream<OWLClassExpression> classExpressions();

    /**
     * Gets the part of this axiom that corresponds to an
     * {@code EquivalentClasses} axiom.
     * 
     * @return The equivalent classes axiom part of this axiom. This is
     *         essentially, {@code EquivalentClasses(CE, CEUnion)} where
     *         {@code CEUnion} is the union of the classes returned by the
     *         {@link #getClassExpressions()} method and {@code CE} is the class
     *         returned by the {@link #getOWLClass()} method.
     */
    OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom();

    /**
     * Gets the part of this axiom that corresponds to an
     * {@code DisjointClasses} axiom.
     * 
     * @return The disjoint classes axiom part of this axiom. This is
     *         essentially, {@code DisjointClasses(CE1, ..., CEn)} where
     *         {@code CEi in (CE1, ..., CEn)} is contained in the classes
     *         returned by the {@link #getClassExpressions()} method.
     */
    OWLDisjointClassesAxiom getOWLDisjointClassesAxiom();

    @Override
    default void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default AxiomType<?> getAxiomType() {
        return AxiomType.DISJOINT_UNION;
    }
}
