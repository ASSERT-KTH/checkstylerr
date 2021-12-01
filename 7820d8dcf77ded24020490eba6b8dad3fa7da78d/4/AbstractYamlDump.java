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
 * A Yaml representer.
 * @author Sherif Waly (sherifwaly95@gmail.com)
 * @version $Id: 2dfb7b9113fac6cc800e2b754823d32d46c71175 $
 * @since 1.0.0
 * @todo #30:30m/DEV Add method ``serlialize()`` in YamlNode interface
 *  and implement it in the YamlNode implementors (e.g. Scalar) to serlialize
 *  it and return the Yaml node as a Yaml tree.
 */
public abstract class AbstractYamlDump {

    /**
     * Turn it into Yaml.
     * @return Yaml node
     */
    abstract YamlNode represent();

    /**
     * Check if the given property is a 'leaf'. For instance, a String is
     * considered a leaf, we don't need to go deeper to check for its
     * properties, we can directly print it.
     * @param property Tested property
     * @return Boolean
     */
    protected final boolean leafProperty(final Object property) {
        boolean leaf = false;
        Class<?> clazz = property.getClass();
        try {
            if(clazz.getName().startsWith("java.lang.")
                || clazz.getName().startsWith("java.util.")){
                if(clazz.getMethod("toString").getDeclaringClass()
                        .equals(clazz)
                ) {
                    leaf = true;
                }
            }
        } catch (final NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (final SecurityException ex) {
            ex.printStackTrace();
        }
        return leaf;
    }
}
