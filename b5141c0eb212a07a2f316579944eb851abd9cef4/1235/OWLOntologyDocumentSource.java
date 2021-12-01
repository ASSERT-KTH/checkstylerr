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

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.emptyOptional;

import java.io.InputStream;
import java.io.Reader;
import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;

/**
 * A document source provides a point for loading an ontology. A document source
 * may provide three ways of obtaining an ontology document:
 * <ol>
 * <li>From a {@link java.io.Reader}
 * <li>From an {@link java.io.InputStream}
 * <li>From an ontology document {@link org.semanticweb.owlapi.model.IRI}
 * </ol>
 * Consumers that use a document source will attempt to obtain a concrete
 * representation of an ontology in the above order. <br>
 * Note that while an ontology document source may appear similar to a SAX input
 * source, an important difference is that the getReader and getInputStream
 * methods return new instances each time the method is called. This allows
 * multiple attempts at loading an ontology.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public interface OWLOntologyDocumentSource {

    /**
     * Gets a reader which an ontology document can be read from. This method
     * may be called multiple times. Each invocation will return a new
     * {@code Reader}. If there is no reader stream available, returns
     * Optional.absent.
     * 
     * @return A new {@code Reader} which the ontology can be read from, wrapped
     *         in an Optional.
     */
    default Optional<Reader> getReader() {
        return emptyOptional();
    }

    /**
     * If an input stream can be obtained from this document source then this
     * method creates it. This method may be called multiple times. Each
     * invocation will return a new input stream. If there is no input stream
     * available, returns Optional.absent. .
     * 
     * @return A new input stream which the ontology can be read from, wrapped
     *         in an Optional.
     */
    default Optional<InputStream> getInputStream() {
        return emptyOptional();
    }

    /**
     * Gets the IRI of the ontology document.
     * 
     * @return An IRI which represents the ontology document IRI
     */
    IRI getDocumentIRI();

    /**
     * @return format for the ontology. If none is known, return
     *         Optional.absent.
     */
    default Optional<OWLDocumentFormat> getFormat() {
        return emptyOptional();
    }

    /**
     * @return MIME type for this source, if one is specified. If none is known,
     *         return Optional.absent.
     */
    default Optional<String> getMIMEType() {
        return emptyOptional();
    }

    /**
     * @return true if there is no reader or input stream available for this
     *         source, or reading from them has already been attempted in a
     *         previous call and has failed. This leaves attempting to resolve
     *         the document IRI as input. No attempt is made to verify that the
     *         document IRI is resolvable.
     */
    boolean hasAlredyFailedOnStreams();

    /**
     * @return true if resolving the document IRI has been attempted by a
     *         previous call and has failed, false if resolution has not been
     *         attempted yet or it has happened successfully.
     */
    boolean hasAlredyFailedOnIRIResolution();

    /**
     * IRI resolution does not happen inside this class. This method allows the
     * resolver to mark the document IRI as unresolvable, so that the
     * information is tracked with the source.
     * 
     * @param value
     *        new value for the flag
     */
    void setIRIResolutionFailed(boolean value);
}
