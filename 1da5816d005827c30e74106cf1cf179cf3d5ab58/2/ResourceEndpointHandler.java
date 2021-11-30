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

import static com.piksel.sequoia.clientsdk.resource.UrlQueryStringParser.urlParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.api.client.http.GenericUrl;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.DefaultResourceResponse;
import com.piksel.sequoia.clientsdk.DefaultResourceResponse.DefaultResourceResponseBuilder;
import com.piksel.sequoia.clientsdk.ResourceResponse;
import com.piksel.sequoia.clientsdk.Response;
import com.piksel.sequoia.clientsdk.criteria.CriteriaUrlApplier;
import com.piksel.sequoia.clientsdk.request.RequestClient;
import com.piksel.sequoia.clientsdk.validation.PutValidation;

@Internal
public class ResourceEndpointHandler<T extends Resource> implements PageableResourceEndpoint<T> {

    protected GenericUrl endpointUrl;
    private final Class<T> payloadClass;
    private final RequestClient requestClient;
    private final String resourceKey;
    private final Gson gson;

    public ResourceEndpointHandler(RequestClient requestClient,
            String resourceKey, String endpointLocation, Class<T> resourceClass,
            Gson gson) {
        this.requestClient = requestClient;
        this.endpointUrl = new GenericUrl(endpointLocation);
        this.payloadClass = resourceClass;
        this.resourceKey = applyCamelCaseConvention(resourceKey);
        this.gson = gson;
    }

    protected ResourceEndpointHandler(RequestClient requestClient,
            String resourceKey, Class<T> resourceClass, Gson gson) {
        this.requestClient = requestClient;
        this.payloadClass = resourceClass;
        this.resourceKey = applyCamelCaseConvention(resourceKey);
        this.gson = gson;
    }

