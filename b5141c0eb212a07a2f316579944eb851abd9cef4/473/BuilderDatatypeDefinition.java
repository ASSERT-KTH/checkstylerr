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

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/** Builder class for OWLDatatypeDefinitionAxiom. */
public class BuilderDatatypeDefinition extends BaseBuilder<OWLDatatypeDefinitionAxiom, BuilderDatatypeDefinition> {

    @Nullable private OWLDataRange range = null;
    @Nullable private OWLDatatype type = null;

    /**
     * Builder initialized from an existing object.
     * 
     * @param expected
     *        the existing object
     * @param df
     *        data factory
     */
    public BuilderDatatypeDefinition(OWLDatatypeDefinitionAxiom expected, OWLDataFactory df) {
        this(df);
        with(expected.getDatatype()).withType(expected.getDataRange()).withAnnotations(expected.annotations());
    }

    /**
     * @param df
     *        data factory
     */
    @Inject
    public BuilderDatatypeDefinition(OWLDataFactory df) {
        super(df);
    }

    /**
     * @param arg
     *        range
     * @return builder
     */
    public BuilderDatatypeDefinition withType(OWL2Datatype arg) {
        return withType(arg.getDatatype(df));
    }

    /**
     * @param arg
     *        range
     * @return builder
     */
    public BuilderDatatypeDefinition withType(OWLDataRange arg) {
        range = arg;
        return this;
    }

    /**
     * @param arg
     *        type
     * @return builder
     */
    public BuilderDatatypeDefinition with(OWLDatatype arg) {
        type = arg;
        return this;
    }

    @Override
    public OWLDatatypeDefinitionAxiom buildObject() {
        return df.getOWLDatatypeDefinitionAxiom(verifyNotNull(type), getRange(), annotations);
    }

    /**
     * @return range
     */
    public OWLDataRange getRange() {
        return verifyNotNull(range);
    }

    /**
     * @return type
     */
    @Nullable
    public OWLDatatype getType() {
        return type;
    }
}
