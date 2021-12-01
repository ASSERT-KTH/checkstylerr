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
package uk.ac.manchester.cs.owl.owlapi;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public class OWLInverseObjectPropertiesAxiomImpl extends OWLNaryPropertyAxiomImpl<OWLObjectPropertyExpression>
    implements OWLInverseObjectPropertiesAxiom {

    private final OWLObjectPropertyExpression first;
    private final OWLObjectPropertyExpression second;

    /**
     * @param first
     *        first property
     * @param second
     *        second peoperty
     * @param annotations
     *        annotations
     */
    public OWLInverseObjectPropertiesAxiomImpl(OWLObjectPropertyExpression first, OWLObjectPropertyExpression second,
        Collection<OWLAnnotation> annotations) {
        super(new TreeSet<>(Arrays.asList(checkNotNull(first, "first cannot be null"), checkNotNull(second,
            "second cannot be null"))), annotations);
        this.first = first;
        this.second = second;
    }

    @Override
    public Collection<OWLInverseObjectPropertiesAxiom> asPairwiseAxioms() {
        return CollectionFactory.createSet((OWLInverseObjectPropertiesAxiom) this);
    }

    @Override
    public Collection<OWLInverseObjectPropertiesAxiom> splitToAnnotatedPairs() {
        return asPairwiseAxioms();
    }

    @Override
    public OWLInverseObjectPropertiesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return new OWLInverseObjectPropertiesAxiomImpl(getFirstProperty(), getSecondProperty(), NO_ANNOTATIONS);
    }

    @Override
    public <T extends OWLAxiom> T getAnnotatedAxiom(Stream<OWLAnnotation> anns) {
        return (T) new OWLInverseObjectPropertiesAxiomImpl(getFirstProperty(), getSecondProperty(), mergeAnnos(anns));
    }

    @Override
    public OWLObjectPropertyExpression getFirstProperty() {
        return first;
    }

    @Override
    public OWLObjectPropertyExpression getSecondProperty() {
        return second;
    }

    @Override
    public Collection<OWLSubObjectPropertyOfAxiom> asSubObjectPropertyOfAxioms() {
        Set<OWLSubObjectPropertyOfAxiom> axs = new HashSet<>();
        axs.add(new OWLSubObjectPropertyOfAxiomImpl(first, second.getInverseProperty().getSimplified(),
            NO_ANNOTATIONS));
        axs.add(new OWLSubObjectPropertyOfAxiomImpl(second, first.getInverseProperty().getSimplified(),
            NO_ANNOTATIONS));
        return axs;
    }
}
