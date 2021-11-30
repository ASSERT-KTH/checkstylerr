package com.piksel.sequoia.clientsdk.configuration;

import com.piksel.sequoia.annotations.PublicEvolving;

/**
 * Allows the user-agent string used in HTTP requests to 
 * be overriden or decorated. Note that this configurer 
 * will be called during configuration, and not for each 
 * request.
 */
@PublicEvolving
public interface UserAgentConfigurer {

    /**
     * Provide the user-agent string to use in all HTTP requests, 
     * taking the default string which is in the format:
     * <pre>
     * sequoia-java-client-sdk/VERSION
     * </pre>
     */
    String configureUserAgentString(String defaultUserAgentString);
    
}