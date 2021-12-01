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
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slc.sli.api.security.context.PagingRepositoryDelegate;
import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;

/**
 * Validates the context of a teacher to see the requested set of student entities.
 * Returns true if the teacher member can see ALL of the entities, and false otherwise.
 *
 * @author mabernathy
 */
@Component
public class TeacherToStudentValidator extends AbstractContextValidator {

    @Autowired
    private PagingRepositoryDelegate<Entity> repo;

    @Override
    public boolean canValidate(String entityType, boolean through) {
        return EntityNames.STUDENT.equals(entityType)
                && isTeacher();
    }

    @Override
    public Set<String> validate(String entityName, Set<String> ids) throws IllegalStateException {
        if (!areParametersValid(EntityNames.STUDENT, entityName, ids)) {
            return Collections.emptySet();
        }

        Set<String> idsToValidate = new HashSet<String>(ids);
        Set<String> validIds = new HashSet<String>();

        Set<String> validWithSections = getValidatedWithSections(idsToValidate);
        idsToValidate.removeAll(validWithSections);
        validIds.addAll(validWithSections);
        if (idsToValidate.isEmpty()) {
            return validIds;
        }

        Set<String> validWithPrograms = getValidatedWithPrograms(idsToValidate);
        idsToValidate.removeAll(validWithPrograms);
        validIds.addAll(validWithPrograms);
        if (idsToValidate.isEmpty()) {
            return validIds;
        }


        validIds.addAll(getValidatedWithCohorts(idsToValidate));
        return validIds;
    }

    @Override
    public Set<String> getValid(String entityType, Set<String> ids) {
        Set<String> originalIds = new HashSet<String>(ids);

        Set<String> validated = getValidatedWithSections(originalIds);
        originalIds.removeAll(validated);
        if (originalIds.isEmpty()) {
            return validated;
        }


        validated.addAll(getValidatedWithCohorts(originalIds));
        originalIds.removeAll(validated);
        if (originalIds.isEmpty()) {
            return validated;
        }

        validated.addAll(getValidatedWithPrograms(originalIds));
        originalIds.removeAll(validated);
        return validated;
    }

    private Set<String> getValidatedWithSections(Set<String> ids) {
        Set<String> result = new HashSet<String>();
        if (ids.size() == 0) {
            return result;
        }


        Map<String, List<String>> studentSectionIds = getStudentParameterIds(Lists.newArrayList(ids), ParameterConstants.SECTION_ID,
                EntityNames.STUDENT_SECTION_ASSOCIATION);

        if (studentSectionIds.size() == 0) {
            return result;
        }

        Set<String> teacherSections = getTeacherSections();
        for (Entry<String, List<String>> studentEntry : studentSectionIds.entrySet()) {
            Set<String> tempSet = new HashSet<String>(teacherSections);
            tempSet.retainAll(studentEntry.getValue());
            if (!tempSet.isEmpty()) {
                result.add(studentEntry.getKey());
            }
        }

        return result;
    }

    private Set<String> getValidatedWithCohorts(Set<String> ids) {
        Set<String> staffCohortIds = getStaffCohortIds();

        NeutralQuery query = new NeutralQuery(new NeutralCriteria(ParameterConstants.COHORT_ID,
                NeutralCriteria.CRITERIA_IN, staffCohortIds));
        Iterable<Entity> studentList = repo.findAll(EntityNames.STUDENT_COHORT_ASSOCIATION, query);
        Set<String> studentIds = new HashSet<String>();

        // filter on end_date to get list of students
        for (Entity student : studentList) {
            if (!getDateHelper().isFieldExpired(student.getBody(), ParameterConstants.END_DATE, false)) {
                studentIds.add((String) student.getBody().get(ParameterConstants.STUDENT_ID));
            }

        }
        return studentIds;
    }

    private Set<String> getValidatedWithPrograms(Set<String> ids) {
        Set<String> result = new HashSet<String>();

        if (ids.size() == 0) {
            return result;
        }

        Set<String> staffProgramIds = getStaffPrograms();

        Map<String, List<String>> studentProgramIds = getStudentParameterIds(Lists.newArrayList(ids), ParameterConstants.PROGRAM_ID, EntityNames.STUDENT_PROGRAM_ASSOCIATION);

        if (studentProgramIds.size() == 0) {
            // students not found by program
            return result;
        }

        Set<String> tempSet = new HashSet<String>(staffProgramIds);
        // Get studentProgramAssociations
        for (Entry<String, List<String>> studentEntry : studentProgramIds.entrySet()) {
            tempSet.retainAll(studentEntry.getValue());

            if (!tempSet.isEmpty()) {
                result.add(studentEntry.getKey());
            }

            tempSet.clear();
            tempSet.addAll(staffProgramIds);
        }

        return result;
    }

