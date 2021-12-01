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
package org.semanticweb.owlapi.rdf.rdfxml.renderer;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asUnorderedSet;

import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

/**
 * @author Matthew Horridge, The University of Manchester, Bio-Health
 *         Informatics Group
 * @since 3.3.0
 */
public class RDFXMLNamespaceManager extends OWLOntologyXMLNamespaceManager {

    /**
     * @param ontology
     *        ontology
     * @param format
     *        format
     */
    public RDFXMLNamespaceManager(OWLOntology ontology, OWLDocumentFormat format) {
        super(ontology, format);
    }

    @Override
    protected Set<OWLEntity> getEntitiesThatRequireNamespaces() {
        return asUnorderedSet(
            Stream.of(
                getOntology().axioms(AxiomType.OBJECT_PROPERTY_ASSERTION).flatMap(
                    ax -> ax.getProperty().objectPropertiesInSignature()),
                getOntology().axioms(AxiomType.DATA_PROPERTY_ASSERTION).map(ax -> ax.getProperty().asOWLDataProperty()),
                getOntology().annotationPropertiesInSignature(Imports.INCLUDED)).flatMap(x -> x));
    }

    /**
     * @return entities with invalid qnames
     */
    public Set<OWLEntity> getEntitiesWithInvalidQNames() {
        return asUnorderedSet(getEntitiesThatRequireNamespaces().stream().filter(e -> !e.getIRI().getRemainder()
            .isPresent()));
    }
}
