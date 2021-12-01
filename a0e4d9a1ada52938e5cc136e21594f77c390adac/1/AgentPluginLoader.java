/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.core.plugin.loader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.shardingsphere.agent.core.path.AgentPathBuilder;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.plugin.definition.PluginDefinition;
import org.apache.shardingsphere.agent.core.plugin.service.BootService;
import org.apache.shardingsphere.agent.core.plugin.service.ServiceSupervisor;
import org.apache.shardingsphere.agent.core.cache.AgentObjectPool;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Agent plugin loader.
 */
@Slf4j
public final class AgentPluginLoader extends ClassLoader implements Closeable {
    
    static {
        registerAsParallelCapable();
    }
    
    private static volatile AgentPluginLoader agentPluginLoader;
    
    private final ConcurrentHashMap<String, Object> objectPool = new ConcurrentHashMap<>();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private final List<PluginJar> jars = Lists.newArrayList();
    
    private final List<BootService> bootServices = Lists.newArrayList();
    
    private Map<String, PluginInterceptorPoint> interceptorPointMap;
    
    private AgentPluginLoader() {
        super(AgentPluginLoader.class.getClassLoader());
    }
    
    /**
     * Get agent plugin loader instance.
     *
     * @return agent plugin loader instance
     */
    public static AgentPluginLoader getInstance() {
        if (null == agentPluginLoader) {
            synchronized (AgentPluginLoader.class) {
                if (null == agentPluginLoader) {
                    agentPluginLoader = new AgentPluginLoader();
                }
            }
        }
        return agentPluginLoader;
    }
    
    /**
     * Load all plugins.
     *
     * @throws IOException IO exception
     */
    public void loadAllPlugins() throws IOException {
        File[] jarFiles = AgentPathBuilder.getPluginPath().listFiles(file -> file.getName().endsWith(".jar"));
        if (null == jarFiles) {
            return;
        }
        Map<String, PluginInterceptorPoint> pointMap = Maps.newHashMap();
        List<String> activatedLists = AgentObjectPool.INSTANCE.get(AgentConfiguration.class).getActivatedPlugins();
        Set<String> activatedPlugins = Sets.newHashSet(activatedLists);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (File jarFile : jarFiles) {
            outputStream.reset();
            JarFile jar = new JarFile(jarFile, true);
            jars.add(new PluginJar(jar, jarFile));
            log.info("Loaded jar {}.", jarFile.getName());
            Attributes attributes = jar.getManifest().getMainAttributes();
            String entrypoint = attributes.getValue("Entrypoint");
            if (Strings.isNullOrEmpty(entrypoint)) {
                log.warn("Entrypoint is not setting in {}.", jarFile.getName());
                continue;
            }
            ByteStreams.copy(jar.getInputStream(jar.getEntry(classNameToPath(entrypoint))), outputStream);
            try {
                PluginDefinition pluginDefinition = (PluginDefinition) defineClass(entrypoint, outputStream.toByteArray(), 0, outputStream.size()).newInstance();
                if (!activatedPlugins.isEmpty() && !activatedPlugins.contains(pluginDefinition.getPluginName())) {
                    continue;
                }
                buildPluginInterceptorPointMap(pluginDefinition, pointMap);
                buildBootServices(pluginDefinition);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to load plugin definition, {}.", entrypoint, ex);
            }
        }
        interceptorPointMap = ImmutableMap.<String, PluginInterceptorPoint>builder().putAll(pointMap).build();
    }
    
