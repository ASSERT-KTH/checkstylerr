/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model.text;

import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for XHTML formatters.
 */
public class BaseXhtmlFormat {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(BaseXhtmlFormat.class);
    private static final XMLInputFactory XML_INPUT_FACTORY =
        XMLInputFactory.newInstance();
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    protected XMLStreamReader createXmlReader(Reader reader)
        throws XMLStreamException {
        return XML_INPUT_FACTORY.createXMLStreamReader(reader);
    }

    protected XMLStreamWriter createXmlWriter(Writer writer)
        throws XMLStreamException {
        return XML_OUTPUT_FACTORY.createXMLStreamWriter(writer);
    }

    protected boolean isDiv(XMLStreamReader reader) {
        return reader.getLocalName().equals("div");
    }

    protected boolean isSpan(XMLStreamReader reader) {
        return reader.getLocalName().equals("span");
    }

    protected boolean hasClass(XMLStreamReader reader,
                               String clazz) {
        String value = getClass(reader);
        return value != null && value.equals(clazz);
    }

    protected String getClass(XMLStreamReader reader) {
        return reader.getAttributeValue(null, "class");
    }

    protected void handleParseException(String message,
                                        XMLStreamReader reader)
        throws ParseException {
        handleException(message, reader.getLocation());
    }

    protected void handleXmlException(String message,
                                      XMLStreamException e)
        throws ParseException {
        message += ": " + e.getMessage();
        handleException(message , e.getLocation());
    }

    protected void handleException(String message,
                                   Location location)
        throws ParseException {
        int offset = location != null ? location.getCharacterOffset() : -1;
        throw new ParseException(message, offset);
    }
}
