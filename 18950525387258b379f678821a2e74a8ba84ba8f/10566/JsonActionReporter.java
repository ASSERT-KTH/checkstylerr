/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.common;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Writes command output to a json stream
 *
 * @author Ludovic Champenois
 */
@Service(name = "json")
@PerLookup
public class JsonActionReporter extends ActionReporter {

    /*
    top is true only for the first toplevel message to emit more data like
     * command name and exit_code of the command
     *
     */
    private boolean top = true;

    /** Creates a new instance of JsonActionReporter */
    public JsonActionReporter() {
    }

    @Override
    public void writeReport(OutputStream os) throws IOException {
        PrintWriter writer = new PrintWriter(os);

        write(topMessage, writer);
        if (exception != null) {
            writer.println("Exception raised during operation : <br>");
            exception.printStackTrace(writer);
        }
        if (subActions.size() > 0) {
            writer.println(quote(", number_subactions") + ":" + quote("" + subActions.size()));
        }
        writer.flush();
    }

    private void write(MessagePart part, PrintWriter writer) {
        writer.println("{ " + quote("name") + ":" + quote(part.getMessage()));
        if (top) {
            writer.println(", " + quote("command") + ":" + quote(actionDescription));
            writer.println(", " + quote("exit_code") + ":" + quote("" + this.exitCode));
            top = false;
        }
        writeProperties(part.getProps(), writer);
        boolean first = true;
        for (MessagePart child : part.getChildren()) {
            if (first == true) {
                writer.println(", " + quote("result") + " : [");
            } else {
                writer.println(",");

            }
            first = false;
            write(child, writer);

        }
        if (first == false) { //close the array

            writer.println("]");
        }

        writer.println("}");

    }

    private void writeProperties(Properties props, PrintWriter writer) {
        if (props == null || props.size() == 0) {
            return;
        }
        StringBuilder result = new StringBuilder(",");
        result.append(quote("properties")).append(" : {");
        String sep = "";
        for (Map.Entry entry : props.entrySet()) {
            String line = quote("" + entry.getKey()) + " : ";
            Object value = entry.getValue();
            if (value instanceof List) {
                line += encodeList((List)value);
            } else if (value instanceof Map) {
                line += encodeMap((Map)value);
            } else {
                line += quote("" + value.toString());
            }
            result.append(sep).append(line);

            sep = ",";
        }
        writer.println(result.append("}").toString());

    }

    private String encodeList (List list) {
        StringBuilder result = new StringBuilder("[");
        String sep = "";
        for (Object entry : list) {
            if (entry instanceof List) {
                result.append(sep).append(encodeList((List)entry));
            } else if (entry instanceof Map) {
                result.append(sep).append(encodeMap((Map)entry));
            } else {
                result.append(sep).append(quote (entry.toString()));
            }

            sep = ",";
        }
        return result.append("]").toString();
    }

    private String encodeMap (Map map) {
        StringBuilder result = new StringBuilder("{");
        String sep = "";
        for (Map.Entry entry : (Set<Map.Entry>)map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            result.append(sep).append(quote(key)).append(":");

            if (value instanceof List) {
                result.append(encodeList((List) value));
            } else if (value instanceof Map) {
                result.append(encodeMap((Map) value));
            } else {
                result.append(quote(value.toString()));
            }
            sep = ",";
        }

        return result.append("}").toString();
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places.
     */
    private String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
