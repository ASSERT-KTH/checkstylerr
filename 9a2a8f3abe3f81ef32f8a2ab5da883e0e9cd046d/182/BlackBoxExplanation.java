/* This file is part of the OWL API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright 2011, Clark & Parsia, LLC
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. */
package com.clarkparsia.owlapi.explanation;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.add;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asUnorderedSet;

import com.clarkparsia.owlapi.explanation.util.OntologyUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A black box explanation.
 */
public class BlackBoxExplanation extends SingleExplanationGeneratorImpl implements
    SingleExplanationGenerator {

    /**
     * default expansion limit.
     */
    public static final int DEFAULT_INITIAL_EXPANSION_LIMIT = 50;
    private static final Logger LOGGER = LoggerFactory
        .getLogger(BlackBoxExplanation.class.getName());
    /**
     * The Constant DEFAULT_FAST_PRUNING_WINDOW_SIZE.
     */
    private static final int DEFAULT_FAST_PRUNING_WINDOW_SIZE = 10;
    /**
     * The objects expanded with defining axioms.
     */
    private final Set<OWLEntity> objectsExpandedWithDefiningAxioms = new HashSet<>();
    /**
     * The objects expanded with referencing axioms.
     */
    private final Set<OWLEntity> objectsExpandedWithReferencingAxioms = new HashSet<>();
    /**
     * The expanded with defining axioms.
     */
    private final Set<OWLAxiom> expandedWithDefiningAxioms = new HashSet<>();
    /**
     * The expanded with referencing axioms.
     */
    private final Set<OWLAxiom> expandedWithReferencingAxioms = new HashSet<>();
    /**
     * The initial expansion limit.
     */
    private final int initialExpansionLimit = DEFAULT_INITIAL_EXPANSION_LIMIT;
    /**
     * The owl ontology manager.
     */
    private final OWLOntologyManager man;
    /**
     * The debugging axioms.
     */
    protected Set<OWLAxiom> debuggingAxioms = new LinkedHashSet<>();
    /**
     * The debugging ontology.
     */
    @Nullable
    private OWLOntology debuggingOntology;
    /**
     * The expansion limit.
     */
    private int expansionLimit = initialExpansionLimit;
    /**
     * The fast pruning window size.
     */
    private int fastPruningWindowSize;
    // Creation of debugging ontology and satisfiability testing
    private int satTestCount;

    /**
     * Instantiates a new black box explanation.
     *
     * @param ontology the ontology
     * @param reasonerFactory the reasoner factory
     * @param reasoner the reasoner
     */
    public BlackBoxExplanation(OWLOntology ontology, OWLReasonerFactory reasonerFactory,
        OWLReasoner reasoner) {
        super(ontology, reasonerFactory, reasoner);
        man = ontology.getOWLOntologyManager();
    }

    /**
     * A utility method. Adds axioms from one set to another set upto a
     * specified limit. Annotation axioms are stripped out
     *
     * @param <N> the number type
     * @param source The source set. Objects from this set will be added to the destination set
     * @param dest The destination set. Objects will be added to this set
     * @param limit The maximum number of objects to be added.
     * @return The number of objects that were actually added.
     */
    private static <N extends OWLAxiom> int addMax(Set<N> source, Set<N> dest, int limit) {
        int count = 0;
        for (N obj : source) {
            if (count == limit) {
                break;
            }
            if (!(obj instanceof OWLAnnotationAxiom) && dest.add(obj)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void dispose() {
        reset();
        getReasoner().dispose();
    }

    private void reset() {
        if (debuggingOntology != null) {
            man.removeOntology(verifyNotNull(debuggingOntology));
            debuggingOntology = null;
        }
        debuggingAxioms.clear();
        objectsExpandedWithDefiningAxioms.clear();
        objectsExpandedWithReferencingAxioms.clear();
        expandedWithDefiningAxioms.clear();
        expandedWithReferencingAxioms.clear();
        expansionLimit = initialExpansionLimit;
    }

    @Override
    public Set<OWLAxiom> getExplanation(OWLClassExpression unsatClass) {
        if (!getDefinitionTracker().isDefined(unsatClass)) {
            return Collections.emptySet();
        }
        try {
            satTestCount++;
            if (isFirstExplanation() && getReasoner().isSatisfiable(unsatClass)) {
                return Collections.emptySet();
            }
            reset();
            expandUntilUnsatisfiable(unsatClass);
            pruneUntilMinimal(unsatClass);
            removeDeclarations();
            return new HashSet<>(debuggingAxioms);
        } catch (OWLException e) {
            throw new OWLRuntimeException(e);
        }
    }

    // Expansion
    private int expandAxioms() {
        /*
         * We expand the axiom set using axioms that define entities that are
         * already referenced in the existing set of axioms. If this fails to
         * expand the axiom set we expand using axioms that reference the
         * entities in the axioms that have already been expanded.
         */
        // Keep track of the number of axioms that have been added
        int axiomsAdded = 0;
        int remainingSpace = expansionLimit;
        /* The expansion factor. */
        double expansionFactor = 1.25;
        for (OWLAxiom ax : new ArrayList<>(debuggingAxioms)) {
            if (expandedWithDefiningAxioms.add(ax)) {
                // Collect the entities that have been used in the axiom
                for (OWLEntity curObj : asList(ax.signature())) {
                    if (!objectsExpandedWithDefiningAxioms.contains(curObj)) {
                        int added = expandWithDefiningAxioms(curObj, remainingSpace);
                        axiomsAdded += added;
                        remainingSpace -= added;
                        if (remainingSpace == 0) {
                            expansionLimit *= expansionFactor;
                            return axiomsAdded;
                        }
                        // Flag that we have completely expanded all defining
                        // axioms
                        // for this particular entity
                        objectsExpandedWithDefiningAxioms.add(curObj);
                    }
                }
            }
        }
        if (axiomsAdded > 0) {
            return axiomsAdded;
        }
        // No axioms added at this point. Start adding axioms that reference
        // entities contained in the current set of debugging axioms
        for (OWLAxiom ax : new ArrayList<>(debuggingAxioms)) {
            if (expandedWithReferencingAxioms.add(ax)) {
                // Keep track of the number of axioms that have been added
                for (OWLEntity curObj : asList(ax.signature())) {
                    if (!objectsExpandedWithReferencingAxioms.contains(curObj)) {
                        int added = expandWithReferencingAxioms(curObj, expansionLimit);
                        axiomsAdded += added;
                        remainingSpace -= added;
                        if (remainingSpace == 0) {
                            expansionLimit *= expansionFactor;
                            return axiomsAdded;
                        }
                        objectsExpandedWithReferencingAxioms.add(curObj);
                    }
                }
            }
        }
        return axiomsAdded;
    }

    /**
     * Creates a set of axioms to expands the debugging axiom set by adding the
     * defining axioms for the specified entity.
     *
     * @param obj the obj
     * @param limit the limit
     * @return the int
     */
    private int expandWithDefiningAxioms(OWLEntity obj, int limit) {
        Set<OWLAxiom> expansionAxioms = new HashSet<>();
        getOntology().importsClosure().forEach(ont -> {
            boolean referenceFound = false;
            if (obj instanceof OWLClass) {
                referenceFound = add(expansionAxioms, ont.axioms((OWLClass) obj));
            } else if (obj.isOWLObjectProperty()) {
                referenceFound = add(expansionAxioms, ont.axioms(obj.asOWLObjectProperty()));
            } else if (obj.isOWLDataProperty()) {
                referenceFound = add(expansionAxioms, ont.axioms(obj.asOWLDataProperty()));
            } else if (obj instanceof OWLIndividual) {
                referenceFound = add(expansionAxioms, ont.axioms((OWLIndividual) obj));
            }
            if (!referenceFound) {
                expansionAxioms.add(man.getOWLDataFactory().getOWLDeclarationAxiom(obj));
            }
        });
        expansionAxioms.removeAll(debuggingAxioms);
        return addMax(expansionAxioms, debuggingAxioms, limit);
    }

    /**
     * Expands the axiom set by adding the referencing axioms for the specified
     * entity.
     *
     * @param obj the obj
     * @param limit the limit
     * @return the int
     */
    private int expandWithReferencingAxioms(OWLEntity obj, int limit) {
        // First expand by getting the defining axioms - if this doesn't
        // return any axioms, then get the axioms that reference the entity
        Set<OWLAxiom> expansionAxioms = asUnorderedSet(
            getOntology().referencingAxioms(obj, INCLUDED));
        expansionAxioms.removeAll(debuggingAxioms);
        return addMax(expansionAxioms, debuggingAxioms, limit);
    }

    // Contraction/Pruning - Fast pruning is performed and then slow pruning is
    // performed.
    private void performFastPruning(OWLClassExpression unsatClass) throws OWLException {
        Set<OWLAxiom> axiomWindow = new HashSet<>();
        Object[] axioms = debuggingAxioms.toArray();
        LOGGER.info("Fast pruning: ");
        LOGGER.info("     - Window size: {}", Integer.valueOf(fastPruningWindowSize));
        int windowCount = debuggingAxioms.size() / fastPruningWindowSize;
        for (int currentWindow = 0; currentWindow < windowCount; currentWindow++) {
            axiomWindow.clear();
            int startIndex = currentWindow * fastPruningWindowSize;
            int endIndex = startIndex + fastPruningWindowSize;
            for (int axiomIndex = startIndex; axiomIndex < endIndex; axiomIndex++) {
                OWLAxiom currentAxiom = (OWLAxiom) axioms[axiomIndex];
                axiomWindow.add(currentAxiom);
                debuggingAxioms.remove(currentAxiom);
            }
            if (isSatisfiable(unsatClass)) {
                debuggingAxioms.addAll(axiomWindow);
            }
        }
        // Add any left over axioms
        axiomWindow.clear();
        int remainingAxiomsCount = debuggingAxioms.size() % fastPruningWindowSize;
        if (remainingAxiomsCount > 0) {
            int fragmentIndex = windowCount * fastPruningWindowSize;
            while (fragmentIndex < axioms.length) {
                OWLAxiom curAxiom = (OWLAxiom) axioms[fragmentIndex];
                axiomWindow.add(curAxiom);
                debuggingAxioms.remove(curAxiom);
                fragmentIndex++;
            }
            if (isSatisfiable(unsatClass)) {
                debuggingAxioms.addAll(axiomWindow);
            }
        }
        LOGGER.info("    - End of fast pruning");
    }

    private void performSlowPruning(OWLClassExpression unsatClass) throws OWLException {
        // Simply remove axioms one at a time. If the class
        // being debugged turns satisfiable then we know we have
        // an SOS axiom.
        List<OWLAxiom> axiomsCopy = new ArrayList<>(debuggingAxioms);
        for (OWLAxiom ax : axiomsCopy) {
            debuggingAxioms.remove(ax);
            if (isSatisfiable(unsatClass)) {
                // Affects satisfiability, so add back in
                debuggingAxioms.add(ax);
            }
        }
    }

    /**
     * Tests the satisfiability of the test class. The ontology is recreated
     * before the test is performed.
     *
     * @param unsatClass the unsat class
     * @return true, if is satisfiable
     * @throws OWLException the oWL exception
     */
    private boolean isSatisfiable(OWLClassExpression unsatClass) throws OWLException {
        createDebuggingOntology();
        OWLReasoner reasoner = getReasonerFactory()
            .createNonBufferingReasoner(verifyNotNull(debuggingOntology));
        if (OntologyUtils
            .containsUnreferencedEntity(verifyNotNull(debuggingOntology), unsatClass)) {
            reasoner.dispose();
            return true;
        }
        satTestCount++;
        boolean sat = reasoner.isSatisfiable(unsatClass);
        reasoner.dispose();
        return sat;
    }

    private void createDebuggingOntology() throws OWLException {
        if (debuggingOntology != null) {
            man.removeOntology(verifyNotNull(debuggingOntology));
        }
        debuggingOntology = man.createOntology();
        debuggingOntology.add(debuggingAxioms);
    }

    private void resetSatisfiabilityTestCounter() {
        satTestCount = 0;
    }

    private void expandUntilUnsatisfiable(OWLClassExpression unsatClass) throws OWLException {
        // Perform the initial expansion - this will cause
        // the debugging axioms set to be expanded to the
        // defining axioms for the class being debugged
        resetSatisfiabilityTestCounter();
        if (unsatClass.isAnonymous()) {
            OWLClass owlThing = man.getOWLDataFactory().getOWLThing();
            OWLSubClassOfAxiom axiom = man.getOWLDataFactory()
                .getOWLSubClassOfAxiom(unsatClass, owlThing);
            debuggingAxioms.add(axiom);
            expandAxioms();
            debuggingAxioms.remove(axiom);
        } else {
            expandWithDefiningAxioms((OWLClass) unsatClass, expansionLimit);
        }
        LOGGER.info("Initial axiom count: {}", Integer.valueOf(debuggingAxioms.size()));
        int totalAdded = 0;
        int expansionCount = 0;
        while (isSatisfiable(unsatClass)) {
            LOGGER.info("Expanding axioms (expansion {})", Integer.valueOf(expansionCount));
            expansionCount++;
            int numberAdded = expandAxioms();
            totalAdded += numberAdded;
            LOGGER.info("    ... expanded by {}", Integer.valueOf(numberAdded));
            if (numberAdded == 0) {
                LOGGER.info("ERROR! Cannot find SOS axioms!");
                debuggingAxioms.clear();
                return;
            }
        }
        LOGGER.info("Total number of axioms added: {}", Integer.valueOf(totalAdded));
    }

    /**
     * Prune until minimal.
     *
     * @param unsatClass the unsat class
     * @throws OWLException the oWL exception
     */
    protected void pruneUntilMinimal(OWLClassExpression unsatClass) throws OWLException {
        LOGGER.info("FOUND CLASH! Pruning {} axioms...", Integer.valueOf(debuggingAxioms.size()));
        resetSatisfiabilityTestCounter();
        LOGGER.info("Fast pruning...");
        fastPruningWindowSize = DEFAULT_FAST_PRUNING_WINDOW_SIZE;
        performFastPruning(unsatClass);
        LOGGER.info("... end of fast pruning. Axioms remaining: {}",
            Integer.valueOf(debuggingAxioms.size()));
        LOGGER.info("Performed {} satisfiability tests during fast pruning",
            Integer.valueOf(satTestCount));
        int totalSatTests = satTestCount;
        resetSatisfiabilityTestCounter();
        LOGGER.info("Slow pruning...");
        performSlowPruning(unsatClass);
        LOGGER.info("... end of slow pruning");
        LOGGER.info("Performed {} satisfiability tests during slow pruning",
            Integer.valueOf(satTestCount));
        totalSatTests += satTestCount;
        LOGGER.info("Total number of satisfiability tests performed: {}",
            Integer.valueOf(totalSatTests));
    }

    private void removeDeclarations() {
        debuggingAxioms = asUnorderedSet(
            debuggingAxioms.stream().filter(ax -> !(ax instanceof OWLDeclarationAxiom)));
    }

    @Override
    public String toString() {
        return "BlackBox";
    }
}
