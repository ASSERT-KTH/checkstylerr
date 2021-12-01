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
package org.semanticweb.owlapi.io;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.rdf.api.BlankNode;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.NodeID;

/**
 * Anonymous node implementation.
 */
public class RDFResourceBlankNode extends RDFResource implements
    org.apache.commons.rdf.api.BlankNode {

    /**
     * Random UUID, used by {@link #uniqueReference()}
     */
    private static final UUID UNIQUE_BASE = UUID.randomUUID();
    private final IRI resource;
    private final boolean isIndividual;
    private final boolean forceIdOutput;

    /**
     * Create an RDFResource that is anonymous.
     *
     * @param resource The IRI of the resource
     * @param isIndividual true if the node represents an individual
     * @param forceId true if id should be outputted
     */
    public RDFResourceBlankNode(IRI resource, boolean isIndividual, boolean forceId) {
        this.resource = checkNotNull(resource, "resource cannot be null");
        this.isIndividual = isIndividual;
        forceIdOutput = forceId;
    }

    /**
     * Create an RDFResource that is anonymous.
     *
     * @param anonId the number at the end of the anon IRI
     * @param isIndividual true if the node represents an individual
     * @param forceId true if id should be outputted
     */
    public RDFResourceBlankNode(int anonId, boolean isIndividual, boolean forceId) {
        this(NodeID.nodeId(anonId), isIndividual, forceId);
    }

    /**
     * Create an RDFResource that is anonymous
     *
     * @param isIndividual true if this is an individual
     * @param forceId true if the id should be outputted
     */
    public RDFResourceBlankNode(boolean isIndividual, boolean forceId) {
        this(NodeID.nextFreshNodeId(), isIndividual, forceId);
    }

    @Override
    public boolean isIndividual() {
        return isIndividual;
    }

    @Override
    public boolean shouldOutputId() {
        return forceIdOutput;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RDFResourceBlankNode) {
            RDFResourceBlankNode other = (RDFResourceBlankNode) obj;
            return resource.equals(other.resource);
        }
        // Commons RDF BlankNode.equals() contract
        if (obj instanceof BlankNode) {
            BlankNode blankNode = (BlankNode) obj;
            return uniqueReference().equals(blankNode.uniqueReference());
        }
        return false;
    }

    @Override
    public String toString() {
        return resource.toString();
    }

    @Override
    public IRI getIRI() {
        return resource;
    }

    @Override
    public IRI getResource() {
        return resource;
    }

    @Override
    public String uniqueReference() {
        String nodeId = resource.getIRIString().replace("_:", "");
        return UNIQUE_BASE + ":" + nodeId;
    }
}
