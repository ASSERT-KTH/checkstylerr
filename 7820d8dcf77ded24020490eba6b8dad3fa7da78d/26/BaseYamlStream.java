/**
 * Copyright (c) 2016-2020, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.eoyaml;

import java.util.Collection;
import java.util.Iterator;

/**
 * Base YamlStream which all implementations should extend.
 * It implementing toString(), equals, hashcode and compareTo methods.
 * <br><br>
 * These methods should be default methods on the interface,
 * but we are not allowed to have default implementations of java.lang.Object
 * methods.<br><br>
 * This class also offers the package-protected indent(...) method, which
 * returns the indented value of the stream, used in printing YAML. This
 * method should NOT be visible to users.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 12a9c2187f76b1dae1643841c31ae7885e0ee73f $
 * @since 4.0.0
 */
abstract class BaseYamlStream extends BaseYamlNode implements YamlStream {

    @Override
    public int hashCode() {
        int hash = 0;
        for(final YamlNode node : this.values()) {
            hash += node.hashCode();
        }
        return hash;
    }

    /**
     * Equals method for YamlStream. It returns true if the compareTo(...)
     * method returns 0.
     * @param other The YamlStream to which this is compared.
     * @return True or false
     */
    @Override
    public boolean equals(final Object other) {
        final boolean result;
        if (other == null || !(other instanceof YamlStream)) {
            result = false;
        } else if (this == other) {
            result = true;
        } else {
            result = this.compareTo((YamlStream) other) == 0;
        }
        return result;
    }

    /**
     * Compare this Sequence to another node.<br><br>
     *
     * A YamlStream is always considered greater than a Scalar,
     * a YamlSequence or a YamlMapping.
     *
     * If other is a YamlStream, their integer lengths are compared - the one
     * with the greater length is considered greater. If the lengths are equal,
     * then the 2 YamlStreams are equal if all elements are equal. If the
     * elements are not identical, the comparison of the first unequal
     * elements is returned.
     *
     * @param other The other YamlNode.
     * @checkstyle NestedIfDepth (100 lines)
     * @return
     *   a value &lt; 0 if this &lt; other <br>
     *   0 if this == other or <br>
     *   a value &gt; 0 if this &gt; other
     */
    @Override
    public int compareTo(final YamlNode other) {
        int result = 0;
        if (other == null || other instanceof Scalar) {
            result = 1;
        } else if (other instanceof YamlSequence) {
            result = 1;
        } else if (other instanceof YamlMapping) {
            result = 1;
        } else if (this != other) {
            final Collection<YamlNode> nodes = this.values();
            final Collection<YamlNode> others = ((YamlStream) other).values();
            if(nodes.size() > others.size()) {
                result = 1;
            } else if (nodes.size() < others.size()) {
                result = -1;
            } else {
                final Iterator<YamlNode> iterator = others.iterator();
                final Iterator<YamlNode> here = nodes.iterator();
                while(iterator.hasNext()) {
                    result = here.next().compareTo(iterator.next());
                    if(result != 0) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Indent this YamlStream. It will take all elements and separate
     * them with "---". It also starts with "---".
     *
     * Keep this method package-protected, it should NOT be visible
     * to users.
     *
     * If the YamlStream is empty, it will just print:
     * <pre>
     * ---
     * ...
     * </pre>
     *
     * Here is an example of printed YamlStream:
     * <pre>
     * ---
     *   architect: amihaiemil
     *   developers:
     *     - amihaiemil
     *     - salikjan
     * ---
     *   architect: yegor256
     *   developers:
     *     - paolo
     *     - mario
     * </pre>
     * @param indentation Integer specifying the root indentation level.
     *  Usually 0, but it may be greater than 0 if this YamlNode should
     *  be nested within a bigger YAML.
     * @return String.
     */
    final String indent(final int indentation) {
        if(indentation < 0) {
            throw new IllegalArgumentException(
                "Indentation level has to be >=0"
            );
        }
        final StringBuilder print = new StringBuilder();
        final String newLine = System.lineSeparator();
        int spaces = indentation;
        final StringBuilder indent = new StringBuilder();
        while (spaces > 0) {
            indent.append(" ");
            spaces--;
        }
        final Collection<YamlNode> values = this.values();
        String printed;
        if(values.size() == 0) {
            print
                .append(indent).append("---")
                .append(newLine)
                .append(indent).append("...");
            printed = print.toString();
        } else {
            for (final YamlNode node : values) {
                final BaseYamlNode indentable = (BaseYamlNode) node;
                print.append(indent)
                    .append("---")
                    .append(newLine);
                print.append(indentable.indent(indentation + 2));
                print.append(newLine);
            }
            printed = print.toString();
            if(printed.length() > 0) {
                printed = printed.substring(0, printed.length() - 1);
            }
        }
        return printed;
    }

    /**
     * When printing a stream just indent it to 0, no need for other wrappers.
     * @return Printed stream of YAML Documents
     */
    @Override
    public final String toString() {
        return this.indent(0);
    }
}
