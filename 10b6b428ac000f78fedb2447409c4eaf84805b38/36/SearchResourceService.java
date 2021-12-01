package org.slc.sli.api.search.service;

import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slc.sli.api.config.EntityDefinition;
import org.slc.sli.api.constants.Constraints;
import org.slc.sli.api.constants.ResourceNames;
import org.slc.sli.api.criteriaGenerator.GranularAccessFilter;
import org.slc.sli.api.criteriaGenerator.GranularAccessFilterProvider;
import org.slc.sli.api.exceptions.EntityTypeNotFoundException;
import org.slc.sli.api.representation.EntityBody;
import org.slc.sli.api.resources.generic.PreConditionFailedException;
import org.slc.sli.api.resources.generic.representation.Resource;
import org.slc.sli.api.resources.generic.representation.ServiceResponse;
import org.slc.sli.api.resources.generic.service.DefaultResourceService;
import org.slc.sli.api.resources.generic.util.ResourceHelper;
import org.slc.sli.api.security.SLIPrincipal;
import org.slc.sli.api.security.context.ContextValidator;
import org.slc.sli.api.security.context.resolver.EdOrgHelper;
import org.slc.sli.api.service.EntityService;
import org.slc.sli.api.service.query.ApiQuery;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.common.domain.EmbeddedDocumentRelations;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;

//import org.elasticsearch.client.Client;
//import org.elasticsearch.common.collect.Lists;
//import org.elasticsearch.common.settings.ImmutableSettings;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.node.Node;
//import org.elasticsearch.node.NodeBuilder;

/**
 * Service class to handle all API search requests. Retrieves results using data
 * access classes. Queries and filters results based on the user's security
 * context (role, ed-org, school, section assocs, etc.)
 */

@Component
public class SearchResourceService {

   private static final Logger LOG = LoggerFactory.getLogger(SearchResourceService.class);

   private static final String CONTEXT_SCHOOL_ID = "context.schoolId";
   private static final String ORPHANED_METADATA = "_metaData.isOrphaned";
   private static final String CREATEDBY_METADATA = "_metaData.createdBy";


    // Minimum limit on results to retrieve from Elasticsearch each trip
   private static final int MINIMUM_ES_LIMIT_PER_QUERY = 10;

   @Autowired
   DefaultResourceService defaultResourceService;

   @Autowired
   private ResourceHelper resourceHelper;

   @Autowired
   private EdOrgHelper edOrgHelper;

   @Value("${sli.search.maxUnfilteredResults:15000}")
   private int maxUnfilteredSearchResultCount;

   @Value("${sli.search.maxFilteredResults:250}")
   private int maxFilteredSearchResultCount;

   @Value("${sli.search.maxFilteredResultsOverride:5000}")
   private int maxFilteredSearchResultOverrideCount;

   /**
    * Static class that defines the list of entities that should use the extended search limit.
    +  The business said to increase the value from 250 (DE2300).
    +  TODO Refactor (refer to class level note).
    */
   protected static class ExtendedSearchEntities {
       private static final Set<String> EXTENDED_LIMIT_ENTITIES = new HashSet<String>();

       static {
           EXTENDED_LIMIT_ENTITIES.add(ResourceNames.ASSESSMENTS.toLowerCase());
           EXTENDED_LIMIT_ENTITIES.add(ResourceNames.COMPETENCY_LEVEL_DESCRIPTORS.toLowerCase());
           EXTENDED_LIMIT_ENTITIES.add(ResourceNames.LEARNINGOBJECTIVES.toLowerCase());
           EXTENDED_LIMIT_ENTITIES.add(ResourceNames.LEARNINGSTANDARDS.toLowerCase());
           EXTENDED_LIMIT_ENTITIES.add(ResourceNames.STUDENT_COMPETENCY_OBJECTIVES.toLowerCase());
       }

       /**
        * Returns true if ResourceName should use the extended search value.
        *
        * @param resourceName - Resource name
        *
        * @return true if the extended search value should be used.
        */
       public static boolean find(final String resourceName) {
           if (StringUtils.isEmpty(resourceName)) {
               return false;
           }
           return ExtendedSearchEntities.EXTENDED_LIMIT_ENTITIES.contains(resourceName.toLowerCase());
       }
   }

