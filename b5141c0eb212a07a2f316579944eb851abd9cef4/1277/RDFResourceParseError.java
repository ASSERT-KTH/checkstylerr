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
package org.semanticweb.owlapi.io;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * Describes why an RDF resource could not be parsed into an {@code OWLObject}.
 * For example, why an RDF resource could not be parsed into an
 * {@code OWLClassExpression}. <br>
 * When these errors occur, the RDF parser generates an {@code OWLEntity} that
 * represents the error and inserts this where appropriate into the
 * corresponding complete OWLObject (OWLAxiom) that could not be parsed.
 * 
 * @author Matthew Horridge, The University of Manchester, Bio-Health
 *         Informatics Group
 * @since 3.2
 */
public class RDFResourceParseError implements Serializable {

    private final OWLEntity parserGeneratedErrorEntity;
    private final RDFNode mainNode;
    private final Set<RDFTriple> mainNodeTriples = new HashSet<>();

    /**
     * @param parserGeneratedErrorEntity
     *        the error entity
     * @param mainNode
     *        the main node
     * @param mainNodeTriples
     *        the main node triples
     */
    public RDFResourceParseError(OWLEntity parserGeneratedErrorEntity, RDFNode mainNode,
            Set<RDFTriple> mainNodeTriples) {
        this.parserGeneratedErrorEntity = parserGeneratedErrorEntity;
        this.mainNode = mainNode;
        this.mainNodeTriples.addAll(mainNodeTriples);
    }

    /**
     * @return the error entity
     */
    public OWLEntity getParserGeneratedErrorEntity() {
        return parserGeneratedErrorEntity;
    }

    /**
     * @return the main node
     */
    public RDFNode getMainNode() {
        return mainNode;
    }

    /**
     * @return the main node triples
     */
    public Set<RDFTriple> getMainNodeTriples() {
        return CollectionFactory.copyMutable(mainNodeTriples);
    }
}
