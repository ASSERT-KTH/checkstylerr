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

package org.slc.sli.api.security.context.validator;

import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Decides if given user has access to given staffEdorgAssoc
 * 
 * @author dkornishev
 *
 */
@Component
public class StaffToEducationOrganizationAssociationValidator extends AbstractContextValidator {

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return EntityNames.STAFF_ED_ORG_ASSOCIATION.equals(entityType) && isStaff();
    }
    
    @Override
    public boolean validate(String entityType, Set<String> ids) {
        if (!areParametersValid(EntityNames.STAFF_ED_ORG_ASSOCIATION, entityType, ids)) {
            return false;
        }
        
        info("Validating {}'s access to staffEducationOrganizationAssoc: [{}]", SecurityUtil.getSLIPrincipal().getName(), ids);
        
        Set<String> lineage = this.getStaffEdOrgLineage();
        
        NeutralQuery nq = new NeutralQuery(new NeutralCriteria("_id", "in", ids, false));
        nq.addCriteria(new NeutralCriteria("body.educationOrganizationReference", "in", lineage, false));
        
        List<Entity> found = (List<Entity>) getRepo().findAll(EntityNames.STAFF_ED_ORG_ASSOCIATION, nq);
        
        return ids.size() == found.size();
    }
}