   @Autowired
   private ContextValidator contextValidator;

   @Autowired
   GranularAccessFilterProvider granularAccessFilterProvider;

   private EntityDefinition searchEntityDefinition;

   // keep parameters for ElasticSearch
   // "q" is the query parameter in the url (i.e. /api/rest/v1/search?q=Matt)
   private static final List<String> WHITE_LIST_PARAMETERS = Arrays.asList(new String[]{"q"});

   /**
    * Initialize class.
    */
   @PostConstruct
   public void init() {
      searchEntityDefinition = resourceHelper.getEntityDefinition(EntityNames.SEARCH);
   }

   protected EntityService getService() {
      return searchEntityDefinition.getService();
   }

   protected String getType() {
      return searchEntityDefinition.getType();
   }

   /**
    * Main entry point for retrieving search results.
    *
    * @param resource - Resource
    * @param entity - The entity upon which to search
    * @param queryUri - URI of query
    * @param routeToDefaultApp - get ids via search app and route the request to the default
    *                          app, attaching the ids
    * @return - Service Response
    */
   public ServiceResponse list(Resource resource, final String entity, URI queryUri, boolean routeToDefaultApp) {
      //LOG.debug("   entity: {}", entity);
      List<EntityBody> finalEntities = null;
      Boolean moreEntities;

      // set up query criteria, make query
      try {
         ApiQuery apiQuery = prepareQuery(resource, entity, queryUri);
         Pair<? extends List<EntityBody>, Boolean> resultPair = retrieveResults(entity, apiQuery);
         finalEntities = resultPair.getLeft();
         moreEntities = resultPair.getRight();
         if (routeToDefaultApp) {
            finalEntities = routeToDefaultApp(finalEntities, new ApiQuery(queryUri));
         } else {
            setRealEntityTypes(finalEntities);
         }
      } catch (HttpStatusCodeException hsce) { // TODO: create some sli
         // exception for this
         // warn("Error retrieving results from ES: " + hsce.getMessage());
         // if item not indexed, throw Illegal
         if (hsce.getStatusCode() == HttpStatus.NOT_FOUND || hsce.getStatusCode().value() >= 500) {
            throw (IllegalArgumentException) new IllegalArgumentException(
                  "Search is not available for the user at this moment.").initCause(hsce);
         }
         throw hsce;
      }

      if (moreEntities) {
         ApiQuery query = new ApiQuery(queryUri);
         return new ServiceResponse(finalEntities, finalEntities.size() + query.getLimit() + query.getOffset());
      } else {
         return new ServiceResponse(finalEntities, finalEntities.size());
      }
   }

   private List<EntityBody> routeToDefaultApp(List<EntityBody> entities, ApiQuery query) {
      List<EntityBody> fullEntities = new ArrayList<EntityBody>();
      Table<String, String, EntityBody> entityMap = getEntityTable(entities);
      NeutralCriteria criteria = null;
      // got through each type and execute list() for the list of ids provided
      // by search

      toSubDocCompatible(query, EmbeddedDocumentRelations.getSubDocuments());

      for (String type : entityMap.rowKeySet()) {
         if (criteria != null) {
            query.removeCriteria(criteria);
         }
         criteria = new NeutralCriteria("_id", NeutralCriteria.CRITERIA_IN, entityMap.row(type).keySet());
         query.addCriteria(criteria);
         query.setOffset(0);
         Iterables.addAll(fullEntities, resourceHelper.getEntityDefinitionByType(type).getService().listBasedOnContextualRoles(query));
      }
      return fullEntities;
   }

   private void toSubDocCompatible(NeutralQuery query, Set<String> subdocs) {
      List<NeutralCriteria> newCriteria = new ArrayList<NeutralCriteria>();
      for (NeutralCriteria criteria : query.getCriteria()) {
         String key = criteria.getKey().split("\\.")[0];
         if (invalidCriteria(key)) {
            continue;
         }
         if (subdocs.contains(key)) {
            criteria.setCanBePrefixed(false);
            criteria.setKey(key + ".body" + criteria.getKey().substring(key.length()));
         }
         newCriteria.add(criteria);
      }
      query.setQueryCriteria(newCriteria);
   }