    @Override
    public ResourceResponse<T> store(T resource) {
        return store(resource, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> store(T resource, Map<? extends String, ?> headers) {
        validate(resource, 0);
        return toResourceResponse(requestClient.executePostRequest(endpointUrl, headers, resourceKey, resource),
                headers);
    }

    @Override
    public ResourceResponse<T> store(Collection<T> resources) {
        return store(resources, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> store(Collection<T> resources, Map<? extends String, ?> headers) {
        T[] resourcesArray = collectionToArray(resources);
        validate(resourcesArray);
        return toResourceResponse(requestClient.executePostRequest(endpointUrl, headers, resourceKey, resourcesArray),
                headers);
    }

    @Override
    public ResourceResponse<T> delete(Reference reference) {
        return delete(reference, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> delete(Reference reference, Map<? extends String, ?> headers) {
        validate(reference);
        GenericUrl urlToDelete = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeDeleteRequest(urlToDelete, headers, reference), headers);
    }

    @Override
    public ResourceResponse<T> delete(Collection<Reference> references) {
        return delete(references, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> delete(Collection<Reference> references, Map<? extends String, ?> headers) {
        Reference[] referencesArray = references
                .toArray((Reference[]) java.lang.reflect.Array
                        .newInstance(Reference.class, references.size()));
        validate(referencesArray);
        GenericUrl urlToDelete = endpointUrl.clone();
        return toResourceResponse(requestClient
                .executeDeleteRequest(urlToDelete, headers, referencesArray), headers);
    }

    @Override
    public ResourceResponse<T> update(T resource, Reference reference) {
        return update(resource, reference, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> update(T resource, Reference reference, Map<? extends String, ?> headers) {
        validate(reference);
        validate(resource, 0);
        validate(resource, PutValidation.class);
        validateReferenceToUpdateWithResourceReference(resource, reference);
        GenericUrl urlToUpdate = endpointUrl.clone();
        return toResourceResponse(requestClient.executePutRequest(urlToUpdate, headers, resourceKey, resource,
                reference), headers);
    }

    @Override
    public ResourceResponse<T> read(Reference reference) {
        return read(reference, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> read(Reference reference, Map<? extends String, ?> headers) {
        validate(reference);
        GenericUrl urlToRead = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(urlToRead, headers, reference), headers);
    }

    @Override
    public ResourceResponse<T> read(Collection<Reference> references) {
        return read(references, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> read(Collection<Reference> references, Map<? extends String, ?> headers) {
        Reference[] referencesArray = references
                .toArray((Reference[]) java.lang.reflect.Array
                        .newInstance(Reference.class, references.size()));
        validate(referencesArray);
        GenericUrl urlToRead = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(urlToRead, headers, referencesArray), headers);
    }

    @Override
    public ResourceResponse<T> browse(ResourceCriteria criteria) {
        return browse(criteria, new HashMap<>());
    }

    @Override
    public ResourceResponse<T> browse(ResourceCriteria criteria, Map<? extends String, ?> headers) {
        GenericUrl urlToApplyCriteria = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(new CriteriaUrlApplier()
                        .applyCriteria(urlToApplyCriteria, criteria), headers), headers);
    }

    @Override
    public String getResourceKey() {
        return resourceKey;
    }

    @Override
    public Class<T> getEndpointType() {
        return payloadClass;
    }

    @Override
    public Optional<JsonElement> getPagedResource(String url) {
        return getPagedResource(url, new HashMap<>());
    }

    @Override
    public Optional<JsonElement> getPagedResource(String url, Map<? extends String, ?> headers) {
        GenericUrl urlToCall = endpointUrl.clone();
        urlToCall.putAll(urlParser(url).queryString());
        return requestClient.executeGetRequest(urlToCall, headers).getPayload();
    }

    @Override
    public Optional<JsonElement> getPagedLinkedResource(String url) {
        return getPagedLinkedResource(url, new HashMap<>());
    }

    @Override
    public Optional<JsonElement> getPagedLinkedResource(String url, Map<? extends String, ?> headers) {
        GenericUrl urlToCall = endpointUrl.clone();
        UrlQueryStringParser urlParser = urlParser(url);
        urlToCall.setPathParts(urlParser.getPathParts());
        urlToCall.putAll(urlParser.queryString());
        return requestClient.executeGetRequest(urlToCall, headers).getPayload();
    }

    @Override
    public ResourceEndpointHandler<T> getLinkedPages(String resourceKey, String endpointLocation,
            Class<T> resourceClass) {
        GenericUrl urlToCall = endpointUrl.clone();
        ResourceEndpointHandler<T> newRes = new ResourceEndpointHandler<>(requestClient, resourceKey, resourceClass,
                gson);
        newRes.endpointUrl = new GenericUrl(urlParser(urlToCall.toString()).replaceFinalPath(resourceKey));
        return newRes;
    }

    private void validateReferenceToUpdateWithResourceReference(T resource,
            Reference reference) {
        if (Objects.nonNull(resource.getRef())
                && !reference.equals(resource.getRef())) {
            throw ReferencesMismatchException.thrown(reference.toString(),
                    resource.getRef().toString());
        }
        if (resource.getOwner() != null && resource.getName() != null) {
            Reference resourceReference = Reference.fromOwnerAndName(resource.getOwner(), resource.getName());
            if (!reference.equals(resourceReference)) {
                throw ReferencesMismatchException.thrown(Objects.toString(reference), Objects.toString(
                        resourceReference));
            }
        }

    }

    private void validate(T resource, Class<?> clazz) {
        if (!resource.isValid(clazz).isEmpty()) {
            throw ResourceValidationException.thrown(resource.isValid(clazz));
        }
    }

    @SafeVarargs
    private final void validate(T... resource) {
        int resourceIndex = 0;
        for (T r : resource) {
            validate(r, resourceIndex);
            resourceIndex++;
        }
    }

    @SafeVarargs
    private final void validate(Reference... reference) {
        for (Reference r : reference) {
            if (!r.isValid().isEmpty()) {
                throw ReferenceValidationException.thrown(r.isValid());
            }
        }
    }

    private void validate(T resource, int index) {
        if (!resource.isValid().isEmpty()) {
            throw ResourceValidationException.thrown(resource.isValid(), index);
        }
    }

  

    private ResourceResponse<T> toResourceResponse(Response<JsonElement> jsonResponse,
            Map<? extends String,?> headers) {

        DefaultResourceResponseBuilder<T> builder = DefaultResourceResponse.<T>builder()
                .statusCode(jsonResponse.getStatusCode())
                .successStatusCode(jsonResponse.isSuccessStatusCode());
        builder.payload(getLoadingResourceIterable(jsonResponse, headers));
        return builder.build();
    }
    
    private Optional<ResourceIterable<T>> getLoadingResourceIterable(
            Response<JsonElement> jsonResponse, Map<? extends String,?> headers) {
        if (jsonResponse.getPayload().isPresent()
                && !jsonResponse.getPayload().get().isJsonNull()) {
            return Optional.of(new LazyLoadingResourceIterable<>(jsonResponse.getPayload().get(), this, gson, headers));
        } else {
            return Optional.empty();
        }
    }

    private T[] collectionToArray(Collection<T> resources) {
        @SuppressWarnings("unchecked")
        T[] resourcesArray = resources.toArray((T[]) java.lang.reflect.Array
                .newInstance(payloadClass, resources.size()));
        return resourcesArray;
    }

    private String applyCamelCaseConvention(String resourceKey) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, resourceKey);
    }

}
