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

import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * Represents a
 * <a href="http://www.w3.org/TR/owl2-syntax/#Datatypes">Datatype</a> (named
 * data range) in the OWL 2 Specification.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public interface OWLDatatype extends OWLDataRange, OWLLogicalEntity, OWLNamedObject {

    @Override
    default int hashIndex() {
        return 269;
    }

    @Override
    default int typeIndex() {
        return 4001;
    }

    @Override
    default EntityType<?> getEntityType() {
        return EntityType.DATATYPE;
    }

    @Override
    default DataRangeType getDataRangeType() {
        return DataRangeType.DATATYPE;
    }

    /**
     * Gets the built in datatype information if this datatype is a built in
     * datatype. This method should only be called if the isBuiltIn() method
     * returns {@code true}
     * 
     * @return The OWLDatatypeVocabulary that describes this built in datatype
     * @throws OWLRuntimeException
     *         if this datatype is not a built in datatype.
     */
    OWL2Datatype getBuiltInDatatype();

    /**
     * Determines if this datatype has the IRI {@code xsd:string}.
     * 
     * @return {@code true} if this datatype has the IRI {@code xsd:string},
     *         otherwise {@code false}.
     */
    boolean isString();

    /**
     * Determines if this datatype has the IRI {@code xsd:integer}.
     * 
     * @return {@code true} if this datatype has the IRI {@code xsd:integer},
     *         otherwise {@code false}.
     */
    boolean isInteger();

    /**
     * Determines if this datatype has the IRI {@code xsd:float}.
     * 
     * @return {@code true} if this datatype has the IRI {@code xsd:float},
     *         otherwise {@code false}.
     */
    boolean isFloat();

    /**
     * Determines if this datatype has the IRI {@code xsd:double}.
     * 
     * @return {@code true} if this datatype has the IRI {@code xsd:double},
     *         otherwise {@code false}.
     */
    boolean isDouble();

    /**
     * Determines if this datatype has the IRI {@code xsd:boolean}.
     * 
     * @return {@code true} if this datatype has the IRI {@code xsd:boolean},
     *         otherwise {@code false}.
     */
    boolean isBoolean();

    /**
     * Determines if this datatype has the IRI {@code rdf:PlainLiteral}.
     * 
     * @return {@code true} if this datatype has the IRI
     *         {@code rdf:PlainLiteral} otherwise {@code false}
     */
    boolean isRDFPlainLiteral();

    @Override
    default void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(OWLEntityVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLEntityVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(OWLNamedObjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLNamedObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(OWLDataVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLDataVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    default void accept(OWLDataRangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default <O> O accept(OWLDataRangeVisitorEx<O> visitor) {
        return visitor.visit(this);
    }
}
