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

import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@PublicEvolving
public final class LazyLoadingResourceIterable<T extends Resource> extends LazyLoading<T> implements ResourceIterable<T> {

    private Map<String, Map<String, Integer>> facetCount;

    public LazyLoadingResourceIterable(JsonElement payload, PageableResourceEndpoint<T> endpoint, Gson gson) {
        log.debug("Creating lazy loading list iterable for [{}]", endpoint.getEndpointType());
        this.endpoint = endpoint;
        this.deserializer = new ResourceDeserializer<>(endpoint, gson);
        this.pageIndex = addPage(payload);
        this.totalCount = getTotalCount(payload);
        this.facetCount = getFacetCount(payload);
    }

    @Override
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    boolean theCurrentPageHasContents() {
        return currentPage().items() > 0;
    }

    @Override
    boolean nextPageContainsResources() {
        return true;
    }

    @Override
    public T next() {
        return super.next();
    }

    @Override
    public T single() {
        if (pageIndex != FIRST_PAGE || currentPage().isNotLast() || currentPage().items() != 1) {
            throw new NotSingularException();
        }
        return next();
    }

    @Override
    public Optional<Integer> totalCount() {
        return super.totalCount();
    }

    @Override
    public Optional<Map<String, Map<String, Integer>>> facetCount() {
        return Optional.ofNullable(facetCount);
    }

    private Map<String, Map<String, Integer>> getFacetCount(JsonElement payload) {
        Meta meta = deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
        return meta.getFacetCount();
    }

    void loadNextAndUpdateIndexes() {
        Optional<JsonElement> payload = endpoint.getPagedResource(currentPage().getMeta().getNext());
        pageIndex = addPage(payload.orElseThrow(noSuchElementException()));
        resourceIndex = 0;
    }

}