   private boolean invalidCriteria(String key) {
      //assessmentPeriodDescriptor and assessmentFamilyHierarchyName is no longer in assessment,
      //can't query on those two fields anymore, they will not yield correct result
      return "assessmentPeriodDescriptor".equals(key) || "assessmentFamilyHierarchyName".equals(key);
   }

   /**
    * Takes an ApiQuery and retrieve results. Includes logic for pagination and
    * calls methods to filter by security context.
    *
    * @param entity - The entity upon which to search
    * @param apiQuery - API query
    *
    * @return - Result of query
    */
   public Pair<? extends List<EntityBody>, Boolean> retrieveResults(final String entity, ApiQuery apiQuery) {
       LOG.debug(">>>SearchResourceService.retrieveResults()");

       int maxSearchResultCount =  this.maxFilteredSearchResultCount;

       /* DE2300 - Use maxFilteredSearchResultOverrideCount as the search limit if this is an extended search entity. */
       if (SearchResourceService.ExtendedSearchEntities.find(entity)) {
           maxSearchResultCount = this.maxFilteredSearchResultOverrideCount;
       }

       /* Get the offset and limit requested. */
       int limit = apiQuery.getLimit();

        /* Use local max value if the more obvious HARD_ENTITY_COUNT_LIMIT or 0 was specified. */
       if (limit == 0 || limit == Constraints.HARD_ENTITY_COUNT_LIMIT) {
         limit = maxSearchResultCount;
      }

       if (limit > maxSearchResultCount) {
         String errorMessage = "Invalid condition, limit [" + limit
               + "] cannot be greater than maxFilteredResults [" + maxSearchResultCount + "] on search";
         LOG.error(errorMessage);
         throw new PreConditionFailedException(errorMessage);
      }

      int offset = apiQuery.getOffset();
      int totalLimit = limit + offset + 1;
      LOG.debug("   totalLimit: {}", totalLimit);

        /* Based on the requested offset and limit, calculate new offset and limit for retrieving data in batches from
        * Elasticsearch.  this is necessary because some Elasticsearch results will be filtered out based on
        * security context.
        */
      int limitPerQuery = totalLimit * 2;
      if (limitPerQuery < MINIMUM_ES_LIMIT_PER_QUERY) {
         limitPerQuery = MINIMUM_ES_LIMIT_PER_QUERY;
      }
      apiQuery.setLimit(limitPerQuery);
      apiQuery.setOffset(0);

      List<EntityBody> entityBodies = null;
      List<EntityBody> finalEntities = new ArrayList<EntityBody>();

      while (finalEntities.size() <= totalLimit
            && apiQuery.getOffset() + limitPerQuery < this.maxUnfilteredSearchResultCount) {

         // call BasicService to query the elastic search repo
         entityBodies = (List<EntityBody>) getService().listBasedOnContextualRoles(apiQuery);
         LOG.debug("Got {} entities back", entityBodies.size());
         int lastSize = entityBodies.size();
         finalEntities.addAll(filterResultsBySecurity(entityBodies, offset, limit));

         // if no more results to grab, then we're done
         if (lastSize < limitPerQuery && lastSize < limit) {
            break;
         }

         apiQuery.setOffset(apiQuery.getOffset() + apiQuery.getLimit());
      }

      // debug("finalEntities " + finalEntities.size() + " totalLimit " +
      // totalLimit + " offset " + offset);
      if (finalEntities.size() < offset) {
         return Pair.of(new ArrayList<EntityBody>(), false);
      }

      finalEntities.subList(0, offset).clear();
      if (finalEntities.size() <= limit) {
         return Pair.of(finalEntities, false);
      } else {
         int upperBound = limit > finalEntities.size() ? finalEntities.size() : limit;
         return Pair.of(finalEntities.subList(0, upperBound), true);
      }
   }

   /**
    * Replace entity type 'search' with the real entity types
    *
    * @param entities
    */
   private void setRealEntityTypes(List<EntityBody> entities) {
      for (EntityBody entity : entities) {
         entity.put("entityType", entity.get("type"));
         entity.remove("type");
      }
   }

