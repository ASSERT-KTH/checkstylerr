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

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

/**
 * A short form provider that generates short forms based on entity annotation
 * values. A list of preferred annotation URIs and preferred annotation
 * languages is used to determine which annotation value to select if there are
 * multiple annotations for the entity whose short form is being generated. If
 * there are multiple annotations the these annotations are ranked by preferred
 * IRI and then by preferred language.
 *
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics Group
 * @since 2.0.0
 */
public class AnnotationValueShortFormProvider implements ShortFormProvider {

    private final OWLOntologySetProvider ontologySetProvider;
    private final ShortFormProvider alternateShortFormProvider;
    private final IRIShortFormProvider alternateIRIShortFormProvider;
    private final List<OWLAnnotationProperty> annotationProperties;
    private final Map<OWLAnnotationProperty, List<String>> preferredLanguageMap;
    private StringAnnotationVisitor literalRenderer = new StringAnnotationVisitor();

    /**
     * Constructs an annotation value short form provider. Using
     * {@code SimpleShortFormProvider} as the alternate short form provider
     *
     * @param annotationProperties A {@code List} of preferred annotation properties. The list is
     * searched from start to end, so that annotations that have a property at the start of the list
     * have a higher priority and are selected over annotations with properties that appear towards
     * or at the end of the list.
     * @param preferredLanguageMap A map which maps annotation properties to preferred languages.
     * For any given annotation property there may be a list of preferred languages. Languages at
     * the start of the list have a higher priority over languages at the end of the list. This
     * parameter may be empty but it must not be {@code null}.
     * @param ontologySetProvider An {@code OWLOntologySetProvider} which provides a set of ontology
     * from which candidate annotation axioms should be taken. For a given entity, all ontologies
     * are examined.
     */
    public AnnotationValueShortFormProvider(List<OWLAnnotationProperty> annotationProperties,
        Map<OWLAnnotationProperty, List<String>> preferredLanguageMap,
        OWLOntologySetProvider ontologySetProvider) {
        this(annotationProperties, preferredLanguageMap, ontologySetProvider,
            new SimpleShortFormProvider());
    }

    /**
     * Constructs an annotation short form provider.
     *
     * @param annotationProperties A {@code List} of preferred annotation properties. The list is
     * searched from start to end, so that annotations that have a property at the start of the list
     * have a higher priority and are selected over annotations with properties that appear towards
     * or at the end of the list.
     * @param preferredLanguageMap A map which maps annotation properties to preferred languages.
     * For any given annotation property there may be a list of preferred languages. Languages at
     * the start of the list have a higher priority over languages at the end of the list. This
     * parameter may be empty but it must not be {@code null}.
     * @param ontologySetProvider An {@code OWLOntologySetProvider} which provides a set of ontology
     * from which candidate annotation axioms should be taken. For a given entity, all ontologies
     * are examined.
     * @param alternateShortFormProvider A short form provider which will be used to generate the
     * short form for an entity that does not have any annotations. This provider will also be used
     * in the case where the value of an annotation is an {@code OWLIndividual} for providing the
     * short form of the individual.
     */
    public AnnotationValueShortFormProvider(List<OWLAnnotationProperty> annotationProperties,
        Map<OWLAnnotationProperty, List<String>> preferredLanguageMap,
        OWLOntologySetProvider ontologySetProvider,
        ShortFormProvider alternateShortFormProvider) {
        this(ontologySetProvider, alternateShortFormProvider, new SimpleIRIShortFormProvider(),
            annotationProperties,
            preferredLanguageMap);
    }

    /**
     * @param ontologySetProvider ontologies
     * @param alternateShortFormProvider short form provider
     * @param alternateIRIShortFormProvider iri short form provider
     * @param annotationProperties annotation properties
     * @param preferredLanguageMap preferred language map
     */
    public AnnotationValueShortFormProvider(OWLOntologySetProvider ontologySetProvider,
        ShortFormProvider alternateShortFormProvider,
        IRIShortFormProvider alternateIRIShortFormProvider,
        List<OWLAnnotationProperty> annotationProperties,
        Map<OWLAnnotationProperty, List<String>> preferredLanguageMap) {
        this.ontologySetProvider = checkNotNull(ontologySetProvider,
            "ontologySetProvider cannot be null");
        this.alternateShortFormProvider = checkNotNull(alternateShortFormProvider,
            "alternateShortFormProvider cannot be null");
        this.alternateIRIShortFormProvider = checkNotNull(alternateIRIShortFormProvider,
            "alternateIRIShortFormProvider cannot be null");
        this.annotationProperties = checkNotNull(annotationProperties,
            "annotationProperties cannot be null");
        this.preferredLanguageMap = checkNotNull(preferredLanguageMap,
            "preferredLanguageMap cannot be null");
    }

