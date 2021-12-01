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
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;
import org.wildfly.clustering.marshalling.spi.IndexExternalizer;

/**
 * Externalizers for implementations of {@link SortedSet}.
 * Requires additional serialization of the comparator.
 * @author Paul Ferraro
 */
public class SortedSetExternalizer<T extends SortedSet<Object>> implements Externalizer<T> {

    private final Class<T> targetClass;
    private final Function<Comparator<? super Object>, T> factory;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    SortedSetExternalizer(Class targetClass, Function<Comparator<? super Object>, T> factory) {
        this.targetClass = targetClass;
        this.factory = factory;
    }

    @Override
    public void writeObject(ObjectOutput output, T set) throws IOException {
        output.writeObject(set.comparator());
        CollectionExternalizer.writeCollection(output, set);
    }

    @Override
    public T readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Comparator<? super Object> comparator = (Comparator<? super Object>) input.readObject();
        int size = IndexExternalizer.VARIABLE.readData(input);
        return CollectionExternalizer.readCollection(input, this.factory.apply(comparator), size);
    }

    @Override
    public Class<? extends T> getTargetClass() {
        return this.targetClass;
    }

    @MetaInfServices(Externalizer.class)
    public static class ConcurrentSkipListSetExternalizer extends SortedSetExternalizer<ConcurrentSkipListSet<Object>> {
        public ConcurrentSkipListSetExternalizer() {
            super(ConcurrentSkipListSet.class, ConcurrentSkipListSet<Object>::new);
        }
    }

    @MetaInfServices(Externalizer.class)
    public static class TreeSetExternalizer extends SortedSetExternalizer<TreeSet<Object>> {
        public TreeSetExternalizer() {
            super(TreeSet.class, TreeSet<Object>::new);
        }
    }
}
