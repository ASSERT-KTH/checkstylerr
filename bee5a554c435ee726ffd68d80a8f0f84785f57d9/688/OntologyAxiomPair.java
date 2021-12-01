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

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import javax.annotation.Nullable;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @since 3.0.0
 */
public class OntologyAxiomPair {

    @Nullable
    private final OWLOntology ontology;
    private final OWLAxiom axiom;

    /**
     * @param ontology ontology
     * @param axiom axiom
     */
    public OntologyAxiomPair(@Nullable OWLOntology ontology, OWLAxiom axiom) {
        this.ontology = ontology;
        this.axiom = axiom;
    }

    /**
     * @return the ontology
     */
    @Nullable
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * @return the axiom
     */
    public OWLAxiom getAxiom() {
        return axiom;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OntologyAxiomPair)) {
            return false;
        }
        OntologyAxiomPair other = (OntologyAxiomPair) obj;
        if (ontology != null && other.ontology != null) {
            return verifyNotNull(ontology).equals(other.ontology) && axiom.equals(other.axiom);
        }
        if (ontology != other.ontology) {
            return false;
        }
        return axiom.equals(other.axiom);
    }

    @Override
    public int hashCode() {
        if (ontology != null) {
            return verifyNotNull(ontology).hashCode() + axiom.hashCode();
        }
        return 37 + axiom.hashCode();
    }

    @Override
    public String toString() {
        return axiom + " in " + (ontology != null ? verifyNotNull(ontology).toString() : "");
    }
}
