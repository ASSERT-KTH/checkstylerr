/**
 * Copyright (c) 2016-2020, Mihai Emil Andronache
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * YamlSequence reflected from a Collection or an array of Object.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: b028d132696c738dfdc8351d71be07646f4548b6 $
 * @since 4.3.3
 */
final class ReflectedYamlSequence extends BaseYamlSequence {

    /**
     * If the value is any of these types, it is a Scalar.
     */
    private static final List<Class> SCALAR_TYPES = Arrays.asList(
        Integer.class, Long.class, Float.class, Double.class, Short.class,
        String.class, Boolean.class, Character.class, Byte.class
    );

    /**
     * Object sequence.
     */
    private final Collection<Object> sequence;

    /**
     * Constructor.
     * @param sequence Collection or array of Object.
     */
    ReflectedYamlSequence(final Object sequence) {
        if(sequence instanceof Collection) {
            this.sequence = (Collection<Object>) sequence;
        } else if(sequence.getClass().isArray()) {
            final Object[] array = (Object[]) sequence;
            this.sequence = Arrays.asList(array);
        } else {
            throw new IllegalArgumentException(
                "YamlSequence can only be reflected "
              + "from a Collection or from an array."
            );
        }
    }

    @Override
    public Collection<YamlNode> values() {
        final List<YamlNode> values = new ArrayList<>();
        for(final Object value : this.sequence) {
            values.add(this.objectToYamlNode(value));
        }
        return values;
    }

    @Override
    public Comment comment() {
        return new Comment() {
            @Override
            public YamlNode yamlNode() {
                return ReflectedYamlSequence.this;
            }

            @Override
            public String value() {
                return "";
            }
        };
    }

    /**
     * Turn a Java Object to an appropriate YAML Node.
     * @param value Object value.
     * @return YamlNode.
     */
    private YamlNode objectToYamlNode(final Object value) {
        final YamlNode node;
        if(value == null || SCALAR_TYPES.contains(value.getClass())) {
            node = new ReflectedYamlScalar(value);
        } else if(value instanceof Collection || value.getClass().isArray()){
            node = new ReflectedYamlScalar(value);
        } else {
            node = new ReflectedYamlMapping(value);
        }
        return node;
    }
}
