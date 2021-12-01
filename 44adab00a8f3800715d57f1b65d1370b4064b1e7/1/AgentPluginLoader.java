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
 *
 */

package org.apache.shardingsphere.agent.core.plugin;

import com.google.common.base.Strings;
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
import org.apache.shardingsphere.agent.core.common.AgentPathBuilder;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.utils.SingletonHolder;

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
    
    private final List<UberJar> jars = Lists.newArrayList();
    
    private final List<Service> services = Lists.newArrayList();
    
    private Map<String, PluginAdviceDefinition> pluginDefineMap;
    
    private AgentPluginLoader() {
        super(AgentPluginLoader.class.getClassLoader());
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        String path = classNameToPath(name);
        for (UberJar jar : jars) {
            ZipEntry entry = jar.jarFile.getEntry(path);
            if (Objects.nonNull(entry)) {
                try {
                    byte[] data = ByteStreams.toByteArray(jar.jarFile.getInputStream(entry));
                    return defineClass(name, data, 0, data.length);
                } catch (final IOException ex) {
                    log.error("Failed to load class {}.", name, ex);
                }
            }
        }
        throw new ClassNotFoundException("Class " + name + " not found.");
    }
    
    @Override
    protected Enumeration<URL> findResources(final String name) {
        List<URL> resources = Lists.newArrayList();
        for (UberJar jar : jars) {
            JarEntry entry = jar.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    resources.add(new URL("jar:file:" + jar.sourcePath.getAbsolutePath() + "!/" + name));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return Collections.enumeration(resources);
    }
    
    @Override
    protected URL findResource(final String name) {
        for (UberJar jar : jars) {
            JarEntry entry = jar.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    return new URL("jar:file:" + jar.sourcePath.getAbsolutePath() + "!/" + name);
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return null;
    }
    
    @Override
    public void close() {
        for (UberJar jar : jars) {
            try {
                jar.jarFile.close();
            } catch (final IOException ex) {
                log.error("close is ", ex);
            }
        }
    }
    
    /**
     * To get agent plugin loader instance.
     *
     * @return plugin loader
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
     * @throws IOException the IO exception
     */
    public void loadAllPlugins() throws IOException {
        File[] jarFiles = AgentPathBuilder.getPluginPath().listFiles(file -> file.getName().endsWith(".jar"));
        if (null == jarFiles) {
            return;
        }
        Map<String, PluginAdviceDefinition> pluginAdviceDefinitionMap = Maps.newHashMap();
        AgentConfiguration configuration = SingletonHolder.INSTANCE.get(AgentConfiguration.class);
        List<String> activatedLists = configuration.getActivatedPlugins();
        if (null == activatedLists) {
            activatedLists = Lists.newArrayList();
        }
        Set<String> activatedPlugins = Sets.newHashSet(activatedLists);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (File jarFile : jarFiles) {
            outputStream.reset();
            JarFile jar = new JarFile(jarFile, true);
            jars.add(new UberJar(jar, jarFile));
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
                pluginDefinition.build().forEach(plugin -> {
                    String target = plugin.getClassNameOfTarget();
                    if (pluginAdviceDefinitionMap.containsKey(target)) {
                        PluginAdviceDefinition definition = pluginAdviceDefinitionMap.get(target);
                        definition.getConstructorPoints().addAll(plugin.getConstructorPoints());
                        definition.getInstanceMethodPoints().addAll(plugin.getInstanceMethodPoints());
                        definition.getClassStaticMethodPoints().addAll(plugin.getClassStaticMethodPoints());
                    } else {
                        pluginAdviceDefinitionMap.put(target, plugin);
                    }
                });
                pluginDefinition.getAllServices().forEach(klass -> {
                    try {
                        services.add(klass.newInstance());
                        // CHECKSTYLE:OFF
                    } catch (final Throwable ex) {
                        // CHECKSTYLE:ON
                        log.error("Failed to create service instance, {}.", klass, ex);
                    }
                });
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to load plugin definition, {}.", entrypoint, ex);
            }
        }
        pluginDefineMap = ImmutableMap.<String, PluginAdviceDefinition>builder().putAll(pluginAdviceDefinitionMap).build();
    }
    
    private String classNameToPath(final String className) {
        return className.replace(".", "/") + ".class";
    }
    
    /**
     * To find all intercepting target classes then to build TypeMatcher.
     *
     * @return type matcher
     */
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return new ElementMatcher.Junction<TypeDescription>() {
            
            @Override
            public boolean matches(final TypeDescription target) {
                return pluginDefineMap.containsKey(target.getTypeName());
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
        return pluginDefineMap.containsKey(typeDescription.getTypeName());
    }
    
    /**
     * Load the definition configuration by TypeDescription.
     *
     * @param typeDescription TypeDescription
     * @return the plugin definition configurations
     */
    public PluginAdviceDefinition loadPluginAdviceDefine(final TypeDescription typeDescription) {
        return pluginDefineMap.getOrDefault(typeDescription.getTypeName(), PluginAdviceDefinition.createDefault());
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
    
    /**
     * Initial all services.
     */
    public void initialAllServices() {
        services.forEach(service -> {
            try {
                service.setup();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to initial service.", ex);
            }
        });
    }
    
    /**
     * Start all services.
     */
    public void startAllServices() {
        services.forEach(service -> {
            try {
                service.start();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to start service.", ex);
            }
        });
    }
    
    /**
     * Shutdown all services.
     */
    public void shutdownAllServices() {
        services.forEach(service -> {
            try {
                service.cleanup();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to shutdown service.", ex);
            }
        });
    }
    
    @RequiredArgsConstructor
    private static class UberJar {
        
        private final JarFile jarFile;
        
        private final File sourcePath;
    }
}
