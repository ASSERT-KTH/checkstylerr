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
package org.semanticweb.owlapi.model.axiomproviders;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;

/**
 * Disjoint union provider.
 */
@FunctionalInterface
public interface DisjointUnionAxiomProvider {

    /**
     * @param owlClass
     *        left hand side of the axiom.
     * @param classExpressions
     *        right hand side of the axiom. Cannot be null or contain nulls.
     * @return a disjoint union axiom
     */
    default OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
        Collection<? extends OWLClassExpression> classExpressions) {
        return getOWLDisjointUnionAxiom(owlClass, classExpressions, Collections.emptySet());
    }

    /**
     * @param owlClass
     *        left hand side of the axiom. Cannot be null.
     * @param classExpressions
     *        right hand side of the axiom. Cannot be null or contain nulls.
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a disjoint union axiom with annotations
     */
    default OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
        Collection<? extends OWLClassExpression> classExpressions, Collection<OWLAnnotation> annotations) {
        return getOWLDisjointUnionAxiom(owlClass, classExpressions.stream(), annotations);
    }

    /**
     * @param owlClass
     *        left hand side of the axiom.
     * @param classExpressions
     *        right hand side of the axiom. Cannot be null or contain nulls.
     * @return a disjoint union axiom
     */
    default OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
        Stream<? extends OWLClassExpression> classExpressions) {
        return getOWLDisjointUnionAxiom(owlClass, classExpressions, Collections.emptySet());
    }

    /**
     * @param owlClass
     *        left hand side of the axiom. Cannot be null.
     * @param classExpressions
     *        right hand side of the axiom. Cannot be null or contain nulls.
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a disjoint union axiom with annotations
     */
    OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
        Stream<? extends OWLClassExpression> classExpressions, Collection<OWLAnnotation> annotations);
}
