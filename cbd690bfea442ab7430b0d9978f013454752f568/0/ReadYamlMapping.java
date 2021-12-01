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

import com.amihaiemil.eoyaml.exceptions.YamlReadingException;
import java.util.*;

/**
 * YamlMapping read from somewhere. YAML directives and
 * document start/end markers are ignored. This is assumed
 * to be a plain YAML mapping.
 * @checkstyle CyclomaticComplexity (300 lines)
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 2ec488aade84c15134155ec932ce6a254689e01b $
 * @since 1.0.0
 */
final class ReadYamlMapping extends BaseYamlMapping {

    /**
     * Yaml line just previous to the one where this mapping starts. E.g.
     * <pre>
     * 0  mapping:
     * 1    key1: elem1
     * 2    key2: elem2
     * </pre>
     * In the above example the mapping consists of keys key1 and key2, while
     * "previous" is line 0. If the mapping starts at the root, then line
     * "previous" is {@link com.amihaiemil.eoyaml.YamlLine.NullYamlLine}; E.g.
     * <pre>
     * 0  key1: elem1
     * 1  key2: elem2
     * </pre>
     */
    private YamlLine previous;

    /**
     * All the lines of this YAML document.
     */
    private final AllYamlLines all;

    /**
     * Only the significant lines of this YamlMapping.
     */
    private final YamlLines significant;

    /**
     * Ctor.
     * @param lines Given lines.
     */
    ReadYamlMapping(final AllYamlLines lines) {
        this(new YamlLine.NullYamlLine(), lines);
    }

    /**
     * Ctor.
     * @param previous Line just before the start of this mapping.
     * @param lines Given lines.
     */
    ReadYamlMapping(final YamlLine previous, final AllYamlLines lines) {
        this.previous = previous;
        this.all = lines;
        this.significant = new SameIndentationLevel(
            new WellIndented(
                new Skip(
                    lines,
                    line -> line.number() <= previous.number(),
                    line -> line.trimmed().startsWith("#"),
                    line -> line.trimmed().startsWith("---"),
                    line -> line.trimmed().startsWith("..."),
                    line -> line.trimmed().startsWith("%"),
                    line -> line.trimmed().startsWith("!!")
                )
            )
        );
    }

    @Override
    public Set<YamlNode> keys() {
        final Set<YamlNode> keys = new LinkedHashSet<>();
        for (final YamlLine line : this.significant) {
            final String trimmed = line.trimmed();
            if(trimmed.startsWith(":")) {
                continue;
            } else if ("?".equals(trimmed)) {
                keys.add(this.significant.toYamlNode(line));
            } else {
                if(!trimmed.contains(":")) {
                    throw new YamlReadingException(
                        "Expected scalar key on line " 
                        + (line.number() + 1) + "."
                        + " The line should have the format " 
                        + "'key: value' or 'key:'. "
                        + "Instead, the line is: "
                        + "[" + line.trimmed() + "]."
                    );
                }
                final String key = trimmed.substring(
                        0, trimmed.indexOf(":")).trim();
                if(!key.isEmpty()) {
                    keys.add(new PlainStringScalar(key));
                }
            }
        }
        return keys;
    }

    @Override
    public Collection<YamlNode> values() {
        final List<YamlNode> values = new LinkedList<>();
        for(final YamlNode key : this.keys()) {
            values.add(this.value(key));
        }
        return values;
    }

    @Override
    public YamlNode value(final YamlNode key) {
        final YamlNode value;
        if(key instanceof Scalar) {
            value = this.valueOfStringKey(((Scalar) key).value());
        } else {
            value = this.valueOfNodeKey(key);
        }
        return value;
    }

