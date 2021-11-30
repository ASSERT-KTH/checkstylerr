package com.piksel.sequoia.clientsdk.resource;

import static com.piksel.sequoia.clientsdk.resource.LinkedDeserializer.getRelationShip;
import static com.piksel.sequoia.clientsdk.resource.LinkedIndirectRelationshipDeserializer.linkedIndirectDeserializer;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.LINKED;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@PublicEvolving
public final class LazyLoadingLinkedResourceIterable<T extends Resource> extends LazyLoading<T> implements LinkedResourceIterable<T> {

    private final Field field;
    private final T resource;
    private final Gson gson;
    private final ResourceDeserializer<T> linkedDeserializer;
    private int numItemsPage = 0;

    public LazyLoadingLinkedResourceIterable(JsonElement payload, PageableResourceEndpoint<T> endpoint, PageableResourceEndpoint<T> linkedEndpoint,
            Gson gson, Field field, T resource) {
        log.debug("Creating lazy loading list iterable for [{}]", endpoint.getEndpointType());
        this.endpoint = endpoint;
        this.field = field;
        this.gson = gson;
        this.resource = resource;
        this.deserializer = new ResourceDeserializer<>(endpoint, gson);
        this.linkedDeserializer = new ResourceDeserializer<>(linkedEndpoint, gson);
        this.pageIndex = addLinkedPage(payload);
        this.totalCount = getTotalCount(payload);
    }

    @Override
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    boolean theCurrentPageHasContents() {
        return true;
    }

    @Override
    public T next() {
        return super.next();
    }

    @Override
    public Optional<Integer> totalCount() {
        return super.totalCount();
    }

    int addLinkedPage(JsonElement payload) {
        long startTime = System.nanoTime();
        LinkedMeta meta = deserializer.linkedMetaFrom(payload, getRelationShip(field), resource.getRef()).orElse(deserializer.emptyLinkedMeta());
        ArrayList<T> linkedResources = new ArrayList<>(
                linkedIndirectDeserializer(endpoint, gson, payload).getLinkedResources(resource, field, getLinked(payload)));
        pages.put(meta.getPage(), Page.from(meta, linkedResources));
        numItemsPage = linkedResources.size();
        long endTime = System.nanoTime();
        log.debug("time to add linked page - {} seconds", (double) (endTime - startTime) / 1000000000.0);
        return meta.getPage();
    }

    void loadNextAndUpdateIndexes() {
        Optional<JsonElement> payload = endpoint.getPagedLinkedResource(currentPage().getMeta().getNext());
        deserializer = linkedDeserializer;
        Optional<JsonElement> filteredPayload = deserializer.includeJustLinkedItems(payload.orElseThrow(noSuchElementException()),
                field.getAnnotation(IndirectRelationship.class).ref(), resource.getRef().toString());
        pageIndex = addPage(filteredPayload.orElseThrow(noSuchElementException()));
        resourceIndex = 0;
        numItemsPage = deserializer.numLinkedItemsInPayload(payload.orElseThrow(noSuchElementException()),
                field.getAnnotation(IndirectRelationship.class).ref(), resource.getRef().toString());
    }

    @Override
    boolean nextPageContainsResources() {
        if ((noItemsInPage() || lastPageItem()) && metaHasNext()) {
            while (currentPage().isNotLast()) {
                loadNextAndUpdateIndexes();
                if (currentPage().items() > 0) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    boolean lastPageItem() {
        return numItemsPage > 0 && resourceIndex == numItemsPage;
    }

    private boolean noItemsInPage() {
        return numItemsPage == 0;
    }

    private JsonElement getLinked(JsonElement payload) {
        return payload.getAsJsonObject().get(LINKED);
    }

}