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
package org.semanticweb.owlapi.rdf.turtle.renderer;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.RDFLiteral;
import org.semanticweb.owlapi.io.RDFNode;
import org.semanticweb.owlapi.io.RDFResource;
import org.semanticweb.owlapi.io.RDFResourceBlankNode;
import org.semanticweb.owlapi.io.RDFResourceIRI;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.rdf.RDFRendererBase;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.EscapeUtils;
import org.semanticweb.owlapi.util.VersionInfo;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics Group
 * @since 2.2.0
 */
public class TurtleRenderer extends RDFRendererBase {

    private final PrintWriter writer;
    private final PrefixManager pm;
    private final Set<RDFResource> pending = new HashSet<>();
    private final Deque<RDFResourceBlankNode> nodesToRenderSeparately = new LinkedList<>();
    private final String base;
    private final OWLDocumentFormat format;
    int bufferLength = 0;
    int lastNewLineIndex = 0;
    protected final Deque<Integer> tabs = new LinkedList<>();
    int level = 0;

    /**
     * @param ontology ontology
     * @param writer writer
     * @param format format
     */
    public TurtleRenderer(OWLOntology ontology, Writer writer, OWLDocumentFormat format) {
        super(ontology, format, ontology.getOWLOntologyManager().getOntologyWriterConfiguration());
        this.format = checkNotNull(format, "format cannot be null");
        this.writer = new PrintWriter(writer);
        pm = new DefaultPrefixManager();
        if (!ontology.isAnonymous()) {
            String ontologyIRIString = ontology.getOntologyID().getOntologyIRI().get().toString();
            String defaultPrefix = ontologyIRIString;
            if (!ontologyIRIString.endsWith("/")) {
                defaultPrefix = ontologyIRIString + '#';
            }
            pm.setDefaultPrefix(defaultPrefix);
        }
        if (format instanceof PrefixDocumentFormat) {
            PrefixDocumentFormat prefixFormat = (PrefixDocumentFormat) format;
            pm.copyPrefixesFrom(prefixFormat);
            pm.setPrefixComparator(prefixFormat.getPrefixComparator());
        }
        base = "";
    }

    private void writeNamespaces() {
        pm.getPrefixName2PrefixMap().forEach((k, v) -> {
            write("@prefix ");
            write(k);
            writeSpace();
            writeAsURI(v);
            write(" .");
            writeNewLine();
        });
    }

    protected void pushTab() {
        tabs.push(getIndent());
    }

    protected void popTab() {
        if (!tabs.isEmpty()) {
            tabs.pop();
        }
    }

    private void write(String s) {
        int newLineIndex = s.indexOf('\n');
        if (newLineIndex != -1) {
            lastNewLineIndex = bufferLength + newLineIndex;
        }
        writer.write(s);
        bufferLength += s.length();
    }

    private int getCurrentPos() {
        return bufferLength;
    }

    private Integer getIndent() {
        return Integer.valueOf(getCurrentPos() - lastNewLineIndex);
    }

    private void writeAsURI(String s) {
        write("<");
        if (s.startsWith(base)) {
            write(s.substring(base.length()));
        } else {
            write(s);
        }
        write(">");
    }

    private void write(IRI iri) {
        if (NodeID.isAnonymousNodeIRI(iri)) {
            write(iri.toString());
        } else if (iri.equals(ontology.getOntologyID().getOntologyIRI().orElse(null))) {
            writeAsURI(iri.toString());
        } else {
            String name = pm.getPrefixIRI(iri);
            if (name == null) {
                // No QName!
                // As this is not an XML output, qnames are not necessary; other
                // splits are allowed.
                name = forceSplitIfPrefixExists(iri);
            }
            if (name == null) {
                // no qname and no matching prefix
                writeAsURI(iri.toString());
            } else {
                if (name.endsWith(".")) {
                    writeAsURI(iri.toString());
                } else if (name.indexOf(':') != -1) {
                    write(name);
                } else {
                    write(":");
                    write(name);
                }
            }
        }
    }

