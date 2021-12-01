/*
 * This file is part of the OWL API.
 * 
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * 
 * Copyright (C) 2011, The University of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 * 
 * 
 * Alternatively, the contents of this file may be used under the terms of the Apache License,
 * Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable
 * instead of those above.
 * 
 * Copyright 2011, The University of Queensland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.semanticweb.owlapi.rio;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.*;

import javax.annotation.Nullable;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.RDFDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.rdf.rdfxml.parser.OWLRDFConsumer;
import org.semanticweb.owlapi.util.AnonymousNodeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link OWLRDFConsumer} implementation that implements the Sesame
 * {@link RDFHandler} interface.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 4.0.0
 */
public class RioOWLRDFConsumerAdapter extends OWLRDFConsumer implements RDFHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RioOWLRDFConsumerAdapter.class);

    /**
     * @param ontology
     *        ontology to update
     * @param checker
     *        node checker
     * @param configuration
     *        loading configuration
     */
    public RioOWLRDFConsumerAdapter(OWLOntology ontology, AnonymousNodeChecker checker,
        OWLOntologyLoaderConfiguration configuration) {
        super(ontology, checker, configuration);
    }

    @Override
    public void endRDF() {
        endModel();
    }

    @Override
    public void handleComment(@Nullable String comment) {
        // nothing to do here
    }

    @Override
    public void handleNamespace(@Nullable String prefix, @Nullable String uri) {
        RDFDocumentFormat format = getOntologyFormat();
        // XXX this pattern might be cleaned up by making PrefixDocumentFormat
        // methods default methods on OWLDocumentFormat
        if (format instanceof PrefixDocumentFormat) {
            PrefixDocumentFormat prefixDocumentFormat = (PrefixDocumentFormat) format;
            prefixDocumentFormat.setPrefix(prefix + ':', verifyNotNull(uri));
        }
    }

    @Override
    public void handleStatement(@Nullable final Statement st) {
        checkNotNull(st);
        assert st != null;
        String subjectString;
        String objectString;
        if (st.getSubject() instanceof BNode) {
            subjectString = st.getSubject().stringValue();
            // it is not mandatory for BNode.stringValue() to return a string
            // prefixed with the turtle blank node syntax, so we check here to
            // make sure
            subjectString = "_:genid-nodeid-" + subjectString;
        } else {
            subjectString = st.getSubject().stringValue();
        }
        if (st.getObject() instanceof BNode) {
            objectString = st.getObject().stringValue();
            // it is not mandatory for BNode.stringValue() to return a string
            // prefixed with the turtle blank node syntax, so we check here to
            // make sure
            objectString = "_:genid-nodeid-" + objectString;
        } else {
            objectString = st.getObject().stringValue();
        }
        if (st.getObject() instanceof Resource) {
            LOGGER.trace("statement with resource value");
            statementWithResourceValue(subjectString, st.getPredicate().stringValue(), objectString);
        } else {
            final Literal literalObject = (Literal) st.getObject();
            String literalDatatype = null;
            final String literalLanguage = literalObject.getLanguage().orElse(null);
            if (literalLanguage == null) {
                literalDatatype = literalObject.getDatatype().stringValue();
            }
            LOGGER.trace("statement with literal value");
            statementWithLiteralValue(subjectString, st.getPredicate().stringValue(), objectString, literalLanguage,
                literalDatatype);
        }
    }

    @Override
    public void startRDF() {
        // creating a mock IRI here. In the current implementation its value is
        // ignored
        startModel(IRI.create("urn:test:", "unused"));
    }
}
