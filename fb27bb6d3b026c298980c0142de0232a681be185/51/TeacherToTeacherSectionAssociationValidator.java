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

import com.google.common.collect.Sets;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Validates teacher's access to given teacherSectionAssociations
 * Teacher can see his/hers teacherSectionAssociations
 * 
 * @author dkornishev
 * 
 */
@Component
public class TeacherToTeacherSectionAssociationValidator extends AbstractContextValidator {

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return EntityNames.TEACHER_SECTION_ASSOCIATION.equals(entityType) && isTeacher();
    }

	@Override
	public Set<String> validate(String entityType, Set<String> ids) throws IllegalStateException {
        if (!areParametersValid(EntityNames.TEACHER_SECTION_ASSOCIATION, entityType, ids)) {
            return Collections.EMPTY_SET;
        }

        NeutralQuery nq = new NeutralQuery(new NeutralCriteria(ParameterConstants.TEACHER_ID,
                NeutralCriteria.OPERATOR_EQUAL, SecurityUtil.getSLIPrincipal().getEntity().getEntityId()));
        nq.addCriteria(new NeutralCriteria(ParameterConstants.ID, NeutralCriteria.CRITERIA_IN, ids));
        
        Iterable<String> tsaIds = getRepo().findAllIds(EntityNames.TEACHER_SECTION_ASSOCIATION, nq);
        return Sets.newHashSet(tsaIds);
    }

    @Override
    public SecurityUtil.UserContext getContext() {
        return SecurityUtil.UserContext.TEACHER_CONTEXT;
    }

}
