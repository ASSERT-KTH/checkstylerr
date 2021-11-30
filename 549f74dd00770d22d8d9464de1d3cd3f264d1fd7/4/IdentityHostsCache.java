package com.piksel.sequoia.clientsdk.token.cache;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 - 2019 Piksel
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

import com.google.common.cache.CacheLoader;
import com.piksel.sequoia.clientsdk.configuration.HostConfiguration;
import com.piksel.sequoia.clientsdk.registry.RegisteredService;
import com.piksel.sequoia.clientsdk.registry.RegistryClient;
import com.piksel.sequoia.clientsdk.resource.ResourceIterable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class IdentityHostsCache extends CacheLoader<String, HostConfiguration> {

    private RegistryClient registryClient;
    private final HostConfiguration defaultIdentityHost;

    @Override
    public HostConfiguration load(String key) throws Exception {
        return getIdentityHostFromRegistry(key);
    }

    private HostConfiguration getIdentityHostFromRegistry(String owner) {
        ResourceIterable<RegisteredService> serviceResourceIterable =registryClient.getServiceRegistryListForOwner(owner);
        while(serviceResourceIterable.hasNext()) {
            RegisteredService registeredService = serviceResourceIterable.next();
            if(registeredService.getName().equals("identity")) {
                return new HostConfiguration(registeredService.getLocation());
            }
        }
        return defaultIdentityHost;
    }
}
