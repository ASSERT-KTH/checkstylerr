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
package org.semanticweb.owlapi.profiles;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.profiles.violations.*;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * @author Matthew Horridge, The University of Manchester, Information
 *         Management Group
 */
public class OWL2DLProfile implements OWLProfile {

    /**
     * Gets the name of the profile.
     * 
     * @return A string that represents the name of the profile
     */
    @Override
    public String getName() {
        return "OWL 2 DL";
    }

    @Override
    public IRI getIRI() {
        return Profiles.OWL2_DL.getIRI();
    }

    /**
     * Checks an ontology and its import closure to see if it is within this
     * profile.
     * 
     * @param ontology
     *        The ontology to be checked.
     * @return An {@code OWLProfileReport} that describes whether or not the
     *         ontology is within this profile.
     */
    @Override
    public OWLProfileReport checkOntology(OWLOntology ontology) {
        OWL2Profile owl2Profile = new OWL2Profile();
        OWLProfileReport report = owl2Profile.checkOntology(ontology);
        Set<OWLProfileViolation> violations = new LinkedHashSet<>();
        if (!report.isInProfile()) {
            // We won't be in the OWL 2 DL Profile then!
            violations.addAll(report.getViolations());
        }
        OWLOntologyProfileWalker walker = new OWLOntologyProfileWalker(ontology.importsClosure());
        OWL2DLProfileObjectVisitor visitor = new OWL2DLProfileObjectVisitor(walker);
        walker.walkStructure(visitor);
        violations.addAll(visitor.getProfileViolations());
        return new OWLProfileReport(this, violations);
    }

    private static class OWL2DLProfileObjectVisitor extends OWLOntologyWalkerVisitor {

        @Nullable private OWLObjectPropertyManager objectPropertyManager = null;
        private final Set<OWLProfileViolation> profileViolations = new HashSet<>();

        OWL2DLProfileObjectVisitor(OWLOntologyWalker walker) {
            super(walker);
        }

        public Set<OWLProfileViolation> getProfileViolations() {
            return new HashSet<>(profileViolations);
        }

        private OWLObjectPropertyManager getPropertyManager() {
            if (objectPropertyManager == null) {
                objectPropertyManager = new OWLObjectPropertyManager(getCurrentOntology());
            }
            return verifyNotNull(objectPropertyManager);
        }

        @Override
        public void visit(OWLDataOneOf node) {
            if (node.values().count() < 1) {
                profileViolations.add(new EmptyOneOfAxiom(getCurrentOntology(), getCurrentAxiom()));
            }
        }

