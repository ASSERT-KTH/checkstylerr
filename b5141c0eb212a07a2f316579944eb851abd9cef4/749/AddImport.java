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

import javax.annotation.Nullable;

import org.semanticweb.owlapi.change.AddImportData;

/**
 * Represents an ontology change where an import statement is added to an
 * ontology.
 * 
 * @author Matthew Horridge, The University of Manchester, Information
 *         Management Group
 * @since 3.0.0
 */
public class AddImport extends ImportChange {

    /**
     * @param ont
     *        the ontology to which the change is to be applied
     * @param importDeclaration
     *        the import declaration
     */
    public AddImport(OWLOntology ont, OWLImportsDeclaration importDeclaration) {
        super(ont, importDeclaration);
    }

    @Override
    public AddImportData getChangeData() {
        return new AddImportData(getImportDeclaration());
    }

    @Override
    public int hashCode() {
        return getOntology().hashCode() * 37 + getImportDeclaration().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AddImport)) {
            return false;
        }
        AddImport other = (AddImport) obj;
        return getImportDeclaration().equals(other.getImportDeclaration());
    }

    @Override
    public String toString() {
        return "AddImport(" + getImportDeclaration() + " OntologyID(" + getOntology().getOntologyID() + "))";
    }

    @Override
    public void accept(OWLOntologyChangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <O> O accept(OWLOntologyChangeVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    public OWLOntologyChange reverseChange() {
        return new RemoveImport(getOntology(), getImportDeclaration());
    }
}
