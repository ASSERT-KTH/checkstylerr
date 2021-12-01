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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.domain.Entity;

/**
 * Validator for student access to a staff cohort association
 *
 * @author nbrown
 *
 */
@Component
public class StudentToStaffCohortValidator extends StudentToStaffAssociation {

    public StudentToStaffCohortValidator() {
        super(EntityNames.STAFF_COHORT_ASSOCIATION, "cohortId");
    }

    @Override
    protected List<String> getStudentAssociationIds(Entity me) {
        List<Entity> cohortAssociations = me.getEmbeddedData().get("studentCohortAssociation");
        List<String> myCohorts = new ArrayList<String>();
        for (Entity assoc : cohortAssociations) {
            if (!getDateHelper().isFieldExpired(assoc.getBody())) {
                myCohorts.add((String) assoc.getBody().get("cohortId"));
            }
        }
        return myCohorts;
    }


}
