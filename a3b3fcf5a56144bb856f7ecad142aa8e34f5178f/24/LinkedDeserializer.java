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

import static com.piksel.sequoia.clientsdk.resource.LinkedDirectRelationshipDeserializer.linkedDirectDeserializer;
import static com.piksel.sequoia.clientsdk.resource.LinkedIndirectRelationshipDeserializer.linkedIndirectDeserializer;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.LINKED;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withName;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.piksel.sequoia.clientsdk.resource.ResourceDeserializer.DeserializationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class LinkedDeserializer<T extends Resource> {

    private static final String MESSAGE = "message";
    final PageableResourceEndpoint<T> endpoint;
    final Gson gson;
    final JsonElement payload;
    private final static String STATUS_CODE = "statusCode";

    protected LinkedDeserializer(PageableResourceEndpoint<T> endpoint, Gson gson, JsonElement payload) {
        this.endpoint = endpoint;
        this.gson = gson;
        this.payload = payload;
    }

    protected static <T extends Resource> LinkedDeserializer<T> createLinkedDeserializer(PageableResourceEndpoint<T> endpoint, Gson gson,
            JsonElement payload) {
        return new LinkedDeserializer<>(endpoint, gson, payload);
    }

    protected List<T> addLinkedToResources(ResourcesWithLinkedResources<T> resourcesWithLinkedResources) {
        checkErrorsInMeta(resourcesWithLinkedResources.getMeta());
        Set<Field> linkedFields = extractRelationshipFields();
        return includeLinkedResourcesIntoResources(resourcesWithLinkedResources, linkedFields);
    }

    protected static String getRelationShip(Field field) {
        if (field.getAnnotation(DirectRelationship.class) != null) {
            return field.getAnnotation(DirectRelationship.class).relationship();
        }

        if (field.getAnnotation(IndirectRelationship.class) != null) {
            return field.getAnnotation(IndirectRelationship.class).relationship();
        }

        return null;
    }

    protected String getRef(T resource, Field field) {
        try {
            Optional<Field> refField = getRefField(field, resource);
            if (refField.isPresent()) {
                return (String) getField(refField.get(), resource);
            } else {
                return null;
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DeserializationException("Error deserializing " + resource, ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object getField(Field refField, T resource) throws IllegalAccessException, IllegalArgumentException {
        AccessController.doPrivileged((PrivilegedAction) () -> {
            refField.setAccessible(true);
            return null;
        });
        return refField.get(resource);
    }

    @SuppressWarnings("unchecked")
    protected Optional<Field> getRefField(Field field, T resource) {
        try {
            String refFieldName = field.getAnnotation(DirectRelationship.class).ref();
            Field refField = getAllFields(resource.getClass(), withName(refFieldName)).stream().findFirst().get();
            return Optional.of(refField);
        } catch (java.util.NoSuchElementException exception) {
            log.warn("Field declared in ref in field '" + field.getName() + "' does not exist");
            return Optional.empty();
        }
    }

    protected boolean isOptional(Field field) {
        return field.getType().equals(Optional.class);
    }

    protected boolean isCollectionOfOptionals(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            if (pType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) pType.getActualTypeArguments()[0];
                return type.getRawType().equals(Optional.class);
            }
            return false;
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setField(Field field, T resource, Object linkedResources) throws IllegalArgumentException, IllegalAccessException {
        AccessController.doPrivileged((PrivilegedAction) () -> {
            field.setAccessible(true);
            return null;
        });

        field.set(resource, linkedResources);

    }

    protected Object getRelatedResource(Field field, Collection<T> linkedResources) {
        try {
            return getRelatedResourceForCollectionOrSingle(field, linkedResources);
        } catch (RuntimeException ex) {
            log.info(String.format("Can not populate field %s with payload %s", field.getName(), payload), ex);
            return null;
        }
    }

    protected boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    protected boolean isLazyLoading(Field field) {
        return LinkedResourceIterable.class.isAssignableFrom(field.getType());
    }

    protected Class<?> getType(Field field) {
        if (nonNull(field.getGenericType())) {
            if (field.getGenericType() instanceof Class) {
                return (Class<?>) field.getGenericType();
            }
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            if (genericType.getActualTypeArguments()[0] instanceof Class) {
                return (Class<?>) genericType.getActualTypeArguments()[0];
            }
            return (Class<?>) ((ParameterizedType) genericType.getActualTypeArguments()[0]).getActualTypeArguments()[0];
        }
        return field.getType();
    }

    private Object getRelatedResourceForCollectionOrSingle(Field field, Collection<T> linkedResources) {
        if (linkedResources != null) {
            if (isCollection(field) || isLazyLoading(field)) {
                return linkedResources;
            } else {
                if (linkedResources.isEmpty()) {
                    return null;
                }
                return linkedResources.iterator().next();
            }
        } else {
            return null;
        }
    }

    private void checkError(Field field, JsonElement meta) {
        checkErrorStr(field, meta);
    }

    private void checkErrorStr(Field field, JsonElement meta) {
        if (metaHasNotInfo(field, meta)) {
            return;
        }

        JsonArray linkedResourceInfo = getLinkedRequestInfo(field, meta);
        if (containsStatusCode(linkedResourceInfo)) {
            return;
        }

        checkLinkedRequestInfo(linkedResourceInfo);
    }

    private void checkLinkedRequestInfo(JsonArray linkedResourceInfo) {
        for (int i = 0; i < linkedResourceInfo.size(); i++) {
            Integer statusCode = linkedResourceInfo.get(i).getAsJsonObject().get(STATUS_CODE).getAsInt();
            if (statusCode >= 400 && statusCode != 404) {
                throw new IncludeResourceException(Collections.singletonList(linkedResourceInfo.get(i).getAsJsonObject().get(MESSAGE).getAsString()));
            }
        }
    }

    private boolean isDirectRelationship(Field field) {
        return field.getAnnotation(DirectRelationship.class) != null;
    }

    private boolean containsStatusCode(JsonArray linkedResourceInfo) {
        return linkedResourceInfo.size() <= 0 || !linkedResourceInfo.toString().contains(STATUS_CODE);
    }

    private JsonArray getLinkedRequestInfo(Field field, JsonElement meta) {
        return meta.getAsJsonObject().get(LINKED).getAsJsonObject().get(getRelationShip(field)).getAsJsonArray();
    }

    private boolean metaHasNotInfo(Field field, JsonElement meta) {
        return meta.isJsonNull() || !linkedExists(meta) || fieldDoesNotExist(field, meta);
    }

    private boolean linkedExists(JsonElement meta) {
        return nonNull(meta.getAsJsonObject().get(LINKED));
    }

    private boolean fieldDoesNotExist(Field field, JsonElement meta) {
        return isNull(meta.getAsJsonObject().get(LINKED).getAsJsonObject().get(getRelationShip(field)));
    }

    private void checkErrorsInMeta(JsonElement meta) {
        Set<Field> relationshipFields = extractRelationshipFields();
        relationshipFields.forEach((field) -> checkError(field, meta));
    }

    @SuppressWarnings("unchecked")
    private Set<Field> extractRelationshipFields() {
        Set<Field> fields = getAllFields(endpoint.getEndpointType(), withAnnotation(DirectRelationship.class));
        fields.addAll(getAllFields(endpoint.getEndpointType(), withAnnotation(IndirectRelationship.class)));
        return fields;
    }

    private List<T> includeLinkedResourcesIntoResources(ResourcesWithLinkedResources<T> resourcesWithLinkedResources, Set<Field> linkedFields) {
        resourcesWithLinkedResources.getResources()
                .forEach((resource) -> includeLinkedResourcesIntoResource(resource, linkedFields, resourcesWithLinkedResources.getLinkedResources()));
        return resourcesWithLinkedResources.getResources();
    }

    private void includeLinkedResourcesIntoResource(T resource, Set<Field> linkedFields, JsonElement linkedResources) {
        linkedFields.forEach((field) -> includeLinkedField(resource, field, linkedResources));
    }

    private void includeLinkedField(T resource, Field field, JsonElement linkedResources) {
        if (isDirectRelationship(field)) {
            linkedDirectDeserializer(endpoint, gson, payload).includeLinkedFieldDirectRelationship(resource, field, linkedResources);
        } else {
            linkedIndirectDeserializer(endpoint, gson, payload).includeLinkedFieldIndirectRelationship(resource, field, linkedResources);
        }
    }

}
