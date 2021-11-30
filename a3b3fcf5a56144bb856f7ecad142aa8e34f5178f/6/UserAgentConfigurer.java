package com.piksel.sequoia.clientsdk.configuration;

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