    /**
     * To find all intercepting target classes then to build TypeMatcher.
     *
     * @return type matcher
     */
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return new Junction<TypeDescription>() {
            
            @Override
            public boolean matches(final TypeDescription target) {
                return interceptorPointMap.containsKey(target.getTypeName());
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> and(final ElementMatcher<? super U> other) {
                return null;
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> or(final ElementMatcher<? super U> other) {
                return null;
            }
        };
    }
    
    /**
     * To detect the type whether or not exists.
     *
     * @param typeDescription TypeDescription
     * @return contains when it is true
     */
    public boolean containsType(final TypeDescription typeDescription) {
        return interceptorPointMap.containsKey(typeDescription.getTypeName());
    }
    
    /**
     * Load plugin interceptor point by TypeDescription.
     *
     * @param typeDescription TypeDescription
     * @return plugin interceptor point
     */
    public PluginInterceptorPoint loadPluginInterceptorPoint(final TypeDescription typeDescription) {
        return interceptorPointMap.getOrDefault(typeDescription.getTypeName(), PluginInterceptorPoint.createDefault());
    }
    
    /**
     * Get the supervisor of boot services.
     *
     * @return service supervisor
     */
    public ServiceSupervisor getBootServices() {
        return new ServiceSupervisor(ImmutableList.<BootService>builder().addAll(bootServices).build());
    }
    
    /**
     * To get or create instance of the advice class. Create new one and caching when it is not exist.
     *
     * @param classNameOfAdvice class name of advice
     * @param <T> advice type
     * @return instance of advice
     */
    @SneakyThrows({ClassNotFoundException.class, IllegalAccessException.class, InstantiationException.class})
    @SuppressWarnings("unchecked")
    public <T> T getOrCreateInstance(final String classNameOfAdvice) {
        if (objectPool.containsKey(classNameOfAdvice)) {
            return (T) objectPool.get(classNameOfAdvice);
        }
        lock.lock();
        try {
            Object inst = objectPool.get(classNameOfAdvice);
            if (Objects.isNull(inst)) {
                inst = Class.forName(classNameOfAdvice, true, this).newInstance();
                objectPool.put(classNameOfAdvice, inst);
            }
            return (T) inst;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        String path = classNameToPath(name);
        for (PluginJar each : jars) {
            ZipEntry entry = each.jarFile.getEntry(path);
            if (Objects.nonNull(entry)) {
                try {
                    byte[] data = ByteStreams.toByteArray(each.jarFile.getInputStream(entry));
                    return defineClass(name, data, 0, data.length);
                } catch (final IOException ex) {
                    log.error("Failed to load class {}.", name, ex);
                }
            }
        }
        throw new ClassNotFoundException("Class " + name + " not found.");
    }
    
    private String classNameToPath(final String className) {
        return className.replace(".", "/") + ".class";
    }
    
    @Override
    protected Enumeration<URL> findResources(final String name) {
        List<URL> resources = Lists.newArrayList();
        for (PluginJar each : jars) {
            JarEntry entry = each.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    resources.add(new URL("jar:file:" + each.sourcePath.getAbsolutePath() + "!/" + name));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return Collections.enumeration(resources);
    }
    
    @Override
    protected URL findResource(final String name) {
        for (PluginJar each : jars) {
            JarEntry entry = each.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    return new URL("jar:file:" + each.sourcePath.getAbsolutePath() + "!/" + name);
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return null;
    }
    
    @Override
    public void close() {
        for (PluginJar each : jars) {
            try {
                each.jarFile.close();
            } catch (final IOException ex) {
                log.error("close is ", ex);
            }
        }
    }
    
    private void buildPluginInterceptorPointMap(final PluginDefinition pluginDefinition, final Map<String, PluginInterceptorPoint> pointMap) {
        pluginDefinition.build().forEach(each -> {
            String target = each.getClassNameOfTarget();
            if (pointMap.containsKey(target)) {
                PluginInterceptorPoint definition = pointMap.get(target);
                definition.getConstructorPoints().addAll(each.getConstructorPoints());
                definition.getInstanceMethodPoints().addAll(each.getInstanceMethodPoints());
                definition.getClassStaticMethodPoints().addAll(each.getClassStaticMethodPoints());
            } else {
                pointMap.put(target, each);
            }
        });
    }
    
    private void buildBootServices(final PluginDefinition pluginDefinition) {
        pluginDefinition.getAllServices().forEach(each -> {
            try {
                bootServices.add(each.newInstance());
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to create service instance, {}.", each, ex);
            }
        });
    }
    
    @RequiredArgsConstructor
    private static class PluginJar {
        
        private final JarFile jarFile;
        
        private final File sourcePath;
    }
}
