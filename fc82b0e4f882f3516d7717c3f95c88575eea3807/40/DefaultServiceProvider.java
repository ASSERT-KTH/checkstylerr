package com.piksel.sequoia.clientsdk.registry.service;

import com.google.api.client.http.GenericUrl;
import com.google.gson.Gson;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.endpoint.Endpoint;
import com.piksel.sequoia.clientsdk.endpoint.EndpointHandler;
import com.piksel.sequoia.clientsdk.registry.RegisteredService;
import com.piksel.sequoia.clientsdk.request.RequestClient;
import com.piksel.sequoia.clientsdk.resource.DynamicResourceEndpointHandler;
import com.piksel.sequoia.clientsdk.resource.Resource;
import com.piksel.sequoia.clientsdk.resource.ResourceEndpointHandler;
import com.piksel.sequoia.clientsdk.resource.ResourcefulEndpoint;
import com.piksel.sequoia.clientsdk.validation.Validatable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Internal
public class DefaultServiceProvider implements ServiceProvider {

    protected static final String DATA = "data";

    protected final RegisteredService service;
    protected final RequestClient requestClient;
    protected final Gson gson;

    public DefaultServiceProvider(RequestClient requestClient, RegisteredService service, Gson gson) {
        this.service = service;
        this.requestClient = requestClient;
        this.gson = gson;
    }

    @Override
    public <T extends Resource> ResourcefulEndpoint<T> resourcefulEndpoint(
            String endpoint, Class<T> resourceClass) {
        String endpointComposed = String.join("/", service.getLocation(), DATA,
                endpoint);
        log.debug("Endpoint composed [{}] ", endpointComposed);
        return new ResourceEndpointHandler<>(requestClient, endpoint,
                endpointComposed, resourceClass, gson);
    }

    @Override
    public <T extends Resource> ResourcefulEndpoint<T> resourcefulEndpoint(
            String owner, String endpoint, Class<T> resourceClass) {
        String endpointComposed = String.join("/", service.getLocation(), DATA,
                endpoint);
        log.debug("Endpoint composed [{}] ", endpointComposed);
        return new DynamicResourceEndpointHandler<>(requestClient, owner,
                endpoint, endpointComposed, resourceClass, gson);
    }

    @Override
    public <T, K extends Validatable> Endpoint<T, K> endpoint(String endpoint,
            Class<T> responseClass) {
        GenericUrl endpointComposed = new GenericUrl(
                String.join("/", service.getLocation(), endpoint));
        log.debug("Endpoint composed [{}] ", endpointComposed);
        return new EndpointHandler<>(requestClient, endpointComposed,
                responseClass, gson);
    }
}
