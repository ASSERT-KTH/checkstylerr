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
package org.semanticweb.owlapi.reasoner.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Matthew Horridge, The University of Manchester, Information
 *         Management Group
 * @since 3.0.0
 */
public class OWLClassNode extends DefaultNode<OWLClass> {

    /**
     * @param entity
     *        the class to be contained
     */
    public OWLClassNode(OWLClass entity) {
        super(entity);
    }

    /**
     * @param entities
     *        the classes to be contained
     */
    public OWLClassNode(Collection<OWLClass> entities) {
        super(entities);
    }

    /**
     * @param entities
     *        the classes to be contained
     */
    public OWLClassNode(Stream<OWLClass> entities) {
        super(entities);
    }

    /** Default constructor. */
    public OWLClassNode() {
        super();
    }

    @Override
    protected Optional<OWLClass> getTopEntity() {
        return Optional.of(TOP_CLASS);
    }

    @Override
    protected Optional<OWLClass> getBottomEntity() {
        return Optional.of(BOTTOM_CLASS);
    }

    /**
     * @return singleton top node
     */
    public static OWLClassNode getTopNode() {
        return TOP_NODE;
    }

    /**
     * @return singleton bottom node
     */
    public static OWLClassNode getBottomNode() {
        return BOTTOM_NODE;
    }
}
