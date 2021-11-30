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
