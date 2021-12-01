package org.slc.sli.api.security.context;

import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.domain.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class EntityOwnershipValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EntityOwnershipValidator.class);

    private Set<String> globalEntities;

    @Autowired
    private EdOrgOwnershipArbiter arbiter;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {

        globalEntities = new HashSet<String>(Arrays.asList(EntityNames.ASSESSMENT,
    EntityNames.COMPETENCY_LEVEL_DESCRIPTOR, EntityNames.EDUCATION_ORGANIZATION, EntityNames.SCHOOL,
    EntityNames.LEARNING_OBJECTIVE, EntityNames.LEARNING_STANDARD, EntityNames.PROGRAM,
    EntityNames.GRADING_PERIOD, EntityNames.SESSION, EntityNames.COURSE, EntityNames.COURSE_OFFERING,
            "stateEducationAgency", "localEducationAgency",
    EntityNames.CLASS_PERIOD, EntityNames.BELL_SCHEDULE));
    }
    /**
     * Determines if the requested entity can be accessed. The implicit assumption in using this
     * function is that access to the entity requested is transitive (only to the entity requested,
     * and not through the entity). If this is not the case, use the canAccess method that takes the
     * isTransitive flag as an argument.
     *
     * @param entity
     *            Requested entity.
     * @return True if the requested entity can be accessed, false otherwise.
     */
    public boolean canAccess(Entity entity) {
        return canAccess(entity, true);
    }

    /**
     * Determines if the requested entity can be accessed in the specified transitive or
     * non-transitive way.
     *
     * @param entity
     *            Requested entity.
     * @param isTransitive
     *            Flag used for specifying transitive vs. non-transitive access to the entity.
     * @return True if the requested entity can be accessed, false otherwise.
     */
    public boolean canAccess(Entity entity, boolean isTransitive) {
        LOG.trace(">>>EntityOwnershipValidator.canAccess()");

        boolean result = false;

        if (SecurityUtil.getSLIPrincipal().getAuthorizingEdOrgs() == null) {
            // explicitly set null if the app is marked as authorized_for_all_edorgs
            LOG.trace("  ...authorized for all edorgs.");
            result = true;
        } else if (Arrays.asList(EntityNames.PROGRAM, EntityNames.SESSION).contains(entity.getType())) {
            //  Some entities are just cannot be pnwed
            LOG.trace("  ...program or session.");
            result = true;
        } else if (isTransitive && globalEntities.contains(entity.getType())) {
            LOG.trace("skipping ownership validation --> transitive access to global entity: {}", entity.getType());
            result = true;
        } else {
            Set<String> owningEdorgs = arbiter.determineEdorgs(Arrays.asList(entity), entity.getType());
            if (owningEdorgs.size() == 0) {
                LOG.warn("Potentially bad data found.");
                result = true;
            } else {
                for (String edOrgId : owningEdorgs) {
                    if (SecurityUtil.getSLIPrincipal().getAuthorizingEdOrgs().contains(edOrgId)) {
                        LOG.trace("discovered owning education organization: {}", edOrgId);
                        result = true;
                    }
                }
            }
        }

        LOG.trace("  ...returning canAccess result: " + result);
        return result;
    }

}
