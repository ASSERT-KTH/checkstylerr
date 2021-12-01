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

/**
 * Builder of YamlSequence. Implementations should be immutable and thread-safe.
 * @author Salavat.Yalalov (s.yalalov@gmail.com)
 * @version $Id: 7beae335c153ff604f2c905171cb5d5c895f1172 $
 * @since 1.0.0
 */
public interface YamlSequenceBuilder {

    /**
     * Add a value to the sequence.
     * @param value String
     * @return This builder
     */
    default YamlSequenceBuilder add(final String value) {
        return this.add(value, "");
    }

    /**
     * Add a value to the sequence.
     * @param value String
     * @param comment Comment referring to the added value.
     * @return This builder
     */
    default YamlSequenceBuilder add(final String value, final String comment) {
        return this.add(
            Yaml.createYamlScalarBuilder().addLine(value).buildPlainScalar(),
            comment
        );
    }

    /**
     * Add a value to the sequence.
     * @param node YamlNode
     * @return This builder
     */
    default YamlSequenceBuilder add(final YamlNode node) {
        return this.add(node, "");
    }

    /**
     * Add a value to the sequence.
     * @param node YamlNode
     * @param comment Comment referring to the added YamlNode.
     * @return This builder
     */
    YamlSequenceBuilder add(final YamlNode node, final String comment);

    /**
     * Build the YamlSequence.
     * @return Built YamlSequence
     */
    default YamlSequence build() {
        return this.build("");
    }

    /**
     * Build the YamlSequence and specify a comment referring to it.
     * @param comment Comment about the built YamlSequence.
     * @return Built YamlSequence
     */
    YamlSequence build(final String comment);
}