   /**
    * Prepare an ApiQuery to send to the search repository. Creates the
    * ApiQuery from the query URI, sets query criteria and security context
    * criteria.
    *
    * @param resource - Resource
    * @param entities - Entities
    * @param queryUri - URI of query
    *
    * @return - API query
    */
   public ApiQuery prepareQuery(Resource resource, String entities, URI queryUri) {
      ApiQuery apiQuery = new ApiQuery(queryUri);
      addGranularAccessCriteria(entities, apiQuery);
      filterCriteria(apiQuery);
      addSecurityContext(apiQuery);
      if (entities != null) {
         apiQuery.addCriteria(new NeutralCriteria("_type", NeutralCriteria.CRITERIA_IN, getEntityTypes(resource,
               entities)));
      }
      return apiQuery;
   }

   /**
    * Given string of resource names, get corresponding string of entity types
    *
    * @param resourceNames
    * @return
    */
   private Collection<String> getEntityTypes(Resource resource, String resourceNames) {
      List<String> entityTypes = new ArrayList<String>();
      EntityDefinition def;
      for (String resourceName : resourceNames.split(",")) {
         def = resourceHelper.getEntityDefinition(resourceName);
         if (def == null || !searchEntityDefinition.getService().collectionExists(def.getType())) {
            throw new EntityTypeNotFoundException(resourceName);
         }
         entityTypes.add(def.getType());
      }
      return entityTypes;
   }

   /**
    * Return list of accessible entities, filtered through the security
    * context. Original list may by cross-collection. Retains the original
    * order of entities.
    *
    * @param entityBodies - Total entities to query
    * @param offset - Query offset
    * @param limit  -Total requested entities
    *
    * @return - Filter result
    */
   public Collection<EntityBody> filterResultsBySecurity(List<EntityBody> entityBodies, int offset, int limit) {
      if (entityBodies == null || entityBodies.isEmpty()) {
         return entityBodies;
      }

      int total = offset + limit + 1;
      List<EntityBody> sublist;
      // this collection will be filtered out based on security context but
      // the original order will be preserved
      List<EntityBody> finalEntities = new ArrayList<EntityBody>(entityBodies);
      final HashBasedTable<String, String, EntityBody> filterMap = HashBasedTable.create();
      Table<String, String, EntityBody> entitiesByType = HashBasedTable.create();
      // filter results through security context
      // security checks are expensive, so do min checks necessary at a time
      while (!entityBodies.isEmpty() && filterMap.size() < total) {
         sublist = new ArrayList<EntityBody>(entityBodies.subList(0, Math.min(entityBodies.size(), limit)));
         entitiesByType = getEntityTable(sublist);

         for (String type : entitiesByType.rowKeySet()) {
                /*
                 * Skip validation for global entities.
                 */
            if (contextValidator.isGlobalEntity(type)) {
               LOG.debug("search: skipping validation --> global entity: {}", type);
               Map<String, EntityBody> row = entitiesByType.row(type);
               Set<String> accessible = row.keySet();
               for (String id : accessible) {
                  if (row.containsKey(id)) {
                     filterMap.put(id, type, row.get(id));
                  }
               }
            } else {
               LOG.debug("search: validating entity: {}", type);
               Map<String, EntityBody> row = entitiesByType.row(type);
               Set<String> accessible = filterOutInaccessibleIds(type, row.keySet());
               for (String id : accessible) {
                  if (row.containsKey(id)) {
                     filterMap.put(id, type, row.get(id));
                  }
               }
            }
         }
         entityBodies.removeAll(sublist);
         entitiesByType.clear();
      }

      // use filter map to return final entity list
      return Lists.newArrayList(Iterables.filter(finalEntities, new Predicate<EntityBody>() {
         @Override
         public boolean apply(EntityBody input) {
            return (filterMap.contains(input.get("id"), input.get("type")));
         }
      }));
   }

   /**
    * Get entities table by type, by ids
    *
    * @param entityList
    * @return
    */
   private Table<String, String, EntityBody> getEntityTable(List<EntityBody> entityList) {
      HashBasedTable<String, String, EntityBody> entitiesByType = HashBasedTable.create();
      for (EntityBody entity : entityList) {
         entitiesByType.put((String) entity.get("type"), (String) entity.get("id"), entity);
      }
      return entitiesByType;
   }

