package com.piksel.sequoia.clientsdk.resource;

import static java.util.Objects.isNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.piksel.sequoia.clientsdk.resource.ResourceDeserializer.DeserializationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class LinkedIndirectRelationshipDeserializer<T extends Resource> extends LinkedDeserializer<T> {

    private LinkedIndirectRelationshipDeserializer(PageableResourceEndpoint<T> endpoint, Gson gson, JsonElement payload) {
        super(endpoint, gson, payload);
    }

    protected static <T extends Resource> LinkedIndirectRelationshipDeserializer<T> linkedIndirectDeserializer(PageableResourceEndpoint<T> endpoint,
            Gson gson, JsonElement payload) {
        return new LinkedIndirectRelationshipDeserializer<>(endpoint, gson, payload);
    }

    protected void includeLinkedFieldIndirectRelationship(T resource, Field field, JsonElement linkedResources) {
        if (isCollection(field) || isLazyLoading(field)) {
            populateCollectionFieldWithIndirectRefs(resource, field, linkedResources);
        } else {
            populateSingleFieldWithIndirectRef(resource, field, linkedResources);
        }
    }

    private void populateSingleFieldWithIndirectRef(T resource, Field field, JsonElement linkedResources) {
        try {
            Collection<T> indirectLinkedArray = getIndirectLinked(field, resource, linkedResources);
            @SuppressWarnings("unchecked")
            Collection<T> linkedResource = (Collection<T>) getRelatedResource(field, indirectLinkedArray);
            if (linkedResource != null && !linkedResource.isEmpty()) {
                setField(field, resource, Optional.of(linkedResource.iterator().next()));
            } else {
                setField(field, resource, Optional.empty());
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DeserializationException("Error deserializing " + resource, ex);
        }
    }

    private void populateCollectionFieldWithIndirectRefs(T resource, Field field, JsonElement linkedResources) {
        if (isCollectionOfOptionals(field)) {
            buildOptionalsIndirectRelationships(resource, field, linkedResources);
        } else if (isCollection(field)) {
            buildCollectionIndirectRelationships(resource, field, linkedResources);
        } else {
            buildLazyLoadingIndirectRelationships(resource, field);
        }
    }

    private void buildOptionalsIndirectRelationships(T resource, Field field, JsonElement linkedResources) {
        try {
            List<Optional<T>> linkedResourcesResult = new ArrayList<>();
            Collection<T> indirectLinkedArray = getIndirectLinked(field, resource, linkedResources);
            @SuppressWarnings("unchecked")
            Collection<T> linkedResourcesCollection = (Collection<T>) getRelatedResource(field, indirectLinkedArray);
            if (linkedResourcesCollection != null) {
                if (!linkedResourcesCollection.isEmpty()) {
                    linkedResourcesCollection.forEach((linkedResource) -> linkedResourcesResult.add(Optional.of(linkedResource)));
                }
            }
            setField(field, resource, linkedResourcesResult);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DeserializationException("Error deserializing " + resource, ex);
        }
    }

    private void buildCollectionIndirectRelationships(T resource, Field field, JsonElement linkedResources) {
        try {
            setField(field, resource, getLinkedResources(resource, field, linkedResources));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DeserializationException("Error deserializing " + resource, ex);
        }
    }

    @SuppressWarnings("unchecked")
    Collection<T> getLinkedResources(T resource, Field field, JsonElement linkedResources) {
        Collection<T> linkedResourceCollection = getIndirectLinked(field, resource, linkedResources);
        Object relatedResource = getRelatedResource(field, linkedResourceCollection);
        if (relatedResource instanceof Collection) {
            return (Collection<T>) relatedResource;
        }
        Collection<T> collectionWithSingle = Lists.newArrayList();
        collectionWithSingle.add((T) relatedResource);
        return collectionWithSingle;
    }

    private void buildLazyLoadingIndirectRelationships(T resource, Field field) {
        try {
            if (resource.getRef() != null) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Type type = pType.getActualTypeArguments()[0];
                @SuppressWarnings("unchecked")
                LazyLoadingLinkedResourceIterable<T> linkedResources = new LazyLoadingLinkedResourceIterable<>(payload, endpoint,
                        endpoint.getLinkedPages(getRelationShip(field), getRelationShip(field), (Class<T>) type), gson, field, resource);
                setField(field, resource, linkedResources);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DeserializationException("Error deserializing " + resource, ex);
        }
    }

    private Collection<T> getIndirectLinked(Field field, T resource, JsonElement linkedResources) {
        JsonElement jsonElement = linkedResources.getAsJsonObject().get(field.getAnnotation(IndirectRelationship.class).relationship());
        Collection<T> resourcesLinked = new ArrayList<>();
        if (Objects.nonNull(jsonElement)) {
            JsonArray arrayResources = jsonElement.getAsJsonArray();
            for (int i = 0; i < arrayResources.size(); i++) {
                addLinkedResources(field, resource, resourcesLinked, arrayResources.get(i),
                        arrayResources.get(i).getAsJsonObject().get(field.getAnnotation(IndirectRelationship.class).ref()));
            }
        }
        return resourcesLinked;
    }

    private void addLinkedResources(Field field, T resource, Collection<T> resourcesLinked, JsonElement jsonElement, JsonElement value) {
        if (isNull(value)) {
            log.warn(
                    "Unable to locate linking field [{}] in linked resource. Please check your model object and query to ensure the correct linked fields are selected",
                    field.getAnnotation(IndirectRelationship.class).ref());
            return;
        }
        if (value.isJsonArray()) {
            addLinkedResourceIfReferenceIsInArray(field, resource, resourcesLinked, jsonElement, value);
        } else {
            addLinkedResourceIfReferenceIsEqual(field, resource, resourcesLinked, jsonElement, value);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void addLinkedResourceIfReferenceIsEqual(Field field, T resource, Collection<T> resourcesLinked, JsonElement jsonElement,
            JsonElement value) {
        if (resource.getRef() != null && value.getAsString().equals(resource.getRef().toString())) {
            resourcesLinked.add((T) gson.fromJson(jsonElement, getType(field)));
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void addLinkedResourceIfReferenceIsInArray(Field field, T resource, Collection<T> resourcesLinked, JsonElement jsonElement,
            JsonElement value) {
        JsonArray array = value.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getAsString().equals(resource.getRef().toString())) {
                resourcesLinked.add((T) gson.fromJson(jsonElement, getType(field)));
            }
        }
    }

}