    @Override
    public String getShortForm(OWLEntity entity) {
        Stream<OWLOntology> onts = ontologySetProvider.ontologies();
        List<OWLAnnotationAssertionAxiom> flatMap = asList(
            onts.flatMap(o -> o.annotationAssertionAxioms(entity
                .getIRI(), INCLUDED)));
        for (OWLAnnotationProperty prop : annotationProperties) {
            // visit the properties in order of preference
            AnnotationLanguageFilter checker = new AnnotationLanguageFilter(prop,
                preferredLanguageMap.get(prop));
            flatMap.forEach(ax -> ax.accept(checker));
            OWLObject match = checker.getMatch();
            if (match != null) {
                return getRendering(match);
            }
        }
        return alternateShortFormProvider.getShortForm(entity);
    }

    /**
     * Obtains the rendering of the specified object. If the object is a
     * constant then the rendering is equal to the literal value, if the object
     * is an individual then the rendering is equal to the rendering of the
     * individual as provided by the alternate short form provider
     *
     * @param object The object to the rendered
     * @return The rendering of the object.
     */
    private String getRendering(OWLObject object) {
        // We return the literal value of constants or use the alternate
        // short form provider to render individuals.
        if (object instanceof OWLLiteral) {
            // TODO refactor this method to use the annotation value visitor
            return literalRenderer.visit((OWLLiteral) object);
        } else if (object.isIRI()) {
            return alternateIRIShortFormProvider.getShortForm((IRI) object);
        } else {
            return alternateShortFormProvider.getShortForm((OWLEntity) object);
        }
    }

    /**
     * @return the annotation URIs that this short form provider uses.
     */
    public List<OWLAnnotationProperty> getAnnotationProperties() {
        return annotationProperties;
    }

    /**
     * @return the preferred language map
     */
    public Map<OWLAnnotationProperty, List<String>> getPreferredLanguageMap() {
        return preferredLanguageMap;
    }

    private static class AnnotationLanguageFilter implements OWLObjectVisitor {

        private final OWLAnnotationProperty prop;
        private final List<String> preferredLanguages;
        @Nullable
        protected OWLObject candidateValue = null;
        int lastLangMatchIndex = Integer.MAX_VALUE;

        AnnotationLanguageFilter(OWLAnnotationProperty prop,
            @Nullable List<String> preferredLanguages) {
            this.prop = prop;
            this.preferredLanguages =
                preferredLanguages == null ? Collections.emptyList() : preferredLanguages;
        }

        @Nullable
        public OWLObject getMatch() {
            return candidateValue;
        }

        @Override
        public void visit(OWLAnnotationAssertionAxiom axiom) {
            if (lastLangMatchIndex > 0 && axiom.getProperty().equals(prop)) {
                // a perfect match - no need to carry on search
                axiom.getValue().accept(this);
            }
        }

        @Override
        public void visit(OWLLiteral node) {
            if (preferredLanguages.isEmpty()) {
                // if there are no languages just match the first thing
                lastLangMatchIndex = 0;
                candidateValue = node;
            } else {
                int index = preferredLanguages.indexOf(node.getLang());
                if (index >= 0 && index < lastLangMatchIndex) {
                    lastLangMatchIndex = index;
                    candidateValue = node;
                }
            }
        }

        @Override
        public void visit(IRI iri) {
            // No language
            candidateValue = iri;
        }
    }

    /**
     * @param literalRenderer the literalRenderer to set
     */
    public void setLiteralRenderer(StringAnnotationVisitor literalRenderer) {
        this.literalRenderer = checkNotNull(literalRenderer);
    }
}
