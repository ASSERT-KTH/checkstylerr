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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public abstract class AbstractLoadingIterable<T extends Resource> {

    protected static final int FIRST_PAGE = 1;

    protected final Map<Integer,Page<T>> pages = new HashMap<>();
    protected Map<String,Map<String,Integer>> facetCount;
    protected Map<? extends String,?> headers;
    protected Gson gson;
    protected PageableResourceEndpoint<T> endpoint;

    protected int pageIndex;
    protected int resourceIndex = 0;
    protected Integer totalCount;
    protected String nextUrl;

    protected ResourceDeserializer<T> deserializer;

    public AbstractLoadingIterable(JsonElement payload, PageableResourceEndpoint<T> endpoint,
            Gson gson, Map<? extends String,?> headers) {
        this.endpoint = endpoint;
        this.gson = gson;
        this.deserializer = new ResourceDeserializer<>(endpoint, gson);
        this.facetCount = getFacetCount(payload);
        this.totalCount = getTotalCount(payload);
        this.headers = headers;
    }

    public AbstractLoadingIterable(JsonElement payload, PageableResourceEndpoint<T> endpoint,
            Gson gson) {
        this(payload, endpoint, gson, new HashMap<>());
    }

    public abstract boolean hasNext();

    public T next() {
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

    public Optional<Integer> totalCount() {
        return Optional.ofNullable(totalCount);
    }

    public Supplier<NoSuchElementException> noSuchElementException() {
        return NoSuchElementException::new;
    }

    public Optional<Map<String,Map<String,Integer>>> facetCount() {
        return Optional.ofNullable(facetCount);
    }

    public Optional<String> nextUrl() {
        return Optional.ofNullable(this.nextUrl);
    }

    protected abstract void loadNextAndUpdateIndexes();

    protected int addPage(JsonElement payload) {
        Meta meta = getMeta(payload);
        pages.put(meta.getPage(), Page.from(meta, deserializer.contentsFrom(payload)));
        return meta.getPage();
    }

    protected Page<T> currentPage() {
        return pages.get(pageIndex);
    }

    protected Meta getMeta(JsonElement payload) {
        return deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
    }

    protected String getNextUrl(JsonElement payload) {
        Meta meta = deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
        return meta.getContinuosPage() != null ? meta.getContinuosPage() : meta.getNext();
    }

    protected Map<String,Map<String,Integer>> getFacetCount(JsonElement payload) {
        Meta meta = deserializer.metaFrom(payload).orElse(deserializer.emptyMeta());
        return meta.getFacetCount();
    }

    private Integer getTotalCount(JsonElement payload) {
        return getMeta(payload).getTotalCount();
    }

}
