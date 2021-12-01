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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;

/**
 * validate cohorts both transitively and non-transitively for a student
 * 
 * @author ycao
 * 
 */
@Component
public class StudentToCohortValidator extends BasicValidator {
    
    public StudentToCohortValidator() {
        super(EntityNames.STUDENT, EntityNames.COHORT);
    }

    @Override
    protected boolean doValidate(Set<String> ids) {
        Entity myself = SecurityUtil.getSLIPrincipal().getEntity();
        if (myself == null || myself.getEmbeddedData() == null) {
            // not sure how this can happen
            return false;
        }
        
        List<Entity> studentCohortAssociations = myself.getEmbeddedData().get(EntityNames.STUDENT_COHORT_ASSOCIATION);
        Set<String> myCohorts = new HashSet<String>();
        for (Entity myCohortAssociation : studentCohortAssociations) {
            if (myCohortAssociation.getBody() != null) {
                myCohorts.add((String) myCohortAssociation.getBody().get(ParameterConstants.COHORT_ID));
            }
        }
        
        return myCohorts.containsAll(ids);
    }
    
}
