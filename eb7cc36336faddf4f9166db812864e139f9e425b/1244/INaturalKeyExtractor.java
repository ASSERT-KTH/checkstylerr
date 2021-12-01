/*
 * Copyright 2012-2013 inBloom, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slc.sli.validation.schema;

import java.util.Map;

import org.slc.sli.common.domain.NaturalKeyDescriptor;
import org.slc.sli.domain.Entity;
import org.slc.sli.validation.NoNaturalKeysDefinedException;

/**
 * @author sashton
 */
public interface INaturalKeyExtractor {
    
    /**
     * Returns a map of natural key field -> value for the given entity
     * 
     * @param entity
     * @return
     */
    public abstract Map<String, String> getNaturalKeys(Entity entity) throws NoNaturalKeysDefinedException;
    
    /**
     * Returns a map of natural keys from the schema for the given entity. The key is the field name
     * and the value is a Boolean representing whether the field is optional.
     * 
     * @param entity
     *            Entity to inspect
     * @return
     */
    public abstract Map<String, Boolean> getNaturalKeyFields(Entity entity) throws NoNaturalKeysDefinedException;
    
    /**
     * Returns a natural key descriptor for the given entity
     * 
     * @param entity
     * @return
     */
    public abstract NaturalKeyDescriptor getNaturalKeyDescriptor(Entity entity) throws NoNaturalKeysDefinedException;
    
}
