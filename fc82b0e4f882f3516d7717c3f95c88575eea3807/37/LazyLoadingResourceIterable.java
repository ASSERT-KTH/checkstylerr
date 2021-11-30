package com.piksel.sequoia.clientsdk.resource;

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