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

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class which will prepare an input source from a file.
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.2.0
 */
public class FileDocumentSource extends OWLOntologyDocumentSourceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDocumentSource.class);
    private final File file;

    /**
     * Constructs an ontology input source using the specified file.
     * 
     * @param file
     *        The file from which a concrete representation of an ontology will
     *        be obtained.
     */
    public FileDocumentSource(File file) {
        this(file, null, null);
    }

    /**
     * Constructs an ontology input source using the specified file.
     * 
     * @param file
     *        The file from which a concrete representation of an ontology will
     *        be obtained.
     * @param format
     *        ontology format. Can be null.
     */
    public FileDocumentSource(File file, OWLDocumentFormat format) {
        this(file, format, null);
    }

    /**
     * Constructs an ontology input source using the specified file.
     * 
     * @param file
     *        The file from which a concrete representation of an ontology will
     *        be obtained.
     * @param format
     *        ontology format. Can be null.
     * @param mime
     *        mime type
     */
    public FileDocumentSource(File file, @Nullable OWLDocumentFormat format, @Nullable String mime) {
        super(IRI.create(file), format, mime);
        this.file = checkNotNull(file, "file cannot be null");
    }

    @Override
    public Optional<InputStream> getInputStream() {
        try {
            return optional(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            LOGGER.error("File cannot be found", e);
            failedOnStreams.set(true);
            return emptyOptional();
        }
    }
}
