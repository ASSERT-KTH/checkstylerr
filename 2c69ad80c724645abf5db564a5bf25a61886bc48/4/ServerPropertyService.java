/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.service;


/**
 * Interface for service that provides access to server 
 * properties
 */
public interface ServerPropertyService extends Service {
    
    /**
     * Get a server property
     * @param property
     */
    public String getServerProperty(String property);
    
    /**
     * Set a server property
     * @param property
     * @param value
     */
    public void setServerProperty(String property, String value);
}
