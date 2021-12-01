package org.slc.sli.bulk.extract.util;

import static org.slc.sli.bulk.extract.LogUtil.audit;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.slc.sli.bulk.extract.extractor.EntityExtractor;
import org.slc.sli.bulk.extract.message.BEMessageCode;
import org.slc.sli.bulk.extract.BulkExtractMongoDA;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.common.util.logging.LogLevelType;
import org.slc.sli.common.util.logging.SecurityEvent;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.domain.Repository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Utils to extract LEAs
 */
@Component
public class EdOrgExtractHelper implements InitializingBean {

    private Set<String> extractEdOrgs;

    @Autowired
    @Qualifier("secondaryRepo")
    Repository<Entity> repository;

    @Autowired
    private SecurityEventUtil securityEventUtil;

    @Autowired
    private BulkExtractMongoDA bulkExtractMongoDA;

    private Map<String, List<String>> edOrgLineages;

    private static final String STATE_EDUCATION_AGENCY = "State Education Agency";

    @Override
    public void afterPropertiesSet() throws Exception {
        edOrgLineages = bulkExtractMongoDA.getEdOrgLineages();
    }

    /**
     * Returns all top level LEAs that will be extracted in the tenant
     * @return a set of top level lea ids
     * 
     * Returns the union of all edOrgs appearing as values in the 
     * map of application -> { authorized edOrgs }
     */
    public Set<String> getBulkExtractEdOrgs(String sea) {
        if (extractEdOrgs == null) {
            extractEdOrgs = new HashSet<String>();
            for (Set<String> appEdOrgs : getBulkExtractEdOrgsPerApp().values()) {
                extractEdOrgs.addAll(appEdOrgs);
            }
        }
        Set<String> result = removeNonExistentEdOrgs(extractEdOrgs);
        
		// Never include the SEA in a "local" (private data) extract, even if the SEA is authorized explicitly for
		// bulk extract app(s). To remove this hardwired restriction, and instead allow an SEA to have its private data extracted
		// to a file stamped (i.e. named) w/ the SEA edOrg id, remove the following line.
        result.remove(sea);
        
        return result;
    }

    private Set<String> removeNonExistentEdOrgs(Set<String> edOrgs) {
        NeutralQuery q = new NeutralQuery(new NeutralCriteria("_id", NeutralCriteria.CRITERIA_IN, edOrgs));
        Iterable<String> entities = repository.findAllIds(EntityNames.EDUCATION_ORGANIZATION, q);
        Set<String> existingEdOrgs = Sets.newHashSet(entities);
        return existingEdOrgs;
    }

    public Set<String> getTopLevelLEAs() {
        Set<String> topLEAs = new HashSet<String>();

        NeutralQuery query = new NeutralQuery(new NeutralCriteria(ParameterConstants.ORGANIZATION_CATEGORIES,
                NeutralCriteria.CRITERIA_IN, Arrays.asList(STATE_EDUCATION_AGENCY)));
        final Iterable<String> seaIds = repository.findAllIds(EntityNames.EDUCATION_ORGANIZATION, query);

        if (seaIds != null) {
            for (String seaId : seaIds) {
                query = new NeutralQuery(new NeutralCriteria(ParameterConstants.PARENT_EDUCATION_AGENCY_REFERENCE,
                        NeutralCriteria.CRITERIA_IN, Arrays.asList(seaId)));
                final Iterable<String> topLevelLEAs = repository.findAllIds(EntityNames.EDUCATION_ORGANIZATION, query);
                for (String topLevelLEA : topLevelLEAs) {
                    topLEAs.add(topLevelLEA);
                }
            }
        }
        return topLEAs;
    }

    /**
     * Attempts to get all of the LEAs per app that should have a LEA level extract scheduled.
     *
     * @return a set of the LEA ids that need a bulk extract per app
     */
    @SuppressWarnings("unchecked")
    public Map<String, Set<String>> getBulkExtractEdOrgsPerApp() {
        NeutralQuery appQuery = new NeutralQuery(new NeutralCriteria("applicationId", NeutralCriteria.CRITERIA_IN,
                getBulkExtractApps()));
        Iterable<Entity> apps = repository.findAll("applicationAuthorization", appQuery);
        Map<String, Set<String>> edorgIds = new HashMap<String, Set<String>>();
        for (Entity app : apps) {
            Set<String> edorgs = new HashSet<String>(BulkExtractMongoDA.getAuthorizedEdOrgIds(app));
            edorgIds.put((String) app.getBody().get("applicationId"), edorgs);
        }
        return edorgIds;
    }

