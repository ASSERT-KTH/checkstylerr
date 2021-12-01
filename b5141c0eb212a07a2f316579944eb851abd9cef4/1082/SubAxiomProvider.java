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

import org.semanticweb.owlapi.model.*;

/**
 * Annotation, datatype and object property range provider.
 */
public interface SubAxiomProvider {

    /**
     * @param subClass
     *        sub class
     * @param superClass
     *        super class
     * @return a subclass axiom with no annotations
     */
    default OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass) {
        return getOWLSubClassOfAxiom(subClass, superClass, Collections.emptySet());
    }

    /**
     * @param subClass
     *        sub class
     * @param superClass
     *        super class
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a subclass axiom with specified annotations
     */
    OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass,
            Collection<OWLAnnotation> annotations);

    /**
     * @param subProperty
     *        sub property
     * @param superProperty
     *        super property
     * @return a subproperty axiom
     */
    default OWLSubObjectPropertyOfAxiom getOWLSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty,
            OWLObjectPropertyExpression superProperty) {
        return getOWLSubObjectPropertyOfAxiom(subProperty, superProperty, Collections.emptySet());
    }

    /**
     * @param subProperty
     *        sub Property
     * @param superProperty
     *        super Property
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a subproperty axiom with annotations
     */
    OWLSubObjectPropertyOfAxiom getOWLSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty,
            OWLObjectPropertyExpression superProperty, Collection<OWLAnnotation> annotations);

    /**
     * @param subProperty
     *        sub Property
     * @param superProperty
     *        super Property
     * @return a subproperty axiom
     */
    default OWLSubDataPropertyOfAxiom getOWLSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty,
            OWLDataPropertyExpression superProperty) {
        return getOWLSubDataPropertyOfAxiom(subProperty, superProperty, Collections.emptySet());
    }

    /**
     * @param subProperty
     *        sub Property
     * @param superProperty
     *        super Property
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a subproperty axiom with annotations
     */
    OWLSubDataPropertyOfAxiom getOWLSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty,
            OWLDataPropertyExpression superProperty, Collection<OWLAnnotation> annotations);

    /**
     * @param sub
     *        sub property
     * @param sup
     *        super property
     * @return a sub annotation property axiom with specified properties
     */
    default OWLSubAnnotationPropertyOfAxiom getOWLSubAnnotationPropertyOfAxiom(OWLAnnotationProperty sub,
            OWLAnnotationProperty sup) {
        return getOWLSubAnnotationPropertyOfAxiom(sub, sup, Collections.emptySet());
    }

    /**
     * @param sub
     *        sub property
     * @param sup
     *        super property
     * @param annotations
     *        A set of annotations. Cannot be null or contain nulls.
     * @return a sub annotation property axiom with specified properties and
     *         annotations
     */
    OWLSubAnnotationPropertyOfAxiom getOWLSubAnnotationPropertyOfAxiom(OWLAnnotationProperty sub,
            OWLAnnotationProperty sup, Collection<OWLAnnotation> annotations);
}
