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
package org.semanticweb.owlapi.model.providers;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.PrefixManager;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics
 *         Research Group
 * @since 3.4.6
 */
@FunctionalInterface
public interface NamedIndividualProvider extends Serializable {

    /**
     * Gets an instance of {@link OWLNamedIndividual} that has the specified
     * {@code IRI}.
     * 
     * @param iri
     *        The IRI.
     * @return An {@link OWLNamedIndividual} that has the specified IRI.
     */
    OWLNamedIndividual getOWLNamedIndividual(IRI iri);

    /**
     * Gets an instance of {@link OWLNamedIndividual} with the same IRI as the
     * input.
     * 
     * @param iri
     *        The HasIRI instance.
     * @return An {@link OWLNamedIndividual} that has iri.getIRI() as IRI.
     */
    default OWLNamedIndividual getOWLNamedIndividual(HasIRI iri) {
        return getOWLNamedIndividual(iri.getIRI());
    }

    /**
     * Gets an instance of {@link OWLNamedIndividual} that has the specified
     * {@code IRI}. This is the same as
     * {@code getOWLNamedIndividual(IRI.create(iri))}.
     * 
     * @param iri
     *        The IRI string.
     * @return An {@link OWLNamedIndividual} that has the specified IRI.
     */
    default OWLNamedIndividual getOWLNamedIndividual(String iri) {
        return getOWLNamedIndividual(IRI.create(iri));
    }

    /**
     * Gets an instance of {@link OWLNamedIndividual} that has the specified
     * {@code IRI}. This is the same as
     * {@code getOWLNamedIndividual(IRI.create(namespace, remainder))}.
     * 
     * @param namespace
     *        The IRI namespace.
     * @param remainder
     *        optional remainder or local name
     * @return An {@link OWLNamedIndividual} that has the specified IRI.
     */
    default OWLNamedIndividual getOWLNamedIndividual(String namespace, @Nullable String remainder) {
        return getOWLNamedIndividual(IRI.create(namespace, remainder));
    }

    /**
     * Gets an OWLNamedIndividual that has an IRI that is obtained by expanding
     * an abbreviated name using an appropriate prefix mapping. See
     * <a href="http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#IRIs">The
     * OWL 2 Structural Specification</a> for more details.
     * 
     * @param abbreviatedIRI
     *        The abbreviated IRI, which is of the form PREFIX_NAME:RC, where
     *        PREFIX_NAME may be the empty string (the default prefix). Note
     *        that abbreviated IRIs always contain a colon as a delimiter, even
     *        if the prefix name is the empty string.
     * @param prefixManager
     *        The prefix manager that is responsible for mapping prefix names to
     *        prefix IRIs.
     * @return An OWLNamedIndividual that has the IRI obtained by expanding the
     *         specified abbreviated IRI using the specified prefix manager.
     *         <br>
     *         For example, suppose "m:Cat" was specified as the abbreviated
     *         IRI, the prefix manager would be used to obtain the IRI prefix
     *         for the "m:" prefix name, this prefix would then be concatenated
     *         with "Cat" to obtain the full IRI which would be the IRI of the
     *         OWLNamedIndividual obtained by this method.
     * @throws OWLRuntimeException
     *         if the prefix name in the specified abbreviated IRI does not have
     *         a mapping to a prefix in the specified prefix manager.
     */
    default OWLNamedIndividual getOWLNamedIndividual(String abbreviatedIRI, PrefixManager prefixManager) {
        checkNotNull(abbreviatedIRI, "curi canno be null");
        checkNotNull(prefixManager, "prefixManager cannot be null");
        return getOWLNamedIndividual(prefixManager.getIRI(abbreviatedIRI));
    }
}