        @Override
        public void visit(OWLDataUnionOf node) {
            if (node.operands().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), getCurrentAxiom(), node));
            }
        }

        @Override
        public void visit(OWLDataIntersectionOf node) {
            if (node.operands().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), getCurrentAxiom(), node));
            }
        }

        @Override
        public void visit(OWLObjectIntersectionOf ce) {
            if (ce.operands().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), getCurrentAxiom(), ce));
            }
        }

        @Override
        public void visit(OWLObjectOneOf ce) {
            if (ce.individuals().count() < 1) {
                profileViolations.add(new EmptyOneOfAxiom(getCurrentOntology(), getCurrentAxiom()));
            }
        }

        @Override
        public void visit(OWLObjectUnionOf ce) {
            if (ce.operands().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), getCurrentAxiom(), ce));
            }
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (axiom.classExpressions().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), axiom, axiom));
            }
        }

        @Override
        public void visit(OWLDisjointClassesAxiom axiom) {
            if (axiom.classExpressions().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), axiom, axiom));
            }
        }

        @Override
        public void visit(OWLDisjointUnionAxiom axiom) {
            if (axiom.classExpressions().count() < 2) {
                profileViolations.add(new InsufficientOperands(getCurrentOntology(), axiom, axiom));
            }
        }

        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            if (axiom.properties().count() < 2) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            if (axiom.properties().count() < 2) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            if (axiom.properties().count() < 2) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLHasKeyAxiom axiom) {
            if (axiom.propertyExpressions().count() < 1) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLSameIndividualAxiom axiom) {
            if (axiom.individuals().count() < 2) {
                profileViolations.add(new InsufficientIndividuals(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            if (axiom.individuals().count() < 2) {
                profileViolations.add(new InsufficientIndividuals(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLOntology ontology) {
            OWLOntologyID id = ontology.getOntologyID();
            if (id.isAnonymous()) {
                return;
            }
            Optional<IRI> oIRI = id.getOntologyIRI();
            if (oIRI.isPresent() && oIRI.get().isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForOntologyIRI(getCurrentOntology()));
            }
            Optional<IRI> vIRI = id.getVersionIRI();
            if (vIRI.isPresent() && vIRI.get().isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForVersionIRI(getCurrentOntology()));
            }
        }

        @Override
        public void visit(OWLClass ce) {
            if (!ce.isBuiltIn() && ce.getIRI().isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForClassIRI(getCurrentOntology(), getCurrentAxiom(),
                    ce));
            }
            if (!ce.isBuiltIn() && !getCurrentOntology().isDeclared(ce, INCLUDED)) {
                profileViolations.add(new UseOfUndeclaredClass(getCurrentOntology(), getCurrentAxiom(), ce));
            }
            if (getCurrentOntology().containsDatatypeInSignature(ce.getIRI())) {
                profileViolations.add(new DatatypeIRIAlsoUsedAsClassIRI(getCurrentOntology(), getCurrentAxiom(), ce
                    .getIRI()));
            }
        }

        @Override
        public void visit(OWLDatatype node) {
            // Each datatype MUST statisfy the following:
            // An IRI used to identify a datatype MUST
            // - Identify a datatype in the OWL 2 datatype map (Section 4.1
            // lists them), or
            // - Have the xsd: prefix, or
            // - Be rdfs:Literal, or
            // - Not be in the reserved vocabulary of OWL 2
            if (!OWL2Datatype.isBuiltIn(node.getIRI())) {
                if (!Namespaces.XSD.inNamespace(node.getIRI()) && !node.isTopDatatype() && node.getIRI()
                    .isReservedVocabulary()) {
                    profileViolations.add(new UseOfUnknownDatatype(getCurrentOntology(), getCurrentAxiom(), node));
                }
                // We also have to declare datatypes that are not built in
                if (!node.isTopDatatype() && !node.isBuiltIn() && !getCurrentOntology().isDeclared(node, INCLUDED)) {
                    profileViolations.add(new UseOfUndeclaredDatatype(getCurrentOntology(), getCurrentAxiom(), node));
                }
            }
            if (getCurrentOntology().containsClassInSignature(node.getIRI(), INCLUDED)) {
                profileViolations.add(new DatatypeIRIAlsoUsedAsClassIRI(getCurrentOntology(), getCurrentAxiom(), node
                    .getIRI()));
            }
        }

        @Override
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            if (axiom.getDatatype().getIRI().isReservedVocabulary()) {
                profileViolations.add(new UseOfBuiltInDatatypeInDatatypeDefinition(getCurrentOntology(), axiom));
            }
            // Check for cycles
            Set<OWLDatatype> datatypes = new HashSet<>();
            Set<OWLAxiom> axioms = new LinkedHashSet<>();
            axioms.add(axiom);
            getDatatypesInSignature(datatypes, axiom.getDataRange(), axioms);
            if (datatypes.contains(axiom.getDatatype())) {
                profileViolations.add(new CycleInDatatypeDefinition(getCurrentOntology(), axiom));
            }
        }

        private void getDatatypesInSignature(Set<OWLDatatype> datatypes, OWLObject obj, Set<OWLAxiom> axioms) {
            Consumer<? super OWLDatatypeDefinitionAxiom> addAndRecurse = ax -> {
                axioms.add(ax);
                getDatatypesInSignature(datatypes, ax.getDataRange(), axioms);
            };
            obj.datatypesInSignature().filter(datatypes::add).forEach(dt -> datatypeDefinitions(dt).forEach(
                addAndRecurse));
        }

        protected Stream<OWLDatatypeDefinitionAxiom> datatypeDefinitions(OWLDatatype dt) {
            return Imports.INCLUDED.stream(getCurrentOntology()).flatMap(o -> o.datatypeDefinitions(dt));
        }

        @Override
        public void visit(OWLObjectProperty property) {
            if (!property.isOWLTopObjectProperty() && !property.isOWLBottomObjectProperty() && property.getIRI()
                .isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForObjectPropertyIRI(getCurrentOntology(),
                    getCurrentAxiom(), property));
            }
            if (!property.isBuiltIn() && !getCurrentOntology().isDeclared(property, INCLUDED)) {
                profileViolations.add(new UseOfUndeclaredObjectProperty(getCurrentOntology(), getCurrentAxiom(),
                    property));
            }
            if (getCurrentOntology().containsDataPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
            if (getCurrentOntology().containsAnnotationPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
        }

        @Override
        public void visit(OWLDataProperty property) {
            if (!property.isOWLTopDataProperty() && !property.isOWLBottomDataProperty() && property.getIRI()
                .isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForDataPropertyIRI(getCurrentOntology(),
                    getCurrentAxiom(), property));
            }
            if (!property.isBuiltIn() && !getCurrentOntology().isDeclared(property, INCLUDED)) {
                profileViolations.add(new UseOfUndeclaredDataProperty(getCurrentOntology(), getCurrentAxiom(),
                    property));
            }
            if (getCurrentOntology().containsObjectPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
            if (getCurrentOntology().containsAnnotationPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
        }

        @Override
        public void visit(OWLAnnotationProperty property) {
            if (!property.isBuiltIn() && property.getIRI().isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForAnnotationPropertyIRI(getCurrentOntology(),
                    getCurrentAxiom(), property));
            }
            if (!property.isBuiltIn() && !getCurrentOntology().isDeclared(property, INCLUDED)) {
                profileViolations.add(new UseOfUndeclaredAnnotationProperty(getCurrentOntology(), getCurrentAxiom(),
                    getCurrentAnnotation(), property));
            }
            if (getCurrentOntology().containsObjectPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
            if (getCurrentOntology().containsDataPropertyInSignature(property.getIRI(), INCLUDED)) {
                profileViolations.add(new IllegalPunning(getCurrentOntology(), getCurrentAxiom(), property.getIRI()));
            }
        }

        @Override
        public void visit(OWLNamedIndividual individual) {
            if (!individual.isAnonymous() && individual.getIRI().isReservedVocabulary()) {
                profileViolations.add(new UseOfReservedVocabularyForIndividualIRI(getCurrentOntology(),
                    getCurrentAxiom(), individual));
            }
        }

        @Override
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            if (axiom.getSubProperty().isOWLTopDataProperty()) {
                profileViolations.add(new UseOfTopDataPropertyAsSubPropertyInSubPropertyAxiom(getCurrentOntology(),
                    axiom));
            }
        }

        @Override
        public void visit(OWLObjectMinCardinality ce) {
            if (getPropertyManager().isNonSimple(ce.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInCardinalityRestriction(getCurrentOntology(),
                    getCurrentAxiom(), ce));
            }
        }

        @Override
        public void visit(OWLObjectMaxCardinality ce) {
            if (getPropertyManager().isNonSimple(ce.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInCardinalityRestriction(getCurrentOntology(),
                    getCurrentAxiom(), ce));
            }
        }

        @Override
        public void visit(OWLObjectExactCardinality ce) {
            if (getPropertyManager().isNonSimple(ce.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInCardinalityRestriction(getCurrentOntology(),
                    getCurrentAxiom(), ce));
            }
        }

        @Override
        public void visit(OWLObjectHasSelf ce) {
            if (getPropertyManager().isNonSimple(ce.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInObjectHasSelf(getCurrentOntology(), getCurrentAxiom(),
                    ce));
            }
        }

        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            if (getPropertyManager().isNonSimple(axiom.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInFunctionalPropertyAxiom(getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            if (getPropertyManager().isNonSimple(axiom.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInInverseFunctionalObjectPropertyAxiom(
                    getCurrentOntology(), axiom));
            }
        }

        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            if (getPropertyManager().isNonSimple(axiom.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInIrreflexivePropertyAxiom(getCurrentOntology(),
                    axiom));
            }
        }

        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            if (getPropertyManager().isNonSimple(axiom.getProperty())) {
                profileViolations.add(new UseOfNonSimplePropertyInAsymmetricObjectPropertyAxiom(getCurrentOntology(),
                    axiom));
            }
        }

        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            if (axiom.properties().count() < 2) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
            axiom.properties().filter(getPropertyManager()::isNonSimple).forEach(p -> profileViolations.add(
                new UseOfNonSimplePropertyInDisjointPropertiesAxiom(getCurrentOntology(), axiom, p)));
        }

        @Override
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            // Restriction on the Property Hierarchy. A strict partial order
            // (i.e., an irreflexive and transitive relation) < on AllOPE(Ax)
            // exists that fulfills the following conditions:
            //
            // OP1 < OP2 if and only if INV(OP1) < OP2 for all object properties
            // OP1 and OP2 occurring in AllOPE(Ax).
            // If OPE1 < OPE2 holds, then OPE2 ->* OPE1 does not hold.
            // Each axiom in Ax of the form SubObjectPropertyOf(
            // ObjectPropertyChain( OPE1 ... OPEn ) OPE ) with n => 2 fulfills
            // the following conditions:
            // OPE is equal to owl:topObjectProperty, or [TOP]
            // n = 2 and OPE1 = OPE2 = OPE, or [TRANSITIVE_PROP]
            // OPEi < OPE for each 1 <= i <= n, or [ALL_SMALLER]
            // OPE1 = OPE and OPEi < OPE for each 2 <= i <= n, or [FIRST_EQUAL]
            // OPEn = OPE and OPEi < OPE for each 1 <= i <= n-1. [LAST_EQUAL]
            if (axiom.getPropertyChain().size() < 2) {
                profileViolations.add(new InsufficientPropertyExpressions(getCurrentOntology(), axiom));
            }
            OWLObjectPropertyExpression superProp = axiom.getSuperProperty();
            if (superProp.isOWLTopObjectProperty() || axiom.isEncodingOfTransitiveProperty()) {
                // TOP or TRANSITIVE_PROP: no violation can occur
                return;
            }
            List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
            OWLObjectPropertyExpression first = chain.get(0);
            OWLObjectPropertyExpression last = chain.get(chain.size() - 1);
            checkCenter(axiom, superProp, chain);
            checkExtremes(axiom, superProp, first, last);
            checkExtremes(axiom, superProp, last, first);
        }

        protected void checkExtremes(OWLSubPropertyChainOfAxiom axiom, OWLObjectPropertyExpression superProp,
            OWLObjectPropertyExpression first, OWLObjectPropertyExpression last) {
            if (first.equals(superProp)) {
                // first equals, last must be smaller
                if (getPropertyManager().isLessThan(superProp, last)) {
                    profileViolations.add(new UseOfPropertyInChainCausesCycle(getCurrentOntology(), axiom, last));
                }
            } else {
                // first not equal, it must be smaller
                if (getPropertyManager().isLessThan(superProp, first)) {
                    profileViolations.add(new UseOfPropertyInChainCausesCycle(getCurrentOntology(), axiom, first));
                }
            }
        }

        protected void checkCenter(OWLSubPropertyChainOfAxiom axiom, OWLObjectPropertyExpression superProp,
            List<OWLObjectPropertyExpression> chain) {
            // center part of the chain must be smaller in any case
            for (int i = 1; i < chain.size() - 1; i++) {
                if (getPropertyManager().isLessThan(superProp, chain.get(i))) {
                    profileViolations.add(new UseOfPropertyInChainCausesCycle(getCurrentOntology(), axiom, chain.get(
                        i)));
                }
            }
        }
    }
}