    @Override
    public Comment comment() {
        return new ReadComment(
            new FirstCommentFound(
                new Backwards(
                    new Skip(
                        this.all,
                        line -> {
                            final boolean skip;
                            if(this.previous.number() < 0) {
                                if(this.significant.iterator().hasNext()) {
                                    skip = line.number() >= this.significant
                                            .iterator().next().number();
                                } else {
                                    skip = false;
                                }
                            } else {
                                skip = line.number() >= this.previous.number();
                            }
                            return skip;
                        },
                        line -> line.trimmed().startsWith("---"),
                        line -> line.trimmed().startsWith("..."),
                        line -> line.trimmed().startsWith("%"),
                        line -> line.trimmed().startsWith("!!")
                    )
                )
            ),
            this
        );
    }

    @Override
    public YamlMapping yamlMapping(final YamlNode key) {
        final YamlMapping found;
        final YamlNode value = this.value(key);
        if(value instanceof ReadYamlMapping) {
            found = (ReadYamlMapping) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public YamlSequence yamlSequence(final YamlNode key) {
        final YamlSequence found;
        final YamlNode value = this.value(key);
        if(value instanceof ReadYamlSequence) {
            found = (ReadYamlSequence) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String string(final YamlNode key) {
        final String found;
        final YamlNode value = this.value(key);
        if(value instanceof ReadPlainScalar) {
            found = ((ReadPlainScalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String foldedBlockScalar(final YamlNode key) {
        final String found;
        final YamlNode value = this.value(key);
        if(value instanceof ReadFoldedBlockScalar) {
            found = ((ReadFoldedBlockScalar) value).toString();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<String> literalBlockScalar(final YamlNode key) {
        final Collection<String> found;
        final YamlNode value = this.value(key);
        if(value instanceof ReadLiteralBlockScalar) {
            found = Arrays.asList(
                ((ReadLiteralBlockScalar) value)
                    .value()
                    .split(System.lineSeparator())
            );
        } else {
            found = null;
        }
        return found;
    }

    /**
     * The YamlNode value associated with a String (scalar) key.
     * @param key String key.
     * @return YamlNode.
     */
    private YamlNode valueOfStringKey(final String key) {
        YamlNode value = null;
        for (final YamlLine line : this.significant) {
            final String trimmed = line.trimmed();
            if(trimmed.endsWith(key + ":")
                || trimmed.matches("^" + key + "\\:[ ]*\\>$")
                || trimmed.matches("^" + key + "\\:[ ]*\\|$")
            ) {
                value = this.significant.toYamlNode(line);
            } else if(trimmed.startsWith(key + ":")
                && trimmed.length() > 1
            ) {
                value = new ReadPlainScalar(this.all, line);
            }
        }
        return value;
    }

    /**
     * The YamlNode value associated with a YamlNode key
     * (a "complex" key starting with '?').
     * @param key YamlNode key.
     * @return YamlNode.
     */
    private YamlNode valueOfNodeKey(final YamlNode key) {
        YamlNode value = null;
        final Iterator<YamlLine> linesIt = this.significant.iterator();
        while(linesIt.hasNext()) {
            final YamlLine line = linesIt.next();
            final String trimmed = line.trimmed();
            if("?".equals(trimmed)) {
                final YamlNode keyNode = this.significant.toYamlNode(line);
                if(keyNode.equals(key)) {
                    final YamlLine colonLine = linesIt.next();
                    if(":".equals(colonLine.trimmed())
                        || colonLine.trimmed().matches("^\\:[ ]*\\>$")
                        || colonLine.trimmed().matches("^\\:[ ]*\\|$")
                    ) {
                        value = this.significant.toYamlNode(colonLine);
                    } else if(colonLine.trimmed().startsWith(":")
                        && (colonLine.trimmed().length() > 1)
                    ){
                        value = new ReadPlainScalar(this.all, colonLine);
                    } else {
                        throw new YamlReadingException(
                            "No value found for existing complex key: "
                          + System.lineSeparator()
                          + ((BaseYamlNode) key).indent(0)
                        );
                    }
                    break;
                }
            }
        }
        return value;
    }
}
