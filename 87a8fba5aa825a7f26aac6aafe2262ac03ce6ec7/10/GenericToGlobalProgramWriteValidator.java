package org.slc.sli.api.security.context.validator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;

/**
 * This validator validates Write access to programs: this is defined as having
 * a direct association to the program, or being directly associated to an edorg
 * which references the program
 * 
 * This logic is applied for both teachers and staff
 * 
 */
@Component
public class GenericToGlobalProgramWriteValidator extends AbstractContextValidator {

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return EntityNames.PROGRAM.equals(entityType) && isTransitive && !isStudentOrParent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> validate(String entityType, Set<String> ids)
            throws IllegalStateException {
        if (!areParametersValid(EntityNames.PROGRAM, entityType, ids)) {
            return Collections.emptySet();
        }
        Set<String> directEdorgs = getDirectEdorgs();

        // Fetch programs of your edorgs
        NeutralQuery nq = new NeutralQuery(new NeutralCriteria(
                ParameterConstants.ID, NeutralCriteria.CRITERIA_IN,directEdorgs, false));
        Iterable<Entity> edorgs = getRepo().findAll(EntityNames.EDUCATION_ORGANIZATION, nq);

        Set<String> programsToValidate = new HashSet<String>(ids);

        for (Entity ed : edorgs) {
            List<String> programs = (List<String>) ed.getBody().get(ParameterConstants.PROGRAM_REFERENCE);

            if (programs != null && programs.size() > 0) {
                programsToValidate.removeAll(programs);
                if (programsToValidate.isEmpty()) {
                    return ids;
                }
            }
        }

        // Fetch associations
        nq = new NeutralQuery(new NeutralCriteria(ParameterConstants.STAFF_ID,
                NeutralCriteria.OPERATOR_EQUAL, SecurityUtil.getSLIPrincipal().getEntity().getEntityId()));
        addEndDateToQuery(nq, false);
        Iterable<Entity> assocs = getRepo().findAll(EntityNames.STAFF_PROGRAM_ASSOCIATION, nq);

        for (Entity assoc : assocs) {
            programsToValidate.remove((String) assoc.getBody().get(ParameterConstants.PROGRAM_ID));
            if (programsToValidate.isEmpty()) {
                return ids;
            }
        }

        // If we made it this far, there's still programs that didn't validate,
        Set<String> validIds = new HashSet<String>(ids);
        validIds.removeAll(programsToValidate);
        return validIds;
    }

    @Override
    public SecurityUtil.UserContext getContext() {
        return SecurityUtil.UserContext.DUAL_CONTEXT;
    }
}
