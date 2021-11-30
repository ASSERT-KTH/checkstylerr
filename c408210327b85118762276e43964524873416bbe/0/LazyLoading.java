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

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class LazyLoading<T extends Resource> {

    static final int FIRST_PAGE = 1;

    // just to provide easier way to navigate back and forth in future
    final Map<Integer, Page<T>> pages = new HashMap<>();

    PageableResourceEndpoint<T> endpoint;

    int pageIndex;
    int resourceIndex = 0;
    Integer totalCount;

    Map<? extends String, ?> headers;

    ResourceDeserializer<T> deserializer;

    T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!currentPage().containsIndex(resourceIndex)) {
            loadNextAndUpdateIndexes();
        }
        try {
            return currentPage().at(resourceIndex++);
        } catch (Page.PageResourceDoesNotExist pne) {
            throw new NoSuchElementException(pne.getMessage());
        }
    }

    boolean hasNext() {
        return theCurrentPageHasContents() &&
                ((currentPage().containsIndex(resourceIndex) || currentPage().isNotLast()) && nextPageIsNotEmpty()
                        && nextPageContainsResources());
    }

    abstract boolean theCurrentPageHasContents();

    abstract boolean nextPageContainsResources();

    Integer getTotalCount(JsonElement payload) {
        Meta meta = deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
        return meta.getTotalCount();
    }

    Page<T> currentPage() {
        return pages.get(pageIndex);
    }

    Optional<Integer> totalCount() {
        return Optional.ofNullable(totalCount);
    }

    Supplier<NoSuchElementException> noSuchElementException() {
        return NoSuchElementException::new;
    }

    boolean nextPageIsNotEmpty() {
        if (lastPageItem(currentPage().getMeta()) && metaHasNext()) {
            loadNextAndUpdateIndexes();
            return (currentPage().items() > 0);
        }
        return true;
    }

    abstract void loadNextAndUpdateIndexes();

    boolean metaHasNext() {
        return nonNull(currentPage().getMeta().getNext());
    }

    int addPage(JsonElement payload) {
        long startTime = System.nanoTime();
        Meta meta = deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
        pages.put(meta.getPage(), Page.from(meta, deserializer.contentsFrom(payload)));
        long endTime = System.nanoTime();
        log.debug("time to process json - {} seconds", (double) (endTime - startTime) / 1000000000.0);
        return meta.getPage();
    }

    boolean lastPageItem(AbstractMeta meta) {
        return resourceIndex == meta.getPerPage();
    }

}
