/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.registry;

import java.util.Map;

import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

/**
 * Clustered registry abstraction that stores a unique key/value per node.
 *
 * @param <K> the type of the registry entry key
 * @param <V> the type of the registry entry value
 * @author Paul Ferraro
 */
public interface Registry<K, V> extends AutoCloseable {

    /**
     * Listener for added, updated and removed entries.
     */
    interface Listener<K, V> {
        /**
         * Called when new entries have been added.
         *
         * @param added a map of entries that have been added
         */
        void addedEntries(Map<K, V> added);

        /**
         * Called when existing entries have been updated.
         *
         * @param updated a map of entries that have been updated
         */
        void updatedEntries(Map<K, V> updated);

        /**
         * Called when entries have been removed.
         *
         * @param removed a map of entries that have been removed
         */
        void removedEntries(Map<K, V> removed);
    }

    /**
     * Returns the group associated with this factory.
     *
     * @return a group
     */
    Group getGroup();

    /**
     * Adds a listener to this registry.
     *
     * @param listener a registry listener
     */
    void addListener(Listener<K, V> listener);

    /**
     * Adds a listener from this registry.
     *
     * @param listener a registry listener
     */
    void removeListener(Listener<K, V> listener);

    /**
     * Returns all registry entries in this group.
     *
     * @return a map for entries
     */
    Map<K, V> getEntries();

    /**
     * Returns the registry entry for the specified node.
     *
     * @param node a node
     * @return the node's registry entry, or null if undefined
     */
    Map.Entry<K, V> getEntry(Node node);

    /**
     * Returns the local registry entry.
     *
     * @return the registry entry of the local node
     * @deprecated Use {@link #getEntry(Node)} instead.
     */
    @Deprecated default Map.Entry<K, V> getLocalEntry() {
        return this.getEntry(this.getGroup().getLocalNode());
    }

    /**
     * Removes our entry from the registry.
     * Once closed, the registry can no longer be accessed.
     */
    @Override
    void close();
}