    /**
     * Filter id set to get accessible ids.
     *
     * @param toType - Entity type
     * @param ids - Entity IDs
     *
     * @return - Accessible IDs
     */
    public Set<String> filterOutInaccessibleIds(String toType, Set<String> ids) {
        return contextValidator.getValidIdsIncludeOrphans(resourceHelper.getEntityDefinitionByType(toType), ids, true);
    }


    /**
    * NeutralCriteria filter.  Keep NeutralCriteria only on the White List.
    *
    * @param apiQuery - API query
    */
   public void filterCriteria(ApiQuery apiQuery) {

      // keep only whitelist parameters
      List<NeutralCriteria> criterias = apiQuery.getCriteria();
      if (criterias != null) {

         // set doFilter true if "q" is in the list of NetralCriteria
         boolean doFilter = false;
         List<NeutralCriteria> removalList = new ArrayList<NeutralCriteria>();
         for (NeutralCriteria criteria : criterias) {
            if (!WHITE_LIST_PARAMETERS.contains(criteria.getKey())) {
               removalList.add(criteria);

            } else if ("q".equals(criteria.getKey())) {
               doFilter = true;
               applyDefaultPattern(criteria);
            }
         }
         if (doFilter) {
            criterias.removeAll(removalList);
         }
      }
   }

   /**
    * Apply default query pattern for ElasticSearch. Query strategy -
    * start-of-word match on each query token
    *
    * @param criteria
    */
   private static void applyDefaultPattern(NeutralCriteria criteria) {
      String queryString = ((String) criteria.getValue()).trim().toLowerCase();

      // filter rule:
      // first, token must be at least 1 tokens

      // if double-quotes string has been passed, we want to search exactly as-is

      NeutralCriteria.SearchType qType = criteria.getType();

      if (queryString.startsWith("\"") && queryString.endsWith("\"")) {
         qType = NeutralCriteria.SearchType.EXACT;
         queryString = queryString.replaceAll("\"", "");
      }

      if (queryString.matches(".*\\d.*")) {
         qType = (qType == NeutralCriteria.SearchType.EXACT) ? NeutralCriteria.SearchType.EXACT_NUMERIC
               : NeutralCriteria.SearchType.NUMERIC;
      }

      String useValue = queryString;
      if (qType != NeutralCriteria.SearchType.EXACT && qType != NeutralCriteria.SearchType.EXACT_NUMERIC) {
         String[] tokens = queryString.split("\\s+");
         if (tokens == null || tokens.length < 1 || queryString.length() < 1) {
            throw new HttpClientErrorException(HttpStatus.REQUEST_ENTITY_TOO_LARGE);
         }
         // append wildcard '*' to each token
         useValue = StringUtils.join(tokens, "* ");
      }

      criteria.setValue(useValue + "*");
      criteria.setType(qType);
   }

