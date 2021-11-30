/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.web.deployment.runtime.LocaleCharsetInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
* node for locale-charset-info tag
*
* @author Jerome Dochez
*/
public class LocaleCharsetInfoNode extends RuntimeDescriptorNode<LocaleCharsetInfo> {

    /**
     * Initialize the child handlers
     */
    public LocaleCharsetInfoNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.LOCALE_CHARSET_MAP),
            LocaleCharsetMapNode.class, "addLocaleCharsetMap");
    }

    protected LocaleCharsetInfo descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public LocaleCharsetInfo getDescriptor() {
        if (descriptor==null) {
            descriptor = new LocaleCharsetInfo();
        }
        return descriptor;
    }


    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (element.getQName().equals(RuntimeTagNames.LOCALE_CHARSET_INFO)) {
            LocaleCharsetInfo info = getDescriptor();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.DEFAULT_LOCALE.equals(attributes.getQName(i))) {
                    info.setAttributeValue(LocaleCharsetInfo.DEFAULT_LOCALE, attributes.getValue(i));
                }
            }
        } else if (element.getQName().equals(RuntimeTagNames.PARAMETER_ENCODING)) {
            LocaleCharsetInfo info = getDescriptor();
            info.setParameterEncoding(true);
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.DEFAULT_CHARSET.equals(attributes.getQName(i))) {
                    info.setAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING, LocaleCharsetInfo.DEFAULT_CHARSET,
                        attributes.getValue(i));
                }
                if (RuntimeTagNames.FORM_HINT_FIELD.equals(attributes.getQName(i))) {
                    info.setAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING, LocaleCharsetInfo.FORM_HINT_FIELD,
                        attributes.getValue(i));
                }
            }
        } else {
            super.startElement(element, attributes);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, LocaleCharsetInfo descriptor) {
        Element locale = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // locale-charset-map+
        if (descriptor.sizeLocaleCharsetMap() > 0) {
            LocaleCharsetMapNode lcmn = new LocaleCharsetMapNode();
            for (int i = 0; i < descriptor.sizeLocaleCharsetMap(); i++) {
                lcmn.writeDescriptor(locale, RuntimeTagNames.LOCALE_CHARSET_MAP, descriptor.getLocaleCharsetMap(i));
            }
        }

        // <!ELEMENT parameter-encoding EMPTY>
        // <!ATTLIST parameter-encoding form-hint-field CDATA #IMPLIED
        // default-charset CDATA #IMPLIED>
        if (descriptor.isParameterEncoding()) {
            Element parameter = appendChild(locale, RuntimeTagNames.PARAMETER_ENCODING);

            if (descriptor.getAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING,
                LocaleCharsetInfo.FORM_HINT_FIELD) != null) {
                setAttribute(parameter, RuntimeTagNames.FORM_HINT_FIELD,
                    descriptor.getAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING, LocaleCharsetInfo.FORM_HINT_FIELD));
            }

            if (descriptor.getAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING,
                LocaleCharsetInfo.DEFAULT_CHARSET) != null) {
                setAttribute(parameter, RuntimeTagNames.DEFAULT_CHARSET,
                    descriptor.getAttributeValue(LocaleCharsetInfo.PARAMETER_ENCODING, LocaleCharsetInfo.DEFAULT_CHARSET));
            }
        }

        // default_locale
        if (descriptor.getAttributeValue(LocaleCharsetInfo.DEFAULT_LOCALE) != null) {
            setAttribute(locale, RuntimeTagNames.DEFAULT_LOCALE,
                descriptor.getAttributeValue(LocaleCharsetInfo.DEFAULT_LOCALE));
        }
        return locale;
    }

}
