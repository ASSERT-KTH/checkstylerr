package com.piksel.sequoia.clientsdk.resource;

import static com.piksel.sequoia.clientsdk.resource.LinkedDeserializer.createLinkedDeserializer;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.AND_ENCODED;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.COLON;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.ENCODED_COLON;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.EQUAL;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.LINKED;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.META;
import static com.piksel.sequoia.clientsdk.resource.ResourceDeserializerConstants.OR_ENCODED;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;

/**
 * Deserializes a Payload. Resolving meta, resources, and linked resources.
 */
@Slf4j
class ResourceDeserializer<T extends Resource> {

    private static final String REQUEST = "request";
    private final PageableResourceEndpoint<T> endpoint;
    private final Gson gson;

    public ResourceDeserializer(PageableResourceEndpoint<T> endpoint, Gson gson) {
        this.endpoint = endpoint;
        this.gson = gson;
    }

    List<T> contentsFrom(JsonElement payload) {
        JsonElement resourceList = payload.getAsJsonObject().get(endpoint.getResourceKey());
        ResourcesWithLinkedResources<T> resourcesWithLinkedResources = ResourcesWithLinkedResources.<T> builder()
                .resources(deserialisedResources(resourceList.getAsJsonArray())).linkedResources(getLinkedFromPayload(payload))
                .meta(getMetaFromPayload(payload)).build();
        if (resourcesWithLinkedResources.getLinkedResources() != null) {
            return createLinkedDeserializer(endpoint, gson, payload).addLinkedToResources(resourcesWithLinkedResources);
        } else {
            return new ArrayList<>(resourcesWithLinkedResources.getResources());
        }
    }

    List<T> linkedResourcesFrom(Resource resource, JsonElement payload) {
        JsonElement resourceList = payload.getAsJsonObject().get(endpoint.getResourceKey());
        final List<? extends T> deserialisedResourcesWithoutLinked = deserialisedResources(resourceList.getAsJsonArray());
        return new ArrayList<>(deserialisedResourcesWithoutLinked);
    }

    Optional<LinkedMeta> linkedMetaFrom(JsonElement jsonElement, String relationship, Reference ref) {
        if (validate(jsonElement, relationship, ref)) {
            return Optional.empty();
        }
        return getMeta(getRelationshipFromMetaAsArray(jsonElement, relationship), relationship, encodeRef(ref));
    }

    Optional<Meta> metaFrom(JsonElement payload) {
        return ofNullable(gson.fromJson(getMetaFromPayload(payload), Meta.class));
    }

    Meta emptyMeta() {
        return new Meta();
    }

    LinkedMeta emptyLinkedMeta() {
        return new LinkedMeta();
    }

    Optional<JsonElement> includeJustLinkedItems(JsonElement payload, String fieldRefName, String refValue) {
        final List<? extends T> justRelatedItems = getLinkedItemsByFieldValue(payload, fieldRefName, refValue);
        payload.getAsJsonObject().add(endpoint.getResourceKey(), gson.toJsonTree(justRelatedItems));
        return Optional.of(payload);
    }

    int numLinkedItemsInPayload(JsonElement payload, String fieldRefName, String value) {
        return getLinkedItemsByFieldValue(payload, fieldRefName, value).size();
    }

    JsonElement getMetaFromPayload(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(META);
    }

    private JsonElement getLinkedFromPayload(JsonElement payload) {
        return payload.getAsJsonObject().get(LINKED);
    }

    private Optional<LinkedMeta> getMeta(JsonArray array, String relationship, String encodedRef) {
        LinkedMeta linkedMeta;
        JsonArray filteredArray = filter(array, encodedRef);
        linkedMeta = getMeta(filteredArray);
        linkedMeta.setPerPage(getPerPages(filteredArray));
        
        return Optional.of(linkedMeta);
    }

    private JsonArray filter(JsonArray array, String encodedRef) {
        JsonArray filteredArray = new JsonArray();
        for (int i = 0; i < array.size(); i++) {
            if (containsEncodeRef(encodedRef, array.get(i).getAsJsonObject().get(REQUEST).getAsString())) {
                filteredArray.add(array.get(i));
            }
        }
        return filteredArray;
    }

    private int getPerPages(JsonArray metaArray) {
        int perPages = 0;
        for (int i = 0; i < metaArray.size(); i++) {
            perPages += gson.fromJson(metaArray.get(i), LinkedMeta.class).getPerPage();
        }
        return perPages;
    }

    private LinkedMeta getMeta(JsonArray metaArray) {

        LinkedMeta linkedMeta = null;
        for (int i = 0; i < metaArray.size(); i++) {
            LinkedMeta linkedMetaCandidate = gson.fromJson(metaArray.get(i), LinkedMeta.class);
            if (Objects.isNull(linkedMeta) || linkedMetaCandidate.getPage() > linkedMeta.getPage()) {
                linkedMeta = linkedMetaCandidate;
            }
        }
        return linkedMeta;
    }

