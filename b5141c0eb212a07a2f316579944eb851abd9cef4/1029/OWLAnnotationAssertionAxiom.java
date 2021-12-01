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

import java.util.stream.Stream;

/**
 * Represents
 * <a href= "http://www.w3.org/TR/owl2-syntax/#Annotation_Assertion" >
 * AnnotationAssertion</a> axioms in the OWL 2 specification.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public interface OWLAnnotationAssertionAxiom
    extends OWLAnnotationAxiom, HasSubject<OWLAnnotationSubject>, HasProperty<OWLAnnotationProperty> {

    @Override
    default Stream<?> componentsWithoutAnnotations() {
        return Stream.of(getSubject(), getProperty(), getValue());
    }

    @Override
    default Stream<?> components() {
        return Stream.of(getSubject(), getProperty(), getValue(), annotations());
    }

    @Override
    default Stream<?> componentsAnnotationsFirst() {
        return Stream.of(annotations(), getSubject(), getProperty(), getValue());
    }

    @Override
    default int hashIndex() {
        return 47;
    }

    /**
     * Gets the annotation value. This is either an
     * {@link org.semanticweb.owlapi.model.IRI}, an
     * {@link org.semanticweb.owlapi.model.OWLAnonymousIndividual} or an
     * {@link OWLLiteral}. Annotation values can be visited with an
     * {@link org.semanticweb.owlapi.model.OWLAnnotationValueVisitor}.
     * 
     * @see org.semanticweb.owlapi.model.OWLAnnotationValueVisitor
     * @see org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx
     * @return The annotation value.
     */
    OWLAnnotationValue getValue();

    /**
     * Gets the combination of the annotation property and the annotation value
     * as an {@link org.semanticweb.owlapi.model.OWLAnnotation} object.
     * 
     * @return The annotation object that combines the property and value of
     *         this annotation.
     */
    OWLAnnotation getAnnotation();

    /**
     * Determines if this annotation assertion deprecates the IRI that is the
     * subject of the annotation.
     * 
     * @return {@code true} if this annotation assertion deprecates the subject
     *         IRI of the assertion, otherwise {@code false}.
     * @see org.semanticweb.owlapi.model.OWLAnnotation#isDeprecatedIRIAnnotation()
     */
    boolean isDeprecatedIRIAssertion();

    @Override
        OWLAnnotationAssertionAxiom getAxiomWithoutAnnotations();

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
        return AxiomType.ANNOTATION_ASSERTION;
    }
}
