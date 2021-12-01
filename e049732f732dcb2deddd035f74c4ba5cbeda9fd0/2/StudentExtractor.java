/*
 * Copyright 2012 Shared Learning Collaborative, LLC
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

/**
 * 
 */
package org.slc.sli.bulk.extract.lea;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slc.sli.bulk.extract.extractor.EntityExtractor;
import org.slc.sli.bulk.extract.util.LocalEdOrgExtractHelper;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.domain.Repository;

/**
 *
 *
 */
public class StudentExtractor implements EntityExtract {
    private LEAExtractFileMap map;
    private EntityExtractor extractor;
    private Repository<Entity> repo;
    private EntityToLeaCache studentCache;
    private EntityToLeaCache parentCache;
    private LocalEdOrgExtractHelper localEdOrgExtractHelper;

    private ExtractorHelper helper;

    private EntityToLeaCache diCache = new EntityToLeaCache();

    public StudentExtractor(EntityExtractor extractor, LEAExtractFileMap map, Repository<Entity> repo,
                            ExtractorHelper helper, EntityToLeaCache studentCache, EntityToLeaCache parentCache,
                            LocalEdOrgExtractHelper localEdOrgExtractHelper) {
        this.extractor = extractor;
        this.map = map;
        this.repo = repo;
        this.helper = helper;
        this.studentCache = studentCache;
        this.parentCache = parentCache;
        this.localEdOrgExtractHelper = localEdOrgExtractHelper;
    }

    /* (non-Javadoc)
     * @see org.slc.sli.bulk.extract.lea.EntityExtract#extractEntities(java.util.Map)
     */
    @Override
    public void extractEntities(EntityToLeaCache entityToEdorgCache) {
        localEdOrgExtractHelper.logSecurityEvent(map.getLeas(), EntityNames.STUDENT, this.getClass().getName());
        Iterator<Entity> cursor = repo.findEach("student", new NeutralQuery());
        while (cursor.hasNext()) {
            Entity e = cursor.next();
            Set<String> schools = helper.fetchCurrentSchoolsForStudent(e);
            Iterable<String> parents = helper.fetchCurrentParentsFromStudent(e);
            for (String lea : map.getLeas()) {
                if (schools.contains(lea)) {
                    // Write
                    extractor.extractEntity(e, map.getExtractFileForLea(lea), "student");
                    
                    // Update studentCache
                    studentCache.addEntry(e.getEntityId(), lea);

                    for (String parent : parents) {
                        parentCache.addEntry(parent, lea);
                    }
                }
            }

            List<Entity> sdias =  e.getEmbeddedData().get("studentDisciplineIncidentAssociation");

            if(sdias != null) {
                for(Entity sdia : sdias) {
                    String did = (String) sdia.getBody().get("disciplineIncidentId");
                    Set<String> leas = studentCache.getEntriesById(e.getEntityId());

                    if(leas != null) {
                        for(String lea : leas) {
                            diCache.addEntry(did, lea);
                        }
                    } else {
                        diCache.addEntry(did, "marker");    // adding a marker that this DI is referenced by a student
                    }

                }
            }
        }
        
    }
    
    public void setEntityCache(EntityToLeaCache cache) {
        this.studentCache = cache;
    }
    
    public EntityToLeaCache getEntityCache() {
        return studentCache;
    }
    
    public EntityToLeaCache getParentCache() {
        return parentCache;
    }

    public EntityToLeaCache getDiCache() {
        return diCache;
    }
}
