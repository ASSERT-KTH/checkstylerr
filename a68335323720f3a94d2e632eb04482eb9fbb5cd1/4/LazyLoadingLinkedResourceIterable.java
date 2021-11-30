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
public final class LazyLoadingLinkedResourceIterable<T extends Resource> extends AbstractLazyLoadingIterable<T> implements LinkedResourceIterable<T> {

    private final Field field;
    private final T resource;
    private final ResourceDeserializer<T> linkedDeserializer;
    private int numItemsPage = 0;

    public LazyLoadingLinkedResourceIterable(JsonElement payload, PageableResourceEndpoint<T> endpoint, PageableResourceEndpoint<T> linkedEndpoint,
            Gson gson, Field field, T resource) {
        super(payload, endpoint, gson);
        this.field = field;
        this.resource = resource;
        this.linkedDeserializer = new ResourceDeserializer<>(linkedEndpoint, gson);
        this.pageIndex = addLinkedPage(payload);
    }

    @Override
    protected boolean theCurrentPageHasContents() {
        return true;
    }

    @Override
    protected void loadNextAndUpdateIndexes() {
        Optional<JsonElement> payload = endpoint.getPagedLinkedResource(currentPage().getMeta().getContinuesPage());
        deserializer = linkedDeserializer;
        Optional<JsonElement> filteredPayload = deserializer.includeJustLinkedItems(payload.orElseThrow(noSuchElementException()),
                field.getAnnotation(IndirectRelationship.class).ref(), resource.getRef().toString());
        pageIndex = addPage(filteredPayload.orElseThrow(noSuchElementException()));
        resourceIndex = 0;
        numItemsPage = deserializer.numLinkedItemsInPayload(payload.orElseThrow(noSuchElementException()),
                field.getAnnotation(IndirectRelationship.class).ref(), resource.getRef().toString());
    }

    @Override
    protected boolean nextPageContainsResources() {
        if ((noItemsInPage() || lastPageItem()) && metaHasContinue()) {
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
    
    private int addLinkedPage(JsonElement payload) {
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

    private boolean lastPageItem() {
        return numItemsPage > 0 && resourceIndex == numItemsPage;
    }

    private boolean noItemsInPage() {
        return numItemsPage == 0;
    }

    private JsonElement getLinked(JsonElement payload) {
        return payload.getAsJsonObject().get(LINKED);
    }

}
