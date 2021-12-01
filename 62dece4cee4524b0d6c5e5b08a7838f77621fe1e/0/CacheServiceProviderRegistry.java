/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.clustering.server.provider;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.context.Flag;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.jboss.msc.service.ServiceName;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.ee.Batch;
import org.wildfly.clustering.ee.Batcher;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;
import org.wildfly.clustering.provider.ServiceProviderRegistration;
import org.wildfly.clustering.provider.ServiceProviderRegistration.Listener;
import org.wildfly.clustering.provider.ServiceProviderRegistry;

/**
 * Infinispan {@link Cache} based {@link ServiceProviderRegistrationFactory}.
 * This factory can create multiple {@link ServiceProviderRegistration} instance,
 * all of which share the same {@link Cache} instance.
 * @author Paul Ferraro
 */
@org.infinispan.notifications.Listener
public class CacheServiceProviderRegistry<T> implements ServiceProviderRegistry<T>, Group.Listener, AutoCloseable {

    final ConcurrentMap<T, Listener> listeners = new ConcurrentHashMap<>();
    final Batcher<? extends Batch> batcher;
    final Cache<T, Set<Node>> cache;

    private final Group group;
    private final CommandDispatcher<Set<T>> dispatcher;

    public CacheServiceProviderRegistry(CacheServiceProviderRegistryConfiguration<T> config) {
        this.group = config.getGroup();
        this.cache = config.getCache();
        this.batcher = config.getBatcher();
        this.dispatcher = config.getCommandDispatcherFactory().createCommandDispatcher(config.getId(), this.listeners.keySet());
        this.cache.addListener(this);
        this.group.addListener(this);
    }

    @Override
    public void close() {
        this.group.removeListener(this);
        this.cache.removeListener(this);
        this.dispatcher.close();
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public ServiceProviderRegistration<T> register(T service) {
        return this.register(service, null);
    }

    @Override
    public ServiceProviderRegistration<T> register(final T service, Listener listener) {
        if (this.listeners.putIfAbsent(service, listener) != null) {
            throw new IllegalArgumentException(service.toString());
        }
        final Node node = this.group.getLocalNode();
        Set<Node> nodes = new HashSet<>(Collections.singleton(node));
        try (Batch batch = this.batcher.createBatch()) {
            Set<Node> existing = this.cache.getAdvancedCache().withFlags(Flag.FORCE_SYNCHRONOUS).putIfAbsent(service, nodes);
            if (existing != null) {
                if (existing.add(node)) {
                    this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).replace(service, existing);
                }
            }
        }
        return new AbstractServiceProviderRegistration<T>(service, this) {
            @Override
            public void close() {
                Node node = CacheServiceProviderRegistry.this.getGroup().getLocalNode();
                try (Batch batch = CacheServiceProviderRegistry.this.batcher.createBatch()) {
                    Set<Node> nodes = CacheServiceProviderRegistry.this.cache.get(service);
                    if ((nodes != null) && nodes.remove(node)) {
                        Cache<T, Set<Node>> cache = CacheServiceProviderRegistry.this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
                        if (nodes.isEmpty()) {
                            cache.remove(service);
                        } else {
                            cache.replace(service, nodes);
                        }
                    }
                }
                CacheServiceProviderRegistry.this.listeners.remove(service);
            }
        };
    }

    @Override
    public Set<Node> getProviders(final T service) {
        Set<Node> nodes = this.cache.get(service);
        return (nodes != null) ? Collections.unmodifiableSet(nodes) : Collections.<Node>emptySet();
    }

    @Override
    public Set<T> getServices() {
        return this.cache.keySet();
    }

    @Override
    public void membershipChanged(List<Node> previousMembers, List<Node> members, final boolean merged) {
        if (this.getGroup().isCoordinator()) {
            final Set<Node> deadNodes = new HashSet<>(previousMembers);
            deadNodes.removeAll(members);
            final Set<Node> newNodes = new HashSet<>(members);
            newNodes.removeAll(previousMembers);
            try (Batch batch = this.batcher.createBatch()) {
                if (!deadNodes.isEmpty()) {
                    try (CloseableIterator<Map.Entry<T, Set<Node>>> entries = this.cache.entrySet().iterator()) {
                        while (entries.hasNext()) {
                            Map.Entry<T, Set<Node>> entry = entries.next();
                            Set<Node> nodes = entry.getValue();
                            if (nodes.removeAll(deadNodes)) {
                                entry.setValue(nodes);
                            }
                        }
                    }
                }
                if (merged) {
                    for (Node node: newNodes) {
                        // Re-assert services for new members following merge since these may have been lost following split
                        List<T> services = CacheServiceProviderRegistry.this.getServices(node);
                        for (T service: services) {
                            Set<Node> nodes = new HashSet<>(Collections.singleton(node));
                            Set<Node> existing = this.cache.getAdvancedCache().withFlags(Flag.FORCE_SYNCHRONOUS).putIfAbsent(service, nodes);
                            if (existing != null) {
                                if (existing.add(node)) {
                                    this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).replace(service, existing);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @CacheEntryCreated
    @CacheEntryModified
    public void modified(CacheEntryEvent<ServiceName, Set<Node>> event) {
        if (event.isPre()) return;
        Listener listener = this.listeners.get(event.getKey());
        if (listener != null) {
            listener.providersChanged(event.getValue());
        }
    }

    List<T> getServices(Node node) {
        try {
            return this.dispatcher.executeOnNode(new GetLocalServicesCommand<>(), node).get();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