    /**
     * A helper function to get the list of approved app ids that have bulk extract enabled
     *
     * @return a set of approved bulk extract app ids
     */
    @SuppressWarnings("unchecked")
    public Set<String> getBulkExtractApps() {
        TenantContext.setIsSystemCall(true);
        Iterable<Entity> apps = repository.findAll("application", new NeutralQuery());
        TenantContext.setIsSystemCall(false);
        Set<String> appIds = new HashSet<String>();
        for (Entity app : apps) {
            if (app.getBody().containsKey("isBulkExtract") && (Boolean) app.getBody().get("isBulkExtract")) {
                if (app.getBody().containsKey("registration") &&
                        "APPROVED".equals(((Map<String, Object>) app.getBody().get("registration")).get("status"))) {
                    appIds.add(app.getEntityId());
                }
            }
        }
        return appIds;
    }
    
    /**
     * Returns a list of child edorgs given a collection of parents
     * 
     * @param edOrgs
     * @return a set of child edorgs
     */
    public Set<String> getChildEdOrgs(Collection<String> edOrgs) {
        if (edOrgs.isEmpty()) {
            return new HashSet<String>();
        }
        
        NeutralQuery query = new NeutralQuery(new NeutralCriteria(ParameterConstants.PARENT_EDUCATION_AGENCY_REFERENCE,
                NeutralCriteria.CRITERIA_IN, edOrgs));
        Iterable<Entity> childrenIds = repository.findAll(EntityNames.EDUCATION_ORGANIZATION, query);
        Set<String> children = new HashSet<String>();
        for (Entity child : childrenIds) {
            children.add(child.getEntityId());
        }
        if (!children.isEmpty()) {
            children.addAll(getChildEdOrgs(children));
        }
        return children;
    }

    /**
     * Retrieve all the staff edorg association for a teacher and edorg.
     *
     * @param teacherId  teacher id
     * @param edorgId    education organization id
     *
     * @return  list of all the seoas for the teacher in the edorg
     */
    public Iterable<Entity> retrieveSEOAS(String teacherId, String edorgId) {
        NeutralQuery query = new NeutralQuery();
        query.addCriteria(new NeutralCriteria(ParameterConstants.EDUCATION_ORGANIZATION_REFERENCE, NeutralCriteria.OPERATOR_EQUAL, edorgId));
        query.addCriteria(new NeutralCriteria(ParameterConstants.STAFF_REFERENCE, NeutralCriteria.OPERATOR_EQUAL, teacherId));

        return repository.findAll(EntityNames.STAFF_ED_ORG_ASSOCIATION, query);
    }

    /**
     * Log security events when an extract is initiated for each LEA
     * @param leas
     *          list of LEAs
     * @param entityName
     *          name of the entity being extracted
     * @param className
     *          name of the class from where extract was initiated
     */
    public void logSecurityEvent(Set<String> leas, String entityName, String className) {
        for (String lea : leas) {
            SecurityEvent event = securityEventUtil.createSecurityEvent(className, entityName + " data extract initiated for LEA", LogLevelType.TYPE_INFO, BEMessageCode.BE_SE_CODE_0011, entityName);
            // @TA10431
            //event.setTargetEdOrg(lea);
            event.addTargetEdOrg(lea); //@TA10431
            audit(event);
        }

    }

    /**
     * Set securityEventUtil.
     * @param securityEventUtil the securityEventUtil to set
     */
    public void setSecurityEventUtil(SecurityEventUtil securityEventUtil) {
        this.securityEventUtil = securityEventUtil;
    }

    public Map<String, List<String>> getEdOrgLineages() {
        return edOrgLineages;
    }

    public void setEdOrgLineages(Map<String, List<String>> edOrgLineages) {
        this.edOrgLineages = edOrgLineages;
    }
}
