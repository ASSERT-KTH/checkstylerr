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
package org.semanticweb.owlapitools.builders;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;

/** Builder class for OWLDisjointUnionAxiom. */
public class BuilderDisjointUnion extends
    BaseSetBuilder<OWLDisjointUnionAxiom, BuilderDisjointUnion, OWLClassExpression> {

    @Nullable private OWLClass ce = null;

    /**
     * Builder initialized from an existing object.
     * 
     * @param expected
     *        the existing object
     * @param df
     *        data factory
     */
    public BuilderDisjointUnion(OWLDisjointUnionAxiom expected, OWLDataFactory df) {
        this(df);
        withClass(expected.getOWLClass()).withItems(expected.classExpressions()).withAnnotations(expected
            .annotations());
    }

    /**
     * @param df
     *        data factory
     */
    @Inject
    public BuilderDisjointUnion(OWLDataFactory df) {
        super(df);
    }

    /**
     * @param arg
     *        right hand entity
     * @return builder
     */
    public BuilderDisjointUnion withClass(OWLClass arg) {
        ce = arg;
        return this;
    }

    @Override
    public OWLDisjointUnionAxiom buildObject() {
        return df.getOWLDisjointUnionAxiom(verifyNotNull(ce), items, annotations);
    }

    /**
     * @return class expression
     */
    @Nullable
    public OWLClass getClassExpression() {
        return ce;
    }
}
