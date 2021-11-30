package com.piksel.sequoia.clientsdk.resource;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;

import com.google.api.client.util.Key;
import com.piksel.sequoia.annotations.Internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents meta and contents for a page of a given {@link Resource}. Provides
 * convenient methods to check metadata.
 */
@Internal
@AllArgsConstructor(staticName = "from")
public class Page<T extends Resource> {

    @Key
    private final AbstractMeta meta;
    @Key
    private final List<T> contents;

    public T at(int index) {
        if (index >= contents.size()) {
            throw new PageResourceDoesNotExist(index);
        }
        return contents.get(index);
    }

    public boolean isLast() {
        return isNotPresent(meta.getNext());
    }

    public int items() {
        return contents.size();
    }

    public boolean isNotLast() {
        return !isLast();
    }

    public boolean containsIndex(int index) {
        return contents.size() > index;
    }

    public AbstractMeta getMeta() {
        return meta;
    }

    private boolean isNotPresent(String value) {
        return isNullOrEmpty(value);
    }

    @Getter
    public static class PageResourceDoesNotExist extends IndexOutOfBoundsException {
        private static final long serialVersionUID = 1L;
        private final int index;

        public PageResourceDoesNotExist(int index) {
            super("Requested element from page but no such index exists: " + index);
            this.index = index;
        }

    }

}
