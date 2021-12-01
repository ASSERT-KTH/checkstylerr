/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.nio.reactor;

import java.nio.channels.SelectionKey;

import org.apache.http.util.Args;

/**
 * Helper class, representing an entry on an {@link java.nio.channels.SelectionKey#interestOps(int)
 * interestOps(int)} queue.
 *
 * @since 4.1
 */
class InterestOpEntry {

    private final SelectionKey key;
    private final int eventMask;

    public InterestOpEntry(final SelectionKey key, final int eventMask) {
        super();
        Args.notNull(key, "Selection key");
        this.key = key;
        this.eventMask = eventMask;
    }

    public SelectionKey getSelectionKey() {
        return this.key;
    }

    public int getEventMask() {
        return this.eventMask;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof InterestOpEntry) {
            final InterestOpEntry that = (InterestOpEntry) obj;
            return this.key.equals(that.key);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

}