   /**
    * Add security context criteria to query. The security context is
    * determined by the user's accessible schools as well as orphaned entities. The list of accessible
    * school ids is added to the query, and records in Elasticsearch must match
    * an id in order to be returned.
    *
    * @param apiQuery
    */
   private void addSecurityContext(ApiQuery apiQuery) {
      SLIPrincipal principal = (SLIPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Entity principalEntity = principal.getEntity();
      // get schools for user
      List<String> schoolIds = new ArrayList<String>();
      schoolIds.addAll(edOrgHelper.getUserEdOrgs(principalEntity));
      // a special marker for global entities
      schoolIds.add("ALL");
       NeutralQuery schoolContextQuery = new NeutralQuery(new NeutralCriteria(CONTEXT_SCHOOL_ID, NeutralCriteria.CRITERIA_IN, new ArrayList<String>(
               schoolIds)));

       NeutralQuery orphanedQuery = new NeutralQuery();
      orphanedQuery.addCriteria(new NeutralCriteria(ORPHANED_METADATA, NeutralCriteria.OPERATOR_EQUAL, "true"));
      orphanedQuery.addCriteria(new NeutralCriteria(CREATEDBY_METADATA, NeutralCriteria.OPERATOR_EQUAL, principalEntity.getEntityId()));

      apiQuery.addOrQuery(schoolContextQuery);
      apiQuery.addOrQuery(orphanedQuery);
   }

//   /**
//    * Run an embedded ElasticSearch instance, if enabled by configuration.
//    *
//    * @author dwu
//    */
//   @Component
//   static final class Embedded {
//      private static final String ES_DIR = "es";
//      private Node node;
//
//      @Value(value = "${sli.search.embedded:false}")
//      private boolean embeddedEnabled;
//
//      @Value("${sli.search.embedded.http.port:9200}")
//      private int elasticSearchHttpPort;
//
//      @PostConstruct
//      public void init() {
//         if (embeddedEnabled) {
//            LOG.info("Starting embedded ElasticSearch node");
//
//            String tmpDir = System.getProperty("java.io.tmpdir");
//            File elasticsearchDir = new File(tmpDir, ES_DIR);
//            LOG.debug(String.format("ES data tmp dir is %s", elasticsearchDir.getAbsolutePath()));
//
//            if (elasticsearchDir.exists()) {
//               deleteDirectory(elasticsearchDir);
//            }
//
//            Settings settings = ImmutableSettings.settingsBuilder().put("node.http.enabled", true)
//                  .put("http.port", this.elasticSearchHttpPort)
//                  .put("path.data", elasticsearchDir.getAbsolutePath() + "/data").put("gateway.type", "none")
//                        // .put("index.store.type", "memory")
//                  .put("index.number_of_shards", 1).put("index.number_of_replicas", 1).build();
//
//            node = NodeBuilder.nodeBuilder().local(true).settings(settings).node();
//         }
//      }
//
//      public Client getClient() {
//         return node.client();
//      }
//
//      @PreDestroy
//      public void destroy() {
//         if (embeddedEnabled && node != null) {
//            node.close();
//         }
//      }
//
//      private void deleteDirectory(File folder) {
//         try {
//            FileUtils.forceDelete(folder);
//            if (folder.exists()) {
//               LOG.warn("Unable to delete data directory for embedded elasticsearch");
//            }
//         } catch (Exception e) {
//            LOG.error("Unable to delete data directory for embedded elasticsearch", e);
//         }
//      }
//   }

   /**
    * This method will apply the date filter for schoolYears query param.
    * Assumption: the entities for which the criteria is valid have the end date i.e endDate $exists check is ignored
    *
    * @param entities
    * @param apiQuery
    */
   private void addGranularAccessCriteria(String entities, ApiQuery apiQuery) {

      if (granularAccessFilterProvider.hasFilter()) {
         GranularAccessFilter filter = granularAccessFilterProvider.getFilter();

         if (entities.contains(filter.getEntityName())) {

            if (filter.isNoSessionsFoundForSchoolYear()) {
               //throw new NoGranularAccessDatesException();
            }

            NeutralQuery dateQuery = filter.getNeutralQuery();
            for (NeutralCriteria criteria : dateQuery.getCriteria()) {
               apiQuery.addCriteria(criteria);
            }
            for (NeutralQuery dateOrQuery : dateQuery.getOrQueries()) {
               for (NeutralCriteria dateOrCriteria : dateOrQuery.getCriteria()) {
                  if (ParameterConstants.SCHOOL_YEARS.equals(dateOrCriteria.getKey())) {
                     continue;
                  } else {
                     apiQuery.addCriteria(dateOrCriteria);
                  }
               }
            }
         }
      }
   }

   public void setMaxUnfilteredSearchResultCount(int maxUnfilteredSearchResultCount) {
      this.maxUnfilteredSearchResultCount = maxUnfilteredSearchResultCount;
   }

   public void setMaxFilteredSearchResultCount(int maxFilteredSearchResultCount) {
      this.maxFilteredSearchResultCount = maxFilteredSearchResultCount;
   }

   public void setMaxFilteredSearchResultOverrideCount(int maxFilteredSearchResultOverrideCount) {
      this.maxFilteredSearchResultOverrideCount = maxFilteredSearchResultOverrideCount;
   }

}