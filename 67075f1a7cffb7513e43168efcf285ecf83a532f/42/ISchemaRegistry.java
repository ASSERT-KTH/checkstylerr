/**
 * Copyright 2016-2019 Cloudera, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.hortonworks.registries.schemaregistry;

import com.hortonworks.registries.schemaregistry.cache.SchemaRegistryCacheType;
import com.hortonworks.registries.schemaregistry.errors.IncompatibleSchemaException;
import com.hortonworks.registries.schemaregistry.errors.InvalidSchemaException;
import com.hortonworks.registries.schemaregistry.errors.SchemaBranchNotFoundException;
import com.hortonworks.registries.schemaregistry.errors.SchemaNotFoundException;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public interface ISchemaRegistry extends ISchemaRegistryService {

    String SCHEMA_PROVIDERS = "schemaProviders";

    String AUTHORIZATION = "authorization";

    String DEFAULT_SCHEMA_VERSION_MERGE_STRATEGY = "OPTIMISTIC";

    /**
     * initializes it with the given properties
     *
     * @param props properties to be initialized with.
     */
    void init(Map<String, Object> props);

    /**
     * Registers information about a schema if it is not yet and returns it's identifier.
     *
     * @param schemaMetadata     metadata about schema.
     * @param throwErrorIfExists whether to throw an error if it already exists.
     *
     * @return id of the registered schema which is successfully registered now or earlier.
     */
    Long addSchemaMetadata(SchemaMetadata schemaMetadata, boolean throwErrorIfExists);

    /**
     * If there is a version of the schema with the given schemaText for schema name then it returns respective {@link SchemaVersionInfo},
     * else it returns null.
     *
     * @param schemaName name of the schema
     * @param schemaText text of the schema
     *
     * @return SchemaVersionInfo instance about the registered version of schema which is same as the given {@code schemaText}
     *
     * @throws SchemaNotFoundException when no schema metadata registered with the given schema name.
     * @throws InvalidSchemaException  when the given {@code schemaText} is not valid.
     */
     default SchemaVersionInfo getSchemaVersionInfo(String schemaName, String schemaText) throws SchemaNotFoundException, InvalidSchemaException, SchemaBranchNotFoundException {
         return getSchemaVersionInfo(schemaName, schemaText, false);
     }


    /**
     * @param schemaBranchName name of the schema branch
     * @param schemaName name identifying a schema
     *
     * @return latest enabled version of the schema for the given schemaName
     *
     * @throws SchemaNotFoundException if there is no schema metadata registered with the given {@code schemaName}
     */
    SchemaVersionInfo getLatestEnabledSchemaVersionInfo(String schemaBranchName, String schemaName) throws SchemaNotFoundException, SchemaBranchNotFoundException;

    /**
     * If there is a version of the schema with the given schemaText for schema name then it returns respective {@link SchemaVersionInfo},
     * else it returns null.
     *
     * @param schemaName name of the schema
     * @param schemaText text of the schema
     * @param disableCanonicalCheck true if the schema version should be added despite being canonically similar to an existing schema version, else false
     *
     * @return SchemaVersionInfo instance about the registered version of schema which is same as the given {@code schemaText}
     *
     * @throws SchemaNotFoundException when no schema metadata registered with the given schema name.
     * @throws InvalidSchemaException  when the given {@code schemaText} is not valid.
     */
    SchemaVersionInfo getSchemaVersionInfo(String schemaName, String schemaText, boolean disableCanonicalCheck) throws SchemaNotFoundException, InvalidSchemaException, SchemaBranchNotFoundException;

    /**
     * If there is a version of the schema with the given fingerprint then it returns the respective {@link SchemaVersionInfo},
     * else it throws a {@link SchemaNotFoundException}.
     *
     * @param fingerprint hash of the schemaText (hashing function depends on config)
     * @return the {@link SchemaVersionInfo} of the schema version with the given fingerprint.
     * @throws SchemaNotFoundException when no schema version registered with the given fingerprint.
     * @see SchemaProvider#HASH_FUNCTION_CONFIG
     */
    SchemaVersionInfo findSchemaVersionByFingerprint(String fingerprint) throws SchemaNotFoundException;

    /**
     * @param props properties
     *
     * @return Collects aggregated schema metadata which contains the given properties.
     */
    Collection<AggregatedSchemaMetadataInfo> findAggregatedSchemaMetadata(Map<String, String> props) throws SchemaNotFoundException, SchemaBranchNotFoundException;

    /**
     * @param schemaName name of the schema
     *
     * @return {@link AggregatedSchemaMetadataInfo} for the given schema name, null if there is no schema registered with
     * the given schema name.
     */
    AggregatedSchemaMetadataInfo getAggregatedSchemaMetadataInfo(String schemaName) throws SchemaNotFoundException, SchemaBranchNotFoundException;

    /**
     * @param props properties
     *
     * @return All SchemaMetadata having the given properties.
     */
    Collection<SchemaMetadataInfo> findSchemaMetadata(Map<String, String> props);

    /**
     * @param serDesId id
     *
     * @return SerDesInfo for the given serDesId, null if it does not exist.
     */
    SerDesInfo getSerDes(Long serDesId);

    /**
     * Searches the registry to find schemas according to the given {@code whereClause} and orders the results by given {@code orderByFields}
     *
     * @return Collection of schemas from the results of given where clause.
     */
    Collection<SchemaMetadataInfo> searchSchemas(MultivaluedMap<String, String> queryParameters, Optional<String> orderBy);

    /**
     *  Merges a given schema version to 'MASTER' branch with a merge strategy
     * @param schemaVersionId             id of the schema version to be merged
     * @param schemaVersionMergeStrategy  merge strategy to be used for merging to 'MASTER'
     * @return
     * @throws SchemaNotFoundException
     * @throws IncompatibleSchemaException
     */
    default SchemaVersionMergeResult mergeSchemaVersion(Long schemaVersionId, SchemaVersionMergeStrategy schemaVersionMergeStrategy) throws SchemaNotFoundException, IncompatibleSchemaException {
        return mergeSchemaVersion(schemaVersionId, schemaVersionMergeStrategy, false);
    }

    /**
     *  Merges a given schema version to 'MASTER' branch with a merge strategy
     * @param schemaVersionId             id of the schema version to be merged
     * @param schemaVersionMergeStrategy  merge strategy to be used for merging to 'MASTER'
     * @param disableCanonicalCheck true if the schema version should be added despite being canonically similar to an existing schema version, else false
     *
     * @return
     *
     * @throws SchemaNotFoundException
     * @throws IncompatibleSchemaException
     */
    SchemaVersionMergeResult mergeSchemaVersion(Long schemaVersionId, SchemaVersionMergeStrategy schemaVersionMergeStrategy, boolean disableCanonicalCheck) throws IncompatibleSchemaException, SchemaNotFoundException;

    /**
     * @param schemaName name identifying a schema
     *
     * @return all schema branches with versions of the schemas for given schemaName
     *
     * @throws SchemaNotFoundException if there is no schema metadata registered with the given {@code schemaName}
     */
    Collection<AggregatedSchemaBranch> getAggregatedSchemaBranch(String schemaName) throws SchemaNotFoundException, SchemaBranchNotFoundException;

    /**
     * @param schemaBranchId id of the branch
     *
     * @return schema branch with with the given {@code schemaBranchId}
     *
     * @throws SchemaBranchNotFoundException if there is no schema branch registered with the given {@code schemaBranchId}
     */
    SchemaBranch getSchemaBranch(Long schemaBranchId) throws SchemaBranchNotFoundException;

    /**
     * @param vertionId id of the schema version
     *
     * @return Collection schema branches that contain the given {@code vertionId}
     *
     * @throws SchemaBranchNotFoundException if there is no schema version registered with the given {@code vertionId}
     */
    Collection<SchemaBranch> getSchemaBranchesForVersion(Long vertionId) throws SchemaBranchNotFoundException;

    /**
     * Get schema version by its unique ID. If the version does not exist an exception will be thrown.
     * @param id    id of the version
     */
    SchemaVersionInfo fetchSchemaVersionInfo(Long id) throws SchemaNotFoundException;


    /**
     *  Invalidates a cache entry given its cache type and its key, invalidates all entries in all the caches if the cache type is 'ALL'
     *
     * @param schemaRegistryCacheType cache type
     * @param keyAsString serialized version of the key of the cache
     */
    void invalidateCache(SchemaRegistryCacheType schemaRegistryCacheType, String keyAsString);

    class Options {
        // we may want to remove schema.registry prefix from configuration properties as these are all properties
        // given by client.
        public static final String ENABLE_CACHING = "enableCaching";
        public static final String CACHE = "cache";
        public static final String SCHEMA_CACHE_SIZE = "schemaCacheSize";
        public static final String SCHEMA_CACHE_EXPIRY_INTERVAL_SECS = "schemaCacheExpiryInterval";
        public static final int DEFAULT_SCHEMA_CACHE_SIZE = 10000;
        public static final long DEFAULT_SCHEMA_CACHE_EXPIRY_INTERVAL_SECS = 60 * 60L;

        private final Map<String, ?> config;

        public Options(Map<String, ?> config) {
            this.config = (Map<String, ?>) getValue(config, CACHE, new HashMap<>());
        }

        private Object getValue(Map<String, ?> properties, String propertyKey, Object defaultValue) {
            Object value = properties.get(propertyKey);
            return value != null ? value : defaultValue;
        }

        private Object getPropertyValue(String propertyKey, Object defaultValue) {
            Map<String, ?> properties = (Map<String, ?>) getValue(config, "properties", new HashMap<>());
            return getValue(properties, propertyKey, defaultValue);
        }

        public Boolean isCacheEnabled() {
            return (Boolean) getValue(config, ENABLE_CACHING, Boolean.TRUE);
        }

        public int getMaxSchemaCacheSize() {
            return isCacheEnabled() == true ?
                    Integer.parseInt(getPropertyValue(SCHEMA_CACHE_SIZE, DEFAULT_SCHEMA_CACHE_SIZE).toString()) : 0;
        }

        public long getSchemaExpiryInSecs() {
            return Long.valueOf(getPropertyValue(SCHEMA_CACHE_EXPIRY_INTERVAL_SECS, DEFAULT_SCHEMA_CACHE_EXPIRY_INTERVAL_SECS)
                    .toString());
        }
    }

}