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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;

/**
 * Resolves which courseTranscripts any given staff member can access.
 *
 */
@Component
public class StaffToCourseTranscriptValidator extends AbstractContextValidator {

    @Autowired
    private StaffToSubStudentEntityValidator validator;

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return EntityNames.COURSE_TRANSCRIPT.equals(entityType) && isStaff();
    }

    @Override
    public Set<String> validate(String entityType, Set<String> ids) throws IllegalStateException {

        if (!areParametersValid(EntityNames.COURSE_TRANSCRIPT, entityType, ids)) {
            return Collections.emptySet();
        }

        info("Validating {}'s access to courseTranscripts: [{}]", SecurityUtil.getSLIPrincipal().getName(), ids);

        NeutralQuery query = new NeutralQuery(new NeutralCriteria(ParameterConstants.ID,
                NeutralCriteria.CRITERIA_IN, new ArrayList<String>(ids)));
        Iterable<Entity> entities = getRepo().findAll(EntityNames.COURSE_TRANSCRIPT, query);

        Map<String, String> studentAcademicRecords = new HashMap<String, String>();
        for (Entity entity : entities) {
            Map<String, Object> body = entity.getBody();
            if (body.get(ParameterConstants.STUDENT_ACADEMIC_RECORD_ID) instanceof String) {
                studentAcademicRecords.put((String) body.get(ParameterConstants.STUDENT_ACADEMIC_RECORD_ID), entity.getEntityId());
            } else {
                //studentacademicrecord ID was not a string, this is unexpected
                warn("Possible Corrupt Data detected at "+entityType+"/"+entity.getEntityId());
            }
        }

        if (studentAcademicRecords.isEmpty()) {
            return Collections.EMPTY_SET;
        }

        Set<String> validStudentAcademicRecords = validator.validate(EntityNames.STUDENT_ACADEMIC_RECORD, studentAcademicRecords.keySet());
        return getValidIds(validStudentAcademicRecords, studentAcademicRecords);
    }
}
