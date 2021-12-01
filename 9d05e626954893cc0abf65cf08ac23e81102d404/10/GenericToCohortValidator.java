package org.slc.sli.api.security.context.validator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;

/**
 * 
 *
 */
@Component
public class GenericToCohortValidator extends AbstractContextValidator {

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return !isStudentOrParent() && !isTransitive && EntityNames.COHORT.equals(entityType);
    }

    @Override
    public boolean validate(String entityType, Set<String> ids) throws IllegalStateException {
        if (!areParametersValid(EntityNames.COHORT, entityType, ids)) {
            return false;
        }

        NeutralQuery basicQuery = new NeutralQuery(new NeutralCriteria(ParameterConstants.STAFF_ID, NeutralCriteria.OPERATOR_EQUAL, SecurityUtil.getSLIPrincipal().getEntity().getEntityId()));

        if (SecurityUtil.getSLIPrincipal().isStudentAccessFlag()) {
            basicQuery.addCriteria(new NeutralCriteria(ParameterConstants.STUDENT_RECORD_ACCESS, NeutralCriteria.OPERATOR_EQUAL, true));
        }
        
        Set<String> myCohortIds = new HashSet<String>();
        Iterable<Entity> scas = getRepo().findAll(EntityNames.STAFF_COHORT_ASSOCIATION, basicQuery);
        for (Entity sca : scas) {
            Map<String, Object> body = sca.getBody();
            if (!isFieldExpired(body, ParameterConstants.END_DATE, true)) {
                myCohortIds.add((String) body.get(ParameterConstants.COHORT_ID));
            }
        }

        return myCohortIds.containsAll(ids);
    }

}
