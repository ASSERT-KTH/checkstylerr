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
package org.slc.sli.bulk.extract.date;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slc.sli.common.constants.ContainerEntityNames;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;

/**
 * @author ablum
 */
public class EntityDates {

    public static final Map<String, String> ENTITY_DATE_FIELDS = new HashMap<String, String>();

    public static final Map<String, Map<String, String>> ENTITY_PATH_FIELDS = new HashMap<String, Map<String, String>>();

    public static final Set<String> NON_DATED_ENTITIES = new HashSet<String>();
    static {
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_PROGRAM_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_COHORT_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.DISCIPLINE_INCIDENT, ParameterConstants.INCIDENT_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.DISCIPLINE_ACTION, ParameterConstants.DISCIPLINE_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_ASSESSMENT, ParameterConstants.ADMINISTRATION_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_SCHOOL_ASSOCIATION, ParameterConstants.ENTRY_DATE);
        ENTITY_DATE_FIELDS.put(ContainerEntityNames.YEARLY_TRANSCRIPT, ParameterConstants.SCHOOL_YEAR);
        ENTITY_DATE_FIELDS.put(EntityNames.ATTENDANCE, ParameterConstants.SCHOOL_YEAR);
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_ACADEMIC_RECORD, ParameterConstants.SCHOOL_YEAR);
        ENTITY_DATE_FIELDS.put(EntityNames.STUDENT_SECTION_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.SESSION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.GRADEBOOK_ENTRY, ParameterConstants.DATE_ASSIGNED);
        ENTITY_DATE_FIELDS.put(EntityNames.STAFF_ED_ORG_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.TEACHER_SECTION_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.STAFF_PROGRAM_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.STAFF_COHORT_ASSOCIATION, ParameterConstants.BEGIN_DATE);
        ENTITY_DATE_FIELDS.put(EntityNames.GRADE, ParameterConstants.SCHOOL_YEAR);
        ENTITY_DATE_FIELDS.put(EntityNames.REPORT_CARD, ParameterConstants.SCHOOL_YEAR);

        Map<String, String> diFields = new HashMap<String, String>();
        diFields.put(EntityNames.DISCIPLINE_INCIDENT, ParameterConstants.DISCIPLINE_INCIDENT_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.STUDENT_DISCIPLINE_INCIDENT_ASSOCIATION, diFields);

        Map<String, String> ctFields = new HashMap<String, String>();
        ctFields.put(EntityNames.STUDENT_ACADEMIC_RECORD, ParameterConstants.STUDENT_ACADEMIC_RECORD_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.COURSE_TRANSCRIPT, ctFields);

        Map<String, String> scFields = new HashMap<String, String>();
        scFields.put(EntityNames.STUDENT_SECTION_ASSOCIATION, ParameterConstants.STUDENT_SECTION_ASSOCIATION_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.STUDENT_COMPETENCY, scFields);

        Map<String, String> sectionFields = new HashMap<String, String>();
        sectionFields.put(EntityNames.COURSE_OFFERING, ParameterConstants.COURSE_OFFERING_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.SECTION, sectionFields);

        Map<String, String> coFields = new HashMap<String, String>();
        coFields.put(EntityNames.SESSION, ParameterConstants.SESSION_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.COURSE_OFFERING, coFields);

        Map<String, String> sgbFields = new HashMap<String, String>();
        sgbFields.put(EntityNames.GRADEBOOK_ENTRY, ParameterConstants.GRADEBOOK_ENTRY_ID);
        ENTITY_PATH_FIELDS.put(EntityNames.STUDENT_GRADEBOOK_ENTRY, sgbFields);

        NON_DATED_ENTITIES.add(EntityNames.STUDENT);
        NON_DATED_ENTITIES.add(EntityNames.STUDENT_PARENT_ASSOCIATION);
        NON_DATED_ENTITIES.add(EntityNames.PARENT);
        NON_DATED_ENTITIES.add(EntityNames.STAFF);
        NON_DATED_ENTITIES.add(EntityNames.TEACHER);
    }

}
