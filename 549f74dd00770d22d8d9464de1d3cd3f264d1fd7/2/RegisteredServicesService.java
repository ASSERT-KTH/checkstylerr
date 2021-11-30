package com.piksel.sequoia.clientsdk.registry.service;

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

import static com.google.common.util.concurrent.AbstractScheduledService.Scheduler.newFixedDelaySchedule;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.RequestExecutionException;
import com.piksel.sequoia.clientsdk.registry.HostRegistry;
import com.piksel.sequoia.clientsdk.registry.RegisteredService;
import com.piksel.sequoia.clientsdk.registry.RegistryClient;
import com.piksel.sequoia.clientsdk.resource.ResourceIterable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains the set of registered services available to the given credentials.
 */
@Slf4j
@Internal
public class RegisteredServicesService extends AbstractScheduledService implements HostRegistry {

    private final int refreshIntervalSeconds;
    private final RegistryClient registryClient;

    private boolean isPopulated = false;

    private CompletableFuture<Void> registryPopulated = new CompletableFuture<>();

    private Map<String, MappedRegisteredServices> serviceMap = Maps.newConcurrentMap();

    private String registryOwner;

    public RegisteredServicesService(
            RegisteredServicesServiceConfiguration configuration,
            RegistryClient registryClient) {
        this.registryOwner = configuration.getRegistryServiceOwner();
        this.refreshIntervalSeconds = configuration.getRefreshIntervalSeconds();
        this.registryClient = registryClient;
    }

    @Override
    protected void startUp() throws Exception {
        runOneIteration();
    }

    @Override
    protected void runOneIteration() {
        log.debug("Getting list of registered services");
        ResourceIterable<RegisteredService> serviceRegistryList;
        try {
            serviceRegistryList = registryClient.getServiceRegistryList();
        } catch (RequestExecutionException executionException) {
            failRegistryPopulatedFuture(executionException);
            log.warn("Unable to get service registry list");
            if (!isPopulated) {
                throw executionException;
            }
            return;
        } catch (RuntimeException e) {
            log.warn("Propagating runtime exception", e);
            failRegistryPopulatedFuture(e);
            throw e;
        }
        mapServices(serviceRegistryList);
    }

    void failRegistryPopulatedFuture(Exception executionException) {
        if (!registryPopulated.isDone()) {
            registryPopulated.completeExceptionally(executionException);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return newFixedDelaySchedule(0, refreshIntervalSeconds, SECONDS);
    }

    @Override
    public boolean isPopulated() {
        return isPopulated;
    }

    @Override
    public Optional<RegisteredService> getRegisteredService(String serviceName) {
        return getRegisteredService(serviceName, registryOwner);
    }

    @Override
    public Optional<RegisteredService> getRegisteredService(String serviceName, String owner) {
        Optional<MappedRegisteredServices> registeredServices = getRegisteredServicesForOwner(owner);
        if(!registeredServices.isPresent()) {
            registeredServices = lookForRegisteredServicesForOwner(owner);
        }
        return registeredServices.flatMap(mappedService -> Optional.ofNullable(mappedService.get(serviceName)));
    }

    public Future<Void> awaitPopulation() {
        return registryPopulated;
    }

    private void mapServices(
            ResourceIterable<RegisteredService> serviceRegistryIterator) {
        serviceMap.clear();
        serviceRegistryIterator.forEachRemaining(registerServiceConsumer());
        isPopulated = true;
        registryPopulated.complete(null);
        log.debug("Got list of registered services: [{}]", serviceMap);
    }

    private Consumer<RegisteredService> registerServiceConsumer() {
        return registeredService -> registerService(registeredService, registryOwner);
    }

    private Consumer<RegisteredService> registerServiceConsumer(String owner) {
        return registeredService -> registerService(registeredService, owner);
    }

    private void registerService(RegisteredService registeredService, String owner) {
        MappedRegisteredServices registeredServices = getRegisteredServicesForOwner(owner)
            .orElse(new MappedRegisteredServices());
        registeredServices.put(registeredService.getName(), registeredService);
        serviceMap.put(owner, registeredServices);
    }

    private Optional<MappedRegisteredServices> lookForRegisteredServicesForOwner(String owner) {
        ResourceIterable<RegisteredService> serviceResourceIterable = registryClient
            .getServiceRegistryListForOwner(owner);
        serviceResourceIterable.forEachRemaining(registerServiceConsumer(owner));
        return getRegisteredServicesForOwner(owner);
    }

    private Optional<MappedRegisteredServices> getRegisteredServicesForOwner(String owner) {
        return Optional.ofNullable(serviceMap.get(owner));
    }

    static final class MappedRegisteredServices extends HashMap<String, RegisteredService> {

        private static final long serialVersionUID = 1696904752418203137L;

        public MappedRegisteredServices() {
        }
    }
}
