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
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.api.security.context.PagingRepositoryDelegate;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates the context of a staff member to see the requested set of teacher entities.
 * Returns true if the staff member can see ALL of the entities, and false otherwise.
 *
 * @author mabernathy
 */
@Component
public class StaffToTeacherValidator extends AbstractContextValidator {

    @Autowired
    private PagingRepositoryDelegate<Entity> repo;

    @Override
    public boolean canValidate(String entityType, boolean through) {
        return EntityNames.TEACHER.equals(entityType) && isStaff();
    }

    @Override
    public Set<String> validate(String entityName, Set<String> teacherIds) throws IllegalStateException {
        if (!areParametersValid(EntityNames.TEACHER, entityName, teacherIds)) {
            return Collections.EMPTY_SET;
        }
        
        // Query teacher's schools
        NeutralQuery basicQuery = new NeutralQuery(new NeutralCriteria(ParameterConstants.STAFF_REFERENCE,
                NeutralCriteria.CRITERIA_IN,
                teacherIds));
        Iterable<Entity> schoolAssoc = repo.findAll(EntityNames.STAFF_ED_ORG_ASSOCIATION, basicQuery);
        Map<String, Set<String>> teacherSchoolMap = new HashMap<String, Set<String>>();
        populateMapFromMongoResponse(teacherSchoolMap, schoolAssoc);
        Set<String> edOrgLineage = getStaffEdOrgLineage();

        Set<String> validTeacherIds = new HashSet<String>();

        for (String teacher : teacherSchoolMap.keySet()) {

            // Make sure there's a valid intersection between the schools and edOrgLIneage
            Set<String> tmpSet = new HashSet<String>(teacherSchoolMap.get(teacher));
            tmpSet.retainAll(edOrgLineage);
            if (tmpSet.size() != 0) {
                validTeacherIds.add(teacher);
            }
        }
        return validTeacherIds;
    }

    private void populateMapFromMongoResponse(Map<String, Set<String>> teacherSchoolMap, Iterable<Entity> schoolAssoc) {
        for (Entity assoc : schoolAssoc) {
            String teacherId = (String) assoc.getBody().get(ParameterConstants.STAFF_REFERENCE);
            String schoolId = (String) assoc.getBody().get(ParameterConstants.EDUCATION_ORGANIZATION_REFERENCE);
            if (!isFieldExpired(assoc.getBody(), ParameterConstants.END_DATE, false)) {
                Set<String> edorgList = teacherSchoolMap.get(teacherId);
                if (edorgList == null) {
                    edorgList = new HashSet<String>();
                    teacherSchoolMap.put(teacherId, edorgList);
                }
                edorgList.add(schoolId);
            }
        }
    }

}