    private Map<String, List<String>> getStudentParameterIds(List<String> ids, String parameter, String entityName) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (String id : ids) {
            result.put(id, new ArrayList<String>());
        }

        NeutralQuery queryForStudentRecord = new NeutralQuery(new NeutralCriteria(ParameterConstants.STUDENT_ID, NeutralCriteria.CRITERIA_IN, ids));
        addEndDateToQuery(queryForStudentRecord, false);

        for (Entity studentEntity : repo.findAll(entityName, queryForStudentRecord)) {
            List<String> parameterIds = result.get(studentEntity.getBody().get(ParameterConstants.STUDENT_ID));
            parameterIds.add((String) studentEntity.getBody().get(parameter));
        }

        return result;
    }

    private Set<String> getStaffPrograms() {
        Set<String> staffProgramIds = new HashSet<String>();

        // Get my staffProgramAssociations
        NeutralQuery basicQuery = new NeutralQuery(new NeutralCriteria(ParameterConstants.STAFF_ID, NeutralCriteria.OPERATOR_EQUAL,
                SecurityUtil.getSLIPrincipal().getEntity().getEntityId()));
        addEndDateToQuery(basicQuery, false);
        basicQuery.addCriteria(new NeutralCriteria(ParameterConstants.STUDENT_RECORD_ACCESS,
                NeutralCriteria.OPERATOR_EQUAL, true));
        Iterable<Entity> staffCas = repo.findAll(EntityNames.STAFF_PROGRAM_ASSOCIATION, basicQuery);

        // Look at only the SCAs for programs in my edorg with date/record access
        for (Entity sca : staffCas) {
            String programId = (String) sca.getBody().get(ParameterConstants.PROGRAM_ID);
            staffProgramIds.add(programId);
        }
        return staffProgramIds;
    }

    private Set<String> getTeacherSections() {
        Set<String> teacherSectionIds = new HashSet<String>();

        NeutralQuery basicQuery = new NeutralQuery(
                new NeutralCriteria(ParameterConstants.TEACHER_ID, NeutralCriteria.OPERATOR_EQUAL, SecurityUtil
                        .getSLIPrincipal().getEntity().getEntityId()));
        addEndDateToQuery(basicQuery, true);

        Iterable<Entity> tsas = repo.findAll(EntityNames.TEACHER_SECTION_ASSOCIATION, basicQuery);

        // Look at only the SCAs for programs in my edorg with date/record access
        for (Entity tsa : tsas) {
            String sectionId = (String) tsa.getBody().get(ParameterConstants.SECTION_ID);
            teacherSectionIds.add(sectionId);
        }
        return teacherSectionIds;
    }


    private Set<String> getStaffCohortIds() {
        Set<String> staffCohortIds = new HashSet<String>();

        // Get my staffCohortAssociations
        NeutralQuery basicQuery = new NeutralQuery(new NeutralCriteria(ParameterConstants.STAFF_ID, NeutralCriteria.OPERATOR_EQUAL,
                SecurityUtil.getSLIPrincipal().getEntity().getEntityId()));
        basicQuery.addCriteria(new NeutralCriteria(ParameterConstants.STUDENT_RECORD_ACCESS,
                NeutralCriteria.OPERATOR_EQUAL, true));
        addEndDateToQuery(basicQuery, false);
        Iterable<Entity> staffCas = repo.findAll(EntityNames.STAFF_COHORT_ASSOCIATION, basicQuery);
        // Look at only the SCAs for cohorts in my edorg with date/record access
        if (staffCas != null) {
            for (Entity sca : staffCas) {
                String cohortId = (String) sca.getBody().get(ParameterConstants.COHORT_ID);
                staffCohortIds.add(cohortId);
            }
        }
        return staffCohortIds;
    }

    @Override
    public void setRepo(PagingRepositoryDelegate<Entity> repo) {
        this.repo = repo;
    }
}