    private boolean containsEncodeRef(String encodedRef, String request) {
        return request.endsWith(EQUAL + encodedRef)
                || request.endsWith(OR_ENCODED + encodedRef)
                || request.contains(OR_ENCODED + encodedRef + AND_ENCODED)
                || request.contains(OR_ENCODED + encodedRef + OR_ENCODED)
                || request.contains(OR_ENCODED + encodedRef + OR_ENCODED)
                || request.contains(EQUAL + encodedRef + OR_ENCODED);
    }

    private List<? extends T> getLinkedItemsByFieldValue(JsonElement payload, String fieldRefName, String refValue) {
        JsonElement resourceList = payload.getAsJsonObject().get(endpoint.getResourceKey());
        final List<? extends T> deserialisedResources = deserialisedResources(resourceList.getAsJsonArray());
        final List<? extends T> justRelatedItems = new ArrayList<>(deserialisedResources);
        for (T resource : deserialisedResources) {
            removeNotLinkedItems(fieldRefName, refValue, justRelatedItems, resource);
        }
        return justRelatedItems;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void removeNotLinkedItems(String fieldName, String value, final List<? extends T> justRelatedItems, T resource) {
        try {
            Field field = endpoint.getEndpointType().getDeclaredField(fieldName);
            AccessController.doPrivileged((PrivilegedAction) () -> {
                field.setAccessible(true);
                return null;
            });
            evaluateFieldToRemoveValue(value, justRelatedItems, resource, field, field.get(resource));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new ErrorGettingRelatedItemsException(e);
        }
    }

    private void evaluateFieldToRemoveValue(String value, final List<? extends T> justRelatedItems, T resource, Field field, Object fieldValue) {
        if (isNull(fieldValue)) {
            justRelatedItems.clear();
        } else {
            removeValue(value, justRelatedItems, resource, field, fieldValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeValue(String value, final List<? extends T> justRelatedItems, T resource, Field field, Object fieldValue) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            removeItems((x, y) -> ((Collection<String>) x).contains(y), fieldValue, value, resource, justRelatedItems);
        } else {
            removeItems((x, y) -> x.equals(y), fieldValue, value, resource, justRelatedItems);
        }
    }

    private void removeItems(BiPredicate<Object, String> p, Object fieldValue, String value, T resource, final List<? extends T> justRelatedItems) {
        if (p.negate().test(fieldValue, value)) {
            justRelatedItems.remove(resource);
        }
    }

    private boolean validate(JsonElement jsonElement, String relationship, Reference ref) {
        return isNull(ref) || isNull(getMetaFromPayload(jsonElement)) || isNull(getLinked(jsonElement))
                || isNull(getRelationshipElementFromMeta(jsonElement, relationship))
                || isNull(getRelationshipFromMetaAsArray(jsonElement, relationship));
    }

    private JsonElement getRelationshipElementFromMeta(JsonElement jsonElement, String relationship) {
        return getLinked(jsonElement).getAsJsonObject().get(relationship);
    }

    private JsonArray getRelationshipFromMetaAsArray(JsonElement jsonElement, String relationship) {
        return getRelationshipElementFromMeta(jsonElement, relationship).getAsJsonArray();
    }

    private JsonElement getLinked(JsonElement jsonElement) {
        return getMetaFromPayload(jsonElement).getAsJsonObject().get(LINKED);
    }

    private List<T> deserialisedResources(JsonArray jsonArray) {
        List<T> instancesWithoutLinked = gson.fromJson(jsonArray, theResponseType(endpoint));
        log.debug("Deserialised collection [{}]", instancesWithoutLinked);
        return instancesWithoutLinked;
    }

    private Type theResponseType(ResourcefulEndpoint<T> endpoint) {
        return resourceTypeToken(endpoint).getType();
    }

    @SuppressWarnings("serial")
    private TypeToken<List<T>> resourceTypeToken(ResourcefulEndpoint<T> endpoint) {
        return new TypeToken<List<T>>() {
        }.where(new TypeParameter<T>() {
        }, endpoint.getEndpointType());
    }

    private String encodeRef(Reference ref) {
        return ref.toString().replace(COLON, ENCODED_COLON);
    }

    @SuppressWarnings("serial")
    static class DeserializationException extends RuntimeException {

        public DeserializationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    @SuppressWarnings("serial")
    static class ErrorGettingRelatedItemsException extends RuntimeException {

        public ErrorGettingRelatedItemsException(Exception e) {
            super(e);
        }

    }

    static class ErrorGettingLinkedFromCacheException extends RuntimeException {

        private static final long serialVersionUID = -4120949797236942838L;

        public ErrorGettingLinkedFromCacheException(String message) {
            super(message);
        }

    }

}
