package com.piksel.sequoia.clientsdk.resource;

import static com.piksel.sequoia.clientsdk.resource.UrlQueryStringParser.urlParser;

import java.util.Collection;
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
        validate(resource, 0);
        return toResourceResponse(requestClient.executePostRequest(endpointUrl,
                resourceKey, resource));
    }

    @Override
    public ResourceResponse<T> store(Collection<T> resources) {
        T[] resourcesArray = collectionToArray(resources);
        validate(resourcesArray);
        return toResourceResponse(requestClient.executePostRequest(endpointUrl,
                resourceKey, resourcesArray));
    }

    @Override
    public ResourceResponse<T> delete(Reference reference) {
        validate(reference);
        GenericUrl urlToDelete = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeDeleteRequest(urlToDelete, reference));
    }

    @Override
    public ResourceResponse<T> delete(Collection<Reference> references) {
        Reference[] referencesArray = references
                .toArray((Reference[]) java.lang.reflect.Array
                        .newInstance(Reference.class, references.size()));
        validate(referencesArray);
        GenericUrl urlToDelete = endpointUrl.clone();
        return toResourceResponse(requestClient
                .executeDeleteRequest(urlToDelete, referencesArray));
    }

    @Override
    public ResourceResponse<T> update(T resource, Reference reference) {
        validate(reference);
        validate(resource, 0);
        validate(resource, PutValidation.class);
        validateReferenceToUpdateWithResourceReference(resource, reference);
        GenericUrl urlToUpdate = endpointUrl.clone();
        return toResourceResponse(requestClient.executePutRequest(urlToUpdate,
                resourceKey, resource, reference));
    }

    @Override
    public ResourceResponse<T> read(Reference reference) {
        validate(reference);
        GenericUrl urlToRead = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(urlToRead, reference));
    }

    @Override
    public ResourceResponse<T> read(Collection<Reference> references) {
        Reference[] referencesArray = references
                .toArray((Reference[]) java.lang.reflect.Array
                        .newInstance(Reference.class, references.size()));
        validate(referencesArray);
        GenericUrl urlToRead = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(urlToRead, referencesArray));
    }

    @Override
    public ResourceResponse<T> browse(ResourceCriteria criteria) {
        GenericUrl urlToApplyCriteria = endpointUrl.clone();
        return toResourceResponse(
                requestClient.executeGetRequest(new CriteriaUrlApplier()
                        .applyCriteria(urlToApplyCriteria, criteria)));
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
        GenericUrl urlToCall = endpointUrl.clone();
        urlToCall.putAll(urlParser(url).queryString());
        return requestClient.executeGetRequest(urlToCall).getPayload();
    }
    
    @Override
    public Optional<JsonElement> getPagedLinkedResource(String url) {
        GenericUrl urlToCall = endpointUrl.clone();
        UrlQueryStringParser urlParser = urlParser(url);
        urlToCall.setPathParts(urlParser.getPathParts());
        urlToCall.putAll(urlParser.queryString());
        return requestClient.executeGetRequest(urlToCall).getPayload();
    }

    @Override
    public ResourceEndpointHandler<T> getLinkedPages(String resourceKey, String endpointLocation, Class<T> resourceClass) {
        GenericUrl urlToCall = endpointUrl.clone();
        ResourceEndpointHandler<T> newRes = new ResourceEndpointHandler<>(requestClient, resourceKey, resourceClass, gson);
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
                throw ReferencesMismatchException.thrown(Objects.toString(reference), Objects.toString(resourceReference));
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
    
    private ResourceResponse<T> toResourceResponse(
            Response<JsonElement> jsonResponse) {
        DefaultResourceResponseBuilder<T> builder = DefaultResourceResponse
                .<T>builder().statusCode(jsonResponse.getStatusCode())
                .successStatusCode(jsonResponse.isSuccessStatusCode());
        if (jsonResponse.getPayload().isPresent()
                && !jsonResponse.getPayload().get().isJsonNull()) {
            builder.payload(
                    Optional.of(new LazyLoadingResourceIterable<>(
                            jsonResponse.getPayload().get(), this, gson)));
        } else {
            builder.payload(Optional.empty());
        }
        return builder.build();
    }

    private T[] collectionToArray(Collection<T> resources) {
        @SuppressWarnings("unchecked")
        T[] resourcesArray = resources.toArray((T[]) java.lang.reflect.Array
                .newInstance(payloadClass, resources.size()));
        return resourcesArray;
    }
    
    private String applyCamelCaseConvention(String resourceKey) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL,resourceKey);
    }

}
