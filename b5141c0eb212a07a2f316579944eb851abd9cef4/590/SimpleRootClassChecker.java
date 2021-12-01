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

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.*;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.contains;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * A utility class that can be used to determine is a class is a syntactic
 * direct subclass of owl:Thing. A class is considered NOT to be a syntactic
 * direct subclass of owl:Thing if ANY of the following conditions apply:
 * <ol>
 * <li>It is equal to the left hand side of a subclass axiom, where the right
 * hand side is a named class other than owl:Thing</li>
 * <li>It is an operand in an equivalent class axiom where at least one of the
 * other other operands is an intersection class that has a named operand other
 * than the class in question. For example
 * {@code EquivalentClasses(A,  (B and prop some C))}</li>
 * </ol>
 * This functionality is provided because it is useful for displaying class
 * hierarchies in editors and browsers. In these situations it is needed because
 * not all "orphan" classes are asserted to be subclasses of owl:Thing. For
 * example, if the only referencing axiom of class A was ObjectDomain(propP A)
 * then A is a syntactic subclass of owl:Thing.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public class SimpleRootClassChecker implements RootClassChecker {

    private final Collection<OWLOntology> ontologies;
    // Rules for determining if a class is a direct subclass of Thing
    // 1) It isn't referenced by ANY subclass axiom or equivalent class axioms
    // 2) It is reference only by subclass axioms, but doesn't appear on the LHS
    // of these axioms
    // 3) It is on the LHS of a subclass axiom where the RHS is Thing
    // 4) It is referenced only by equivalent class axioms, where all other
    // operands in these axioms are named
    // 5) It is not referenced by subclass axioms and is not referenced by any
    // equivalent class axiom where there is
    // at least one operand in the equivalent class axiom which is an
    // intersection containing a named operand i.e.
    // EquivalentClasses(A (B and hasP some C)) would not be a subclass of Thing
    private final RootClassCheckerHelper checker = new RootClassCheckerHelper();
    private final NamedSuperChecker superChecker = new NamedSuperChecker();

    /**
     * Creates a root class checker, which examines axioms contained in
     * ontologies from the specified set in order to determine if a class is a
     * syntactic subclass of owl:Thing.
     * 
     * @param ontologies
     *        The ontologies whose axioms are to be taken into consideration
     *        when determining if a class is a syntactic direct subclass of
     *        owl:Thing
     */
    public SimpleRootClassChecker(Collection<OWLOntology> ontologies) {
        this.ontologies = checkNotNull(ontologies, "ontologies cannot be null");
    }

    @Override
    public boolean isRootClass(OWLClass cls) {
        return !ontologies.stream().flatMap(o -> o.referencingAxioms(cls)).anyMatch(ax -> isRootClass(cls, ax));
    }

    private boolean isRootClass(OWLClass cls, OWLAxiom ax) {
        return !ax.accept(checker.setOWLClass(cls)).booleanValue();
    }

    private static class NamedSuperChecker implements OWLClassExpressionVisitorEx<Boolean> {

        protected boolean namedSuper;

        NamedSuperChecker() {}

        public void reset() {
            namedSuper = false;
        }

        @Override
        public Boolean visit(OWLClass ce) {
            namedSuper = true;
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(OWLObjectIntersectionOf ce) {
            return Boolean.valueOf(ce.operands().anyMatch(op -> op.accept(this).booleanValue()));
        }
    }

    protected boolean check(OWLClassExpression e) {
        superChecker.reset();
        e.accept(superChecker);
        return !superChecker.namedSuper;
    }

    /**
     * A utility class that checks if an axiom gives rise to a class being a
     * subclass of Thing.
     */
    private class RootClassCheckerHelper implements OWLAxiomVisitorEx<Boolean> {

        private Boolean isRoot = Boolean.TRUE;
        @Nullable private OWLClass cls = null;

        public RootClassCheckerHelper() {}

        public RootClassCheckerHelper setOWLClass(OWLClass cls) {
            // Start off with the assumption that the class is
            // a root class. This means if the class isn't referenced
            // by any equivalent class axioms or subclass axioms then
            // we correctly identify it as a root
            isRoot = Boolean.TRUE;
            this.cls = cls;
            return this;
        }

        private OWLClass cls() {
            return verifyNotNull(cls, "cls cannot be null. Has the helper been initialised with a valid value?");
        }

        @Override
        public Boolean visit(OWLSubClassOfAxiom axiom) {
            if (axiom.getSubClass().equals(cls())) {
                isRoot = Boolean.valueOf(check(axiom.getSuperClass()));
            }
            return isRoot;
        }

        @Override
        public Boolean visit(OWLEquivalentClassesAxiom axiom) {
            if (!contains(axiom.classExpressions(), cls())) {
                return isRoot;
            }
            boolean check = false;
            Iterator<OWLClassExpression> it = axiom.classExpressions().iterator();
            while (it.hasNext()) {
                OWLClassExpression desc = it.next();
                if (!desc.equals(cls)) {
                    check = check(desc);
                    if (check) {
                        isRoot = Boolean.FALSE;
                        return isRoot;
                    }
                }
            }
            isRoot = Boolean.valueOf(check);
            return isRoot;
        }
    }
}
