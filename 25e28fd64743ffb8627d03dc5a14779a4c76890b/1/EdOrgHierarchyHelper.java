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
package org.slc.sli.domain.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.Repository;

/**
 * Common abstraction to retrieve the EducationOrganization's hierarchy
 * 
 * @author ycao
 * 
 */
public class EdOrgHierarchyHelper {
    private static final Logger LOG = LoggerFactory.getLogger(EdOrgHierarchyHelper.class);

    /*
     * This class is not spring managed bean because the repo required for
     * mongo queries are different depending on the situation, and I don't want to define
     * a lot of them in the xml configuration...
     * 
     * The repo must be passed in from the beans managed by spring. This is
     * a POJO
     */
   
    private Repository<Entity> repo;
    
    public EdOrgHierarchyHelper(Repository<Entity> repo) {
        this.repo = repo;
    }
    
    /**
     * Determine if this edorg is a SEA
     * 
     * @param entity
     * @return boolean
     */
    public boolean isSEA(Entity entity) {
        return isType("State Education Agency", entity);
    }
    
    /**
     * Determine if this edorg is a LEA
     * 
     * @param entity
     * @return boolean
     */
    public boolean isLEA(Entity entity) {
        return isType("Local Education Agency", entity);
    }
    
    /**
     * Determine if this edorg is a school
     * 
     * @param entity
     * @return boolean
     */
    public boolean isSchool(Entity entity) {
        return isType("School", entity);
    }
    
    @SuppressWarnings("unchecked")
    private boolean isType(String type, Entity entity) {
        if (entity == null) {
            return false;
        }
        
        List<String> category = (List<String>) entity.getBody().get("organizationCategories");
        
        if (category != null && category.contains(type)) {
            return true;
        }

        return false;
    }

    // TODO this logic will need to support multiple parentIds - see us5821
    private List<Entity> getParentEdOrg(Entity entity) {
        if (entity.getBody().containsKey("parentEducationAgencyReference")) {
//            String parentId;
            @SuppressWarnings("unchecked")
            List<String> parentIds = (List<String>) entity.getBody().get("parentEducationAgencyReference");
            // TODO add support of multiple parent references - see us5821
            List<Entity> parents = new ArrayList<Entity>();
            for(String parentId: parentIds) {
            	parents.add(repo.findById(EntityNames.EDUCATION_ORGANIZATION, parentId));
            }
            return parents;
        }
        return null;
    }
    
    /**
     * Given an school or LEA level entity, returns the top LEA it belongs to
     * 
     * if input is SEA, returns null
     * 
     * @param entity
     * @return top level LEA
     */
    public List<Entity> getTopLEAOfEdOrg(Entity entity) {
    	List<Entity> topLEAs = new ArrayList<Entity>();
        if (entity.getBody().containsKey("parentEducationAgencyReference")) {
            List<Entity> parentEdorgs = getParentEdOrg(entity);
            for(Entity parentEdorg: parentEdorgs) {
            	if (isLEA(parentEdorg)) {
            		topLEAs.addAll(getTopLEAOfEdOrg(parentEdorg));
            	}
            }
            if(topLEAs.size()>0) {
            	return topLEAs;
            }
        }
        
        if (isLEA(entity)) {
        	topLEAs.add(entity);
        	return topLEAs;
        }
        
        return null;
    }
    
    /**
     * Given an edorg entity, returns the SEA it belongs to
     * 
     * @param entity
     * @return SEA
     */
    public String getSEAOfEdOrg(Entity entity) {
        if (isSEA(entity)) {
            return entity.getEntityId();
        } else {
            Entity parentEdorg = getParentEdOrg(entity).get(0);
            if (parentEdorg != null) {
                return getSEAOfEdOrg(parentEdorg);
            } else {
                LOG.warn("EdOrg {} is missing parent SEA", entity.getEntityId());
                return null;
            }
        }
    }
}
