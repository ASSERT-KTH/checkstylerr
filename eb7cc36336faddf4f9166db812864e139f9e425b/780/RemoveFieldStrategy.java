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

package org.slc.sli.dal.migration.strategy.impl;

import java.util.Map;

import org.slc.sli.dal.migration.strategy.MigrationException;
import org.slc.sli.dal.migration.strategy.MigrationStrategy;
import org.slc.sli.domain.Entity;

/**
 * Supports the migration of entities by removing a top level data field.
 * Will not work with nested fields.
 * 
 * @author kmyers
 */

public class RemoveFieldStrategy implements MigrationStrategy {

    public static final String FIELD_NAME = "fieldName";
    
    private String fieldName;
    
    @Override
    public Entity migrate(Entity entity) throws MigrationException {
        
        entity.getBody().remove(fieldName);

        return entity;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) throws MigrationException {

        if (parameters == null) {
            throw new MigrationException(new IllegalArgumentException("Remove strategy missing required arguments "));
        }

        if (!parameters.containsKey(FIELD_NAME)) {
            throw new MigrationException(new IllegalArgumentException("Remove strategy missing required argument: " + FIELD_NAME));
        }

        this.fieldName = parameters.get(FIELD_NAME).toString();
    }
    
}
