/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.clustering.marshalling.spi.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;

/**
 * @author Paul Ferraro
 */
public class MapEntryExternalizer<T extends Map.Entry<Object, Object>> implements Externalizer<T> {

    private final Class<T> targetClass;
    private final BiFunction<Object, Object, T> factory;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    MapEntryExternalizer(Class targetClass, BiFunction<Object, Object, T> factory) {
        this.targetClass = targetClass;
        this.factory = factory;
    }

    @Override
    public void writeObject(ObjectOutput output, T entry) throws IOException {
        output.writeObject(entry.getKey());
        output.writeObject(entry.getValue());
    }

    @Override
    public T readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        return this.factory.apply(input.readObject(), input.readObject());
    }

    @Override
    public Class<? extends T> getTargetClass() {
        return this.targetClass;
    }

    @MetaInfServices(Externalizer.class)
    public static class SimpleEntryExternalizer extends MapEntryExternalizer<AbstractMap.SimpleEntry<Object, Object>> {
        public SimpleEntryExternalizer() {
            super(AbstractMap.SimpleEntry.class, AbstractMap.SimpleEntry<Object, Object>::new);
        }
    }

    @MetaInfServices(Externalizer.class)
    public static class SimpleImmutableEntryExternalizer extends MapEntryExternalizer<AbstractMap.SimpleImmutableEntry<Object, Object>> {
        public SimpleImmutableEntryExternalizer() {
            super(AbstractMap.SimpleImmutableEntry.class, AbstractMap.SimpleImmutableEntry<Object, Object>::new);
        }
    }
}
