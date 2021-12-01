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

package org.slc.sli.bulk.extract.lea;

import java.util.Iterator;
import java.util.Set;

import org.slc.sli.bulk.extract.extractor.EntityExtractor;
import org.slc.sli.bulk.extract.util.EdOrgExtractHelper;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.domain.Repository;

public class StaffProgramAssociationExtractor implements EntityExtract {
    private EntityExtractor extractor;
    private ExtractFileMap map;
    private Repository<Entity> repo;
    private EdOrgExtractHelper edOrgExtractHelper;
    
    public StaffProgramAssociationExtractor(EntityExtractor extractor, ExtractFileMap map, Repository<Entity> repo, EdOrgExtractHelper edOrgExtractHelper) {
        this.extractor = extractor;
        this.map = map;
        this.repo = repo;
        this.edOrgExtractHelper = edOrgExtractHelper;
    }

    @Override
    public void extractEntities(EntityToEdOrgCache staffToEdorgCache) {
        edOrgExtractHelper.logSecurityEvent(map.getEdOrgs(), EntityNames.STAFF_PROGRAM_ASSOCIATION, this.getClass().getName());
        Iterator<Entity> spas = repo.findEach(EntityNames.STAFF_PROGRAM_ASSOCIATION, new NeutralQuery());
        while (spas.hasNext()) {
            Entity spa = spas.next();
            Set<String> edOrgs = staffToEdorgCache.getEntriesById(spa.getBody().get(ParameterConstants.STAFF_ID).toString());
            if (edOrgs == null || edOrgs.size() == 0) {
                continue;
            }
            for (String edOrg : edOrgs) {
                extractor.extractEntity(spa, map.getExtractFileForEdOrg(edOrg), EntityNames.STAFF_PROGRAM_ASSOCIATION);
            }
            
        }
    }
    
}
