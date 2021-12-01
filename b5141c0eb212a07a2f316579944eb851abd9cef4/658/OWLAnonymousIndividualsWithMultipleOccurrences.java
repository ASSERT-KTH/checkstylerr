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
package org.semanticweb.owlapi.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

/**
 * A utility class that visits axioms, class expressions etc. and accumulates
 * the anonymous individuals objects that are referred to in those axioms, class
 * expressions etc.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group, Date: 13-Nov-2006
 */
public class OWLAnonymousIndividualsWithMultipleOccurrences extends AbstractCollector implements IndividualAppearance {

    private Set<OWLObject> singleAppearanceAsSubject = new HashSet<>();
    private Set<OWLObject> singleAppearance = new HashSet<>();
    private Set<OWLObject> multipleAppearances = new HashSet<>();

    @Override
    public boolean appearsMultipleTimes(OWLAnonymousIndividual i) {
        return multipleAppearances.contains(i);
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {
        checkAppearanceAsObject(individual);
    }

    @Override
    public void visit(OWLAnnotation a) {
        a.getValue().asAnonymousIndividual().ifPresent(this::checkAppearanceAsObject);
        a.annotations().forEach(a1 -> a1.accept(this));
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        axiom.getObject().accept(this);
        axiom.annotations().forEach(a1 -> a1.accept(this));
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        axiom.annotations().forEach(a1 -> a1.accept(this));
    }

    @Override
    public void visit(OWLOntology ontology) {
        ontology.annotations().forEach(a1 -> a1.accept(this));
        AxiomType.AXIOM_TYPES.forEach(t -> ontology.axioms(t).forEach(ax -> ax.accept(this)));
        singleAppearance.clear();
        singleAppearanceAsSubject.clear();
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        axiom.getSubject().asAnonymousIndividual().ifPresent(this::checkAppearanceAsSubject);
        axiom.getValue().asAnonymousIndividual().ifPresent(this::checkAppearanceAsObject);
    }

    protected void checkAppearanceAsObject(OWLAnonymousIndividual a) {
        if (!multipleAppearances.contains(a) && !singleAppearance.add(a)) {
            // already seen, move it
            singleAppearance.remove(a);
            multipleAppearances.add(a);
        }
    }

    protected void checkAppearanceAsSubject(OWLAnonymousIndividual a) {
        if (!multipleAppearances.contains(a) && !singleAppearanceAsSubject.add(a)) {
            // already seen, move it
            singleAppearanceAsSubject.remove(a);
            multipleAppearances.add(a);
        }
    }
}
