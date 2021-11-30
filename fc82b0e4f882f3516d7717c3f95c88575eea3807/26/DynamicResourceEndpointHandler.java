package com.piksel.sequoia.clientsdk.resource;

import com.google.api.client.http.GenericUrl;
import com.google.gson.Gson;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.request.RequestClient;

@Internal
public class DynamicResourceEndpointHandler<T extends Resource> extends ResourceEndpointHandler<T> {

    public DynamicResourceEndpointHandler(RequestClient requestClient,
            String owner, String resourceKey, String endpointLocation,
            Class<T> resourceClass, Gson gson) {
        super(requestClient, resourceKey, resourceClass, gson);
        this.endpointUrl = new GenericUrl(endpointLocation);
        endpointUrl.set("owner", owner);
    }
}