    // TODO move to PrefixManager
    @Nullable
    private String forceSplitIfPrefixExists(IRI iri) {
        List<Map.Entry<String, String>> prefixName2PrefixMap = new ArrayList<>(
            pm.getPrefixName2PrefixMap().entrySet());
        // sort the entries in reverse lexicographic order by value (longest
        // prefix first)
        Collections.sort(prefixName2PrefixMap, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        String actualIRI = iri.toString();
        for (Map.Entry<String, String> e : prefixName2PrefixMap) {
            if (actualIRI.startsWith(e.getValue()) && noSplits(actualIRI, e.getValue().length())) {
                return e.getKey() + actualIRI.substring(e.getValue().length());
            }
        }
        return null;
    }

    private boolean noSplits(String s, int index) {
        return s.indexOf('#', index) < 0 && s.indexOf('/', index) < 0;
    }

    private void writeNewLine() {
        write("\n");
        int tabIndent = 0;
        if (!tabs.isEmpty()) {
            tabIndent = tabs.peek().intValue();
        }
        for (int i = 1; i < tabIndent; i++) {
            writeSpace();
        }
    }

    protected void writeAt() {
        write("@");
    }

    protected void writeSpace() {
        write(" ");
    }

    private void write(RDFNode node) {
        if (node.isLiteral()) {
            write((RDFLiteral) node);
        } else {
            write((RDFResource) node);
        }
    }

    private void write(RDFLiteral node) {
        if (!node.isPlainLiteral()) {
            if (node.getDatatype().equals(XSDVocabulary.INTEGER.getIRI())) {
                write(node.getLexicalValue());
            } else if (node.getDatatype().equals(XSDVocabulary.DECIMAL.getIRI())) {
                write(node.getLexicalValue());
            } else {
                writeStringLiteral(node.getLexicalValue());
                if (node.hasLang()) {
                    writeAt();
                    write(node.getLang());
                } else {
                    write("^^");
                    write(node.getDatatype());
                }
            }
        } else {
            writeStringLiteral(node.getLexicalValue());
            if (node.hasLang()) {
                writeAt();
                write(node.getLang());
            }
        }
    }

    private void writeStringLiteral(String literal) {
        String escapedLiteral = EscapeUtils.escapeString(literal);
        if (escapedLiteral.indexOf('\n') != -1) {
            write("\"\"\"");
            write(escapedLiteral);
            write("\"\"\"");
        } else {
            write("\"");
            write(escapedLiteral);
            write("\"");
        }
    }

    private void write(RDFResource node) {
        if (!node.isAnonymous()) {
            write(node.getIRI());
        } else {
            pushTab();
            if (!isObjectList(node)) {
                render(node);
            } else {
                // List - special syntax
                List<RDFNode> list = new ArrayList<>();
                toJavaList(node, list);
                pushTab();
                write("(");
                writeSpace();
                pushTab();
                for (Iterator<RDFNode> it = list.iterator(); it.hasNext(); ) {
                    write(verifyNotNull(it.next()));
                    if (it.hasNext()) {
                        writeNewLine();
                    }
                }
                popTab();
                writeNewLine();
                write(")");
                popTab();
            }
            popTab();
        }
    }

    @Override
    protected void beginDocument() {
        // Namespaces
        writeNamespaces();
        write("@base ");
        write("<");
        if (!ontology.isAnonymous()) {
            write(ontology.getOntologyID().getOntologyIRI().get().toString());
        } else {
            write(Namespaces.OWL.toString());
        }
        write("> .\n\n");
        // Ontology URI
    }

    @Override
    protected void endDocument() {
        writeComment(VersionInfo.getVersionInfo().getGeneratedByMessage());
        if (!format.isAddMissingTypes()) {
            // missing type declarations could have been omitted, adding a
            // comment to document it
            writeComment("Warning: type declarations were not added automatically.");
        }
        writer.flush();
    }

    @Override
    protected void writeClassComment(OWLClass cls) {
        writeComment(cls.getIRI().toString());
    }

    @Override
    protected void writeObjectPropertyComment(OWLObjectProperty prop) {
        writeComment(prop.getIRI().toString());
    }

    @Override
    protected void writeDataPropertyComment(OWLDataProperty prop) {
        writeComment(prop.getIRI().toString());
    }

    @Override
    protected void writeIndividualComments(OWLNamedIndividual ind) {
        writeComment(ind.getIRI().toString());
    }

    @Override
    protected void writeAnnotationPropertyComment(OWLAnnotationProperty prop) {
        writeComment(prop.getIRI().toString());
    }

    @Override
    protected void writeDatatypeComment(OWLDatatype datatype) {
        writeComment(datatype.getIRI().toString());
    }

    private void writeComment(String comment) {
        write("###  ");
        write(comment);
        writeNewLine();
    }

    @Override
    protected void endObject() {
        writeNewLine();
    }

    @Override
    protected void writeBanner(String name) {
        writer.print("#################################################################\n#    ");
        writer.println(name);
        writer.println("#################################################################\n");
    }

    @Override
    public void render(RDFResource node) {
        level++;
        Collection<RDFTriple> triples;
        if (pending.contains(node)) {
            // We essentially remove all structure sharing during parsing - any
            // cycles therefore indicate a bug!
            triples = Collections.emptyList();
        } else {
            triples = getRDFGraph().getTriplesForSubject(node);
        }
        pending.add(node);
        RDFResource lastSubject = null;
        RDFResourceIRI lastPredicate = null;
        boolean first = true;
        for (RDFTriple triple : triples) {
            RDFResource subj = triple.getSubject();
            RDFResourceIRI pred = triple.getPredicate();
            RDFNode object = triple.getObject();
            if (lastSubject != null && (subj.equals(lastSubject) || subj.isAnonymous())) {
                if (lastPredicate != null && pred.equals(lastPredicate)) {
                    // Only the object differs from previous triple
                    // Just write the object
                    write(" ,");
                    writeNewLine();
                    if (object.isAnonymous() && object.isIndividual() && object.shouldOutputId()) {
                        if (!pending.contains(object)) {
                            nodesToRenderSeparately.add((RDFResourceBlankNode) object);
                        }
                        write(object.getIRI());
                    } else {
                        write(object);
                    }
                } else {
                    // The predicate, object differ from previous triple
                    // Just write the predicate and object
                    write(" ;");
                    popTab();
                    writeNewLine();
                    write(triple.getPredicate());
                    writeSpace();
                    pushTab();
                    if (object.isAnonymous() && object.isIndividual() && object.shouldOutputId()) {
                        if (!pending.contains(object)) {
                            nodesToRenderSeparately.add((RDFResourceBlankNode) object);
                        }
                        write(object.getIRI());
                    } else {
                        write(object);
                    }
                }
            } else {
                if (!first) {
                    popTab();
                    popTab();
                    writeNewLine();
                }
                // Subject, predicate and object are different from last triple
                if (!node.isAnonymous()) {
                    write(subj);
                    writeSpace();
                } else if (node.isIndividual() && node.shouldOutputId()) {
                    write(subj.getIRI());
                    writeSpace();
                } else {
                    pushTab();
                    write("[");
                    writeSpace();
                }
                pushTab();
                write(triple.getPredicate());
                writeSpace();
                pushTab();
                if (object.isAnonymous() && object.isIndividual() && object.shouldOutputId()) {
                    if (!pending.contains(object)) {
                        nodesToRenderSeparately.add((RDFResourceBlankNode) object);
                    }
                    write(object.getIRI());
                } else {
                    write(object);
                }
            }
            lastSubject = subj;
            lastPredicate = pred;
            first = false;
        }
        if (node.isAnonymous()) {
            popTab();
            popTab();
            if (!node.isIndividual() || !node.shouldOutputId()) {
                if (triples.isEmpty()) {
                    write("[ ");
                } else {
                    writeNewLine();
                }
                write("]");
            }
            popTab();
        } else {
            popTab();
            popTab();
        }
        if (level == 1 && !triples.isEmpty()) {
            write(" .\n\n");
        }
        writer.flush();
        level--;
        while (!nodesToRenderSeparately.isEmpty()) {
            render(nodesToRenderSeparately.poll());
        }
        pending.remove(node);
    }
}
