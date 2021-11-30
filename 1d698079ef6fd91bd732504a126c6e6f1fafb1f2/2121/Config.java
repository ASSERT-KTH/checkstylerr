/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.Properties;

/**
 * Responsible for holding the Cache configuration properties. If the default
 * constructor is used, this class will load the properties from the
 * <code>cache.configuration</code>.
 *
 * @author   <a href="mailto:fabian.crabus@gurulogic.de">Fabian Crabus</a>
 * @version  $Revision: 412 $
 */
public class Config implements java.io.Serializable {
    
    private static final transient Log log = LogFactory.getLog(Config.class);

    /**
     * Name of the properties file.
     */
    private final static String PROPERTIES_FILENAME = "/oscache.properties";

    /**
     * Properties map to hold the cache configuration.
     */
    private Properties properties = null;

    /**
     * Create an OSCache Config that loads properties from oscache.properties.
     * The file must be present in the root of OSCache's classpath. If the file
     * cannot be loaded, an error will be logged and the configuration will
     * remain empty.
     */
    public Config() {
        this(null);
    }

    /**
     * Create an OSCache configuration with the specified properties.
     * Note that it is the responsibility of the caller to provide valid
     * properties as no error checking is done to ensure that required
     * keys are present. If you're unsure of what keys should be present,
     * have a look at a sample oscache.properties file.
     *
     * @param p The properties to use for this configuration. If null,
     * then the default properties are loaded from the <code>oscache.properties</code>
     * file.
     */
    public Config(Properties p) {
        if (log.isDebugEnabled()) {
            log.debug("OSCache: Config called");
        }

        if (p == null) {
            this.properties = loadProperties(PROPERTIES_FILENAME, "the default configuration");
        } else {
            this.properties = p;
        }
    }

    /**
     * Retrieve the value of the named configuration property. If the property
     * cannot be found this method will return <code>null</code>.
     *
     * @param key The name of the property.
     * @return The property value, or <code>null</code> if the value could
     * not be found.
     *
     * @throws IllegalArgumentException if the supplied key is null.
     */
    public String getProperty(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return null;
        }

        return properties.getProperty(key);
    }

    /**
     * Retrieves all of the configuration properties. This property set
     * should be treated as immutable.
     *
     * @return The configuration properties.
     */
    public Properties getProperties() {
        return properties;
    }

    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * Sets a configuration property.
     *
     * @param key   The unique name for this property.
     * @param value The value assigned to this property.
     *
     * @throws IllegalArgumentException if the supplied key is null.
     */
    public void set(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (value == null) {
            return;
        }

        if (properties == null) {
            properties = new Properties();
        }

        properties.put(key, value);
    }
    
    /**
     * Load the properties from the specified URL.
     * @param url a non null value of the URL to the properties
     * @param info additional logger information if the properties can't be read
     * @return the loaded properties specified by the URL
     * @since 2.4
     */
    public static Properties loadProperties(URL url, String info) {
        log.info("OSCache: Getting properties from URL " + url + " for " + info);

        Properties properties = new Properties();
        InputStream in = null;

        try {
            in = url.openStream();
            properties.load(in);
            log.info("OSCache: Properties read " + properties);
        } catch (Exception e) {
            log.error("OSCache: Error reading from " + url, e);
            log.error("OSCache: Ensure the properties information in " + url+ " is readable and in your classpath.");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.warn("OSCache: IOException while closing InputStream: " + e.getMessage());
            }
        }
        
        return properties;
    }

    /**
     * Load the specified properties file from the classpath. If the file
     * cannot be found or loaded, an error will be logged and no
     * properties will be set.
     * @param filename the properties file with path
     * @param info additional logger information if file can't be read
     * @return the loaded properties specified by the filename
     * @since 2.4
     */
    public static Properties loadProperties(String filename, String info) {
        URL url = null;
        
        ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        if (threadContextClassLoader != null) {
            url = threadContextClassLoader.getResource(filename);
        }
        if (url == null) {
            url = Config.class.getResource(filename);
            if (url == null) {
                log.warn("OSCache: No properties file found in the classpath by filename " + filename);
                return new Properties();
            }
        }
        
        return loadProperties(url, info);
    }

}
