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
package org.semanticweb.owlapi.model;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.io.Serializable;

import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public abstract class OWLOntologyChange implements HasSignature, Serializable {

    private final OWLOntology ont;

    /**
     * @param ont
     *        the ontology to which the change is to be applied
     */
    public OWLOntologyChange(OWLOntology ont) {
        this.ont = checkNotNull(ont, "ontology must not be null");
    }

    /**
     * Determines if the change will cause the addition or removal of an axiom
     * from an ontology.
     * 
     * @return {@code true} if the change is an {@code OWLAddAxiomChange} or
     *         {@code OWLRemoveAxiomChange} otherwise {@code false}.
     */
    public boolean isAxiomChange() {
        return false;
    }

    /**
     * Determines if the change will add an axiom to an ontology.
     * 
     * @return {@code true} if the change is an AddAxiom change and it will add
     *         an axiom to an ontology, {@code false} otherwise.
     */
    public boolean isAddAxiom() {
        return false;
    }

    /**
     * Determines if the change will remove an axiom from an ontology.
     * 
     * @return {@code true} if the change is a RemoveAxiom change and it will
     *         remove an axiom from an ontology, {@code false} otherwise.
     */
    public boolean isRemoveAxiom() {
        return isAxiomChange() && !isAddAxiom();
    }

    /**
     * If the change is an axiom change (i.e. AddAxiom or RemoveAxiom) this
     * method obtains the axiom.
     * 
     * @return The Axiom if this change is an axiom change
     * @throws IllegalStateException
     *         if the change has no axiom; UnsupportedOperationException If the
     *         change is not an axiom change (check with the
     *         {@code isAxiomChange} method first).
     */
    public OWLAxiom getAxiom() {
        throw new UnsupportedOperationException("This is an " + getClass().getSimpleName() + ", not an axiom change: "
            + this);
    }

    /**
     * Determines if this change is an import change and hence causes a change
     * to the imports closure of an ontology.
     * 
     * @return {@code true} if this change is an import change, otherwise
     *         {@code false}.
     */
    public boolean isImportChange() {
        return false;
    }

    /**
     * Gets the ontology that the change is/was applied to.
     * 
     * @return The ontology that the change is applicable to
     */
    public OWLOntology getOntology() {
        return ont;
    }

    /**
     * Gets the data (independent of the ontology) associated with this specific
     * change.
     * 
     * @return The {@link OWLOntologyChangeData} associated with this
     *         {@code OWLOntologyChange}.
     */
    public abstract OWLOntologyChangeData getChangeData();

    /**
     * Gets a {@link OWLOntologyChangeRecord} that is derived from this
     * {@code OWLOntologyChange}'s {@link OWLOntologyID} and it's
     * {@link OWLOntologyChangeData}.
     * 
     * @return An {@link OWLOntologyChangeRecord} containing an
     *         {@link OWLOntologyID} equal to the {@link OWLOntologyID} of this
     *         {@code OWLOntologyChange}'s {@link OWLOntology}. Not {@code null}
     *         .
     */
    public OWLOntologyChangeRecord getChangeRecord() {
        return new OWLOntologyChangeRecord(ont.getOntologyID(), getChangeData());
    }

    /**
     * Accepts a visitor.
     * 
     * @param visitor
     *        The visitor
     */
    public abstract void accept(OWLOntologyChangeVisitor visitor);

    /**
     * Accepts a visitor.
     * 
     * @param visitor
     *        The visitor
     * @param <O>
     *        visitor return type
     * @return visitor value
     */
    public abstract <O> O accept(OWLOntologyChangeVisitorEx<O> visitor);

    /**
     * @return the reverse of this change; can be used to create undo changes.
     */
    public abstract OWLOntologyChange reverseChange();
}
