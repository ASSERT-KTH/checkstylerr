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

package org.slc.sli.dal.repository;

import com.mongodb.DBObject;
import org.slc.sli.common.util.datetime.DateTimeUtil;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.common.util.tenantdb.TenantIdToDbName;
import org.slc.sli.common.util.uuid.UUIDGeneratorStrategy;
import org.slc.sli.dal.RetryMongoCommand;
import org.slc.sli.dal.convert.ContainerDocumentAccessor;
import org.slc.sli.dal.convert.Denormalizer;
import org.slc.sli.dal.convert.SubDocAccessor;
import org.slc.sli.dal.convert.SuperdocConverter;
import org.slc.sli.dal.convert.SuperdocConverterRegistry;
import org.slc.sli.dal.encrypt.EntityEncryption;
import org.slc.sli.dal.migration.config.MigrationRunner.MigrateEntity;
import org.slc.sli.dal.migration.config.MigrationRunner.MigrateEntityCollection;
import org.slc.sli.dal.versioning.SliSchemaVersionValidatorProvider;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.EntityMetadataKey;
import org.slc.sli.domain.FullSuperDoc;
import org.slc.sli.domain.MongoEntity;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.validation.EntityValidator;
import org.slc.sli.validation.schema.INaturalKeyExtractor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * mongodb implementation of the entity repository interface that provides basic
 * CRUD and field query methods for entities including core entities and
 * association entities
 *
 * @author Dong Liu dliu@wgen.net
 */

public class MongoEntityRepository extends MongoRepository<Entity> implements InitializingBean,
        ValidationWithoutNaturalKeys {

    @Autowired
    private EntityValidator validator;

    @Autowired(required = false)
    @Qualifier("entityEncryption")
    private EntityEncryption encrypt;

    @Autowired
    @Qualifier("deterministicUUIDGeneratorStrategy")
    private UUIDGeneratorStrategy uuidGeneratorStrategy;

    @Autowired
    private INaturalKeyExtractor naturalKeyExtractor;

    @Autowired
    private SuperdocConverterRegistry converterReg;

    @Autowired
    @Qualifier("entityKeyEncoder")
    private EntityKeyEncoder keyEncoder;

    @Value("${sli.default.mongotemplate.writeConcern}")
    private String writeConcern;

    private SubDocAccessor subDocs;

    private Denormalizer denormalizer;

    private ContainerDocumentAccessor containerDocumentAccessor;

    @Autowired
    protected SliSchemaVersionValidatorProvider schemaVersionValidatorProvider;

    @Override
    public void afterPropertiesSet() {
        setWriteConcern(writeConcern);
        subDocs = new SubDocAccessor(getTemplate(), uuidGeneratorStrategy, naturalKeyExtractor);
        denormalizer = new Denormalizer(getTemplate());
        containerDocumentAccessor = new ContainerDocumentAccessor(uuidGeneratorStrategy, naturalKeyExtractor, template);
    }

    @Override
    public void setReferenceCheck(String referenceCheck) {
        validator.setReferenceCheck(referenceCheck);

    }

    @Override
    protected String getRecordId(Entity entity) {
        return entity.getEntityId();
    }

    @Override
    protected Class<Entity> getRecordClass() {
        return Entity.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Iterable<Entity> findAllAcrossTenants(String collectionName, Query mongoQuery) {
        List<Entity> crossTenantResults = Collections.emptyList();

        guideIfTenantAgnostic("realm");
        List<String> distinctTenantIds = template.getCollection("realm").distinct("body.tenantId");

        String originalTenantId = TenantContext.getTenantId();
        try {
            crossTenantResults = issueQueryToTenantDbs(collectionName, mongoQuery, distinctTenantIds);
        } finally {
            TenantContext.setTenantId(originalTenantId);
        }

        return crossTenantResults;
    }

    private List<Entity> issueQueryToTenantDbs(String collectionName, Query mongoQuery, List<String> distinctTenantIds) {
        List<Entity> crossTenantResults = new ArrayList<Entity>();

        guideIfTenantAgnostic(collectionName);
        for (String tenantId : distinctTenantIds) {
            // escape nasty characters

            String dbName = TenantIdToDbName.convertTenantIdToDbName(tenantId);

            if (isValidDbName(dbName)) {
                TenantContext.setTenantId(tenantId);

                List<Entity> resultsForThisTenant = template.find(mongoQuery, getRecordClass(), collectionName);
                crossTenantResults.addAll(resultsForThisTenant);
            }
        }
        return crossTenantResults;
    }

    private boolean isValidDbName(String tenantId) {
        return tenantId != null && !"sli".equalsIgnoreCase(tenantId) && tenantId.length() > 0
                && tenantId.indexOf(' ') == -1;
    }

    public Entity createWithRetries(final String type, final Map<String, Object> body,
            final Map<String, Object> metaData, final String collectionName, int noOfRetries) {
        RetryMongoCommand rc = new RetryMongoCommand() {

            @Override
            public Object execute() {
                return create(type, body, metaData, collectionName);
            }
        };
        return (Entity) rc.executeOperation(noOfRetries);
    }

    @Override
    public Entity createWithRetries(final String type, final String id, final Map<String, Object> body,
            final Map<String, Object> metaData, final String collectionName, int noOfRetries) {
        RetryMongoCommand rc = new RetryMongoCommand() {

            @Override
            public Object execute() {
                return internalCreate(type, id, body, metaData, collectionName);
            }
        };
        return (Entity) rc.executeOperation(noOfRetries);
    }

    @Override
    public boolean patch(String type, String collectionName, String id, Map<String, Object> newValues) {
        boolean result = false;
        Entity entity = new MongoEntity(type, id, newValues, null);
        validator.validatePresent(entity);
        validator.validateNaturalKeys(entity, false);
        keyEncoder.encodeEntityKey(entity);
        if (subDocs.isSubDoc(collectionName)) {

            // prepare to find desired record to be patched
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(idConverter.toDatabaseId(id)));
            query.addCriteria(createTenantCriteria(collectionName));

            // prepare update operation for record to be patched
            Update update = new Update();
            for (Entry<String, Object> patch : newValues.entrySet()) {
                update.set("body." + patch.getKey(), patch.getValue());
            }
            result = subDocs.subDoc(collectionName).doUpdate(query, update);
        } else if (containerDocumentAccessor.isContainerDocument(type)) {
            result = containerDocumentAccessor.update(type, id, newValues, collectionName);
        } else {
            result = super.patch(type, collectionName, id, newValues);
        }

        if (result && denormalizer.isDenormalizedDoc(collectionName)) {
            Entity updateEntity;
            if (subDocs.isSubDoc(collectionName)) {
                updateEntity = subDocs.subDoc(collectionName).findById(id);
            } else {
                updateEntity = super.findById(collectionName, id);
            }

            Update update = new Update();
            for (Map.Entry<String, Object> patch : newValues.entrySet()) {
                update.set(patch.getKey(), patch.getValue());
            }

            denormalizer.denormalization(collectionName).doUpdate(updateEntity, update);
        }

        return result;
    }

    @Override
    public Entity create(String type, Map<String, Object> body, Map<String, Object> metaData, String collectionName) {
        return internalCreate(type, null, body, metaData, collectionName);
    }

    /*
     * This method should be private, but is used via mockito in the tests, thus
     * it's public. (S. Altmueller)
     */
    Entity internalCreate(String type, String id, Map<String, Object> body, Map<String, Object> origMetaData,
            String collectionName) {
        Assert.notNull(body, "The given entity must not be null!");
        Map<String, Object> metaData = origMetaData == null ? new HashMap<String, Object>() : origMetaData;

        MongoEntity entity = new MongoEntity(type, id, body, metaData);
        validator.validateNaturalKeys(entity, true);
        validator.validate(entity);
        keyEncoder.encodeEntityKey(entity);

        this.addTimestamps(entity);
        this.schemaVersionValidatorProvider.getSliSchemaVersionValidator().insertVersionInformation(entity);

        if (subDocs.isSubDoc(collectionName)) {
            subDocs.subDoc(collectionName).create(entity);

            if (denormalizer.isDenormalizedDoc(collectionName)) {
                denormalizer.denormalization(collectionName).create(entity);
            }

            return entity;
        } else if (containerDocumentAccessor.isContainerDocument(entity.getType())) {
            final String createdId = containerDocumentAccessor.insert(entity);
            if (!createdId.isEmpty()) {
                return new MongoEntity(type, createdId, entity.getBody(), metaData);
            } else {
                return entity;
            }
        } else {
            SuperdocConverter converter = converterReg.getConverter(collectionName);
            if (converter != null) {
                converter.bodyFieldToSubdoc(entity);
            }
            Entity result = super.insert(entity, collectionName);

            if (denormalizer.isDenormalizedDoc(collectionName)) {
                denormalizer.denormalization(collectionName).create(entity);
            }

            return result;
        }
    }

    @Override
    public List<Entity> insert(List<Entity> records, String collectionName) {

        for (Entity entity : records) {
            this.schemaVersionValidatorProvider.getSliSchemaVersionValidator().insertVersionInformation(entity);
        }

        if (subDocs.isSubDoc(collectionName)) {
            subDocs.subDoc(collectionName).insert(records);

            if (denormalizer.isDenormalizedDoc(collectionName)) {
                denormalizer.denormalization(collectionName).insert(records);
            }

            return records;
        } else if (containerDocumentAccessor.isContainerDocument(collectionName)) {
            containerDocumentAccessor.insert(records);
            return records;
        } else {
            List<Entity> persist = new ArrayList<Entity>();

            for (Entity record : records) {

                Entity entity = new MongoEntity(record.getType(), null, record.getBody(), record.getMetaData());
                keyEncoder.encodeEntityKey(entity);
                persist.add(entity);
            }

            List<Entity> results = super.insert(persist, collectionName);

            if (denormalizer.isDenormalizedDoc(collectionName)) {
                denormalizer.denormalization(collectionName).insert(records);
            }
            if (denormalizer.isCached(collectionName)) {
                denormalizer.addToCache(results, collectionName);
            }

            return results;
        }
    }

    @Override
    @MigrateEntity
    public Entity findOne(String collectionName, Query query) {
        if (subDocs.isSubDoc(collectionName) || containerDocumentAccessor.isContainerSubdoc(collectionName)) {
            List<Entity> entities = subDocs.isSubDoc(collectionName) ? subDocs.subDoc(collectionName).findAll(query)
                    : containerDocumentAccessor.findAll(collectionName, query);
            if (entities != null && entities.size() > 0) {
                return entities.get(0);
            }
            return null;
        }
        if (FullSuperDoc.FULL_ENTITIES.containsKey(collectionName)) {
            Set<String> embededFields = FullSuperDoc.FULL_ENTITIES.get(collectionName);
            addEmbededFields(query, embededFields);
        }
        Entity entity = super.findOne(collectionName, query);
        if (entity != null) {
            SuperdocConverter converter = converterReg.getConverter(entity.getType());
            if (converter != null) {
                converter.subdocToBodyField(entity);
            }
        }
        return entity;
    }

    @Override
    public boolean delete(String collectionName, String id) {

        if (subDocs.isSubDoc(collectionName)) {
            Entity entity = subDocs.subDoc(collectionName).findById(id);

            if (denormalizer.isDenormalizedDoc(collectionName)) {
                denormalizer.denormalization(collectionName).delete(entity, id);
            }

            return subDocs.subDoc(collectionName).delete(entity);
        }

        if (containerDocumentAccessor.isContainerSubdoc(collectionName)) {
            Entity entity = containerDocumentAccessor.findById(collectionName, id);
            return containerDocumentAccessor.delete(entity);
        }

        if (denormalizer.isDenormalizedDoc(collectionName)) {
            denormalizer.denormalization(collectionName).delete(null, id);
        }

        return super.delete(collectionName, id);
    }

    @Override
    protected Query getUpdateQuery(Entity entity) {
        String id = getRecordId(entity);
        return new Query(Criteria.where("_id").is(idConverter.toDatabaseId(id)));
    }

    @Override
    protected Entity getEncryptedRecord(Entity entity) {
        MongoEntity encryptedEntity = new MongoEntity(entity.getType(), entity.getEntityId(), entity.getBody(),
                entity.getMetaData(), entity.getCalculatedValues(), entity.getAggregates());
        encryptedEntity.getEmbeddedData().putAll(entity.getEmbeddedData());
        encryptedEntity.encrypt(encrypt);
        return encryptedEntity;
    }

    @Override
    protected Update getUpdateCommand(Entity entity, boolean isSuperdoc) {
        // set up update query
        Map<String, Object> entityBody = entity.getBody();
        Map<String, Object> entityMetaData = entity.getMetaData();
        Update update = new Update();
        update.set("body", entityBody).set("metaData", entityMetaData);
        // superdoc need to update subdoc fields outside body
        if (isSuperdoc && entity.getEmbeddedData() != null) {
            Set<String> subdocFields = FullSuperDoc.FULL_ENTITIES.get(entity.getType());
            if (subdocFields != null) {
                for (String subdocField : subdocFields) {
                    List<Entity> subdocEntities = entity.getEmbeddedData().get(subdocField);
                    if (subdocEntities != null && subdocEntities.size() > 0) {
                        List<Map<String, Object>> updateEntities = new ArrayList<Map<String, Object>>();
                        for (Entity subdocEntity : subdocEntities) {
                            Map<String, Object> updateEntity = new HashMap<String, Object>();
                            updateEntity.put("_id", subdocEntity.getEntityId());
                            updateEntity.put("body", subdocEntity.getBody());
                            updateEntity.put("type", subdocEntity.getType());
                            updateEntity.put("metaData", subdocEntity.getMetaData());
                            updateEntities.add(updateEntity);
                        }
                        update.set(subdocField, updateEntities);
                    } else {
                        update.unset(subdocField);
                    }
                }
            }
        }
        return update;
    }

    @Override
    public boolean updateWithRetries(final String collection, final Entity entity, int noOfRetries) {
        RetryMongoCommand rc = new RetryMongoCommand() {

            @Override
            public Object execute() {
                // updateWithRetries is only used by ingestion, so always set isSuperdoc to false
                return update(collection, entity, false);
            }
        };
        Object result = rc.executeOperation(noOfRetries);
        if (result != null) {
            return (Boolean) result;
        } else {
            return false;
        }

    }

    @Override
    public boolean updateWithoutValidatingNaturalKeys(String collection, Entity entity) {
        return this.update(collection, entity, FullSuperDoc.isFullSuperdoc(entity));
    }

    @Override
    public boolean update(String collection, Entity entity, boolean isSuperdoc) {
        return this.update(collection, entity, true, isSuperdoc);
    }

    private boolean update(String collection, Entity entity, boolean validateNaturalKeys, boolean isSuperdoc) {
        if (validateNaturalKeys) {
            validator.validateNaturalKeys(entity, true);
        }
        validator.validate(entity);

        this.updateTimestamp(entity);

        if (denormalizer.isDenormalizedDoc(collection)) {
            denormalizer.denormalization(collection).create(entity);
        }
        if (subDocs.isSubDoc(collection)) {
            return subDocs.subDoc(collection).create(entity);
        }
        // convert subdoc from superdoc body to outside body
        SuperdocConverter converter = converterReg.getConverter(entity.getType());
        if (converter != null && isSuperdoc) {
            converter.bodyFieldToSubdoc(entity);
        }
        if (containerDocumentAccessor.isContainerDocument(collection)) {
            return !containerDocumentAccessor.update(entity).isEmpty();
        }

        return super.update(collection, entity, null, isSuperdoc); // body);
    }

    /**
     * Add the created and updated timestamp to the document metadata.
     */
    private void addTimestamps(Entity entity) {
        Date now = DateTimeUtil.getNowInUTC();

        Map<String, Object> metaData = entity.getMetaData();
        metaData.put(EntityMetadataKey.CREATED.getKey(), now);
        metaData.put(EntityMetadataKey.UPDATED.getKey(), now);
    }

    /**
     * Update the updated timestamp on the document metadata.
     */
    public void updateTimestamp(Entity entity) {
        Date now = DateTimeUtil.getNowInUTC();
        entity.getMetaData().put(EntityMetadataKey.UPDATED.getKey(), now);
    }

    public void setValidator(EntityValidator validator) {
        this.validator = validator;
    }

    @Override
    @MigrateEntity
    public Entity findById(String collectionName, String id) {
        if (subDocs.isSubDoc(collectionName)) {
            return subDocs.subDoc(collectionName).findById(id);
        } else if(containerDocumentAccessor.isContainerSubdoc(collectionName)) {
            return containerDocumentAccessor.findById(collectionName, id);
        }
        return super.findById(collectionName, id);
    }

    @Override
    public Iterable<String> findAllIds(String collectionName, NeutralQuery neutralQuery) {
        if (subDocs.isSubDoc(collectionName)) {
            NeutralQuery subDocNeutralQuery = neutralQuery == null ? new NeutralQuery() : neutralQuery;
            subDocNeutralQuery.setIncludeFieldString("_id");
            addDefaultQueryParams(subDocNeutralQuery, collectionName);
            Query q = getQueryConverter().convert(collectionName, subDocNeutralQuery);

            List<String> ids = new LinkedList<String>();
            for (Entity e : subDocs.subDoc(collectionName).findAll(q)) {
                ids.add(e.getEntityId());
            }
            return ids;
        }
        return super.findAllIds(collectionName, neutralQuery);
    }

    @Override
    @MigrateEntityCollection
    public Iterable<Entity> findAll(String collectionName, NeutralQuery neutralQuery) {
        if (subDocs.isSubDoc(collectionName)) {
            this.addDefaultQueryParams(neutralQuery, collectionName);
            return subDocs.subDoc(collectionName).findAll(getQueryConverter().convert(collectionName, neutralQuery));
        }
        if (containerDocumentAccessor.isContainerSubdoc(collectionName)) {
            this.addDefaultQueryParams(neutralQuery, collectionName);
            return containerDocumentAccessor.findAll(collectionName, getQueryConverter().convert(collectionName, neutralQuery));
        }
        if (FullSuperDoc.FULL_ENTITIES.containsKey(collectionName)) {
            Set<String> embededFields = FullSuperDoc.FULL_ENTITIES.get(collectionName);
            addEmbededFields(neutralQuery, embededFields);
        }
        Iterable<Entity> entities = super.findAll(collectionName, neutralQuery);
        SuperdocConverter converter = converterReg.getConverter(collectionName);
        if (converter != null) {
            converter.subdocToBodyField(entities);
        }
        return entities;
    }

    @Override
    public boolean exists(String collectionName, String id) {
        if (subDocs.isSubDoc(collectionName)) {
            return subDocs.subDoc(collectionName).exists(id);
        }
        return super.exists(collectionName, id);
    }

    @Override
    public long count(String collectionName, NeutralQuery neutralQuery) {
        if (subDocs.isSubDoc(collectionName)) {
            Query query = this.getQueryConverter().convert(collectionName, neutralQuery);
            return count(collectionName, query);
        }
        return super.count(collectionName, neutralQuery);
    }

    @Override
    public long count(String collectionName, Query query) {
        if (subDocs.isSubDoc(collectionName)) {
            return subDocs.subDoc(collectionName).count(query);
        }
        return super.count(collectionName, query);
    }

    @Override
    public boolean doUpdate(String collectionName, NeutralQuery neutralQuery, Update update) {
        if (subDocs.isSubDoc(collectionName)) {
            Query query = this.getQueryConverter().convert(collectionName, neutralQuery);
            return subDocs.subDoc(collectionName).doUpdate(query, update);
        }
        return super.doUpdate(collectionName, neutralQuery, update);
    }

    @Override
    public void deleteAll(String collectionName, NeutralQuery neutralQuery) {
        if (subDocs.isSubDoc(collectionName)) {
            Query query = this.getQueryConverter().convert(collectionName, neutralQuery);
            subDocs.subDoc(collectionName).deleteAll(query);
        } else {
            super.deleteAll(collectionName, neutralQuery);
        }
    }

    /**
     * @Deprecated "This is a deprecated method that should only be used by the ingestion ID
     *             Normalization code.
     *             It is not tenant-safe meaning clients of this method must include tenantId in the
     *             metaData block"
     */
    @Override
    @Deprecated
    @MigrateEntityCollection
    public Iterable<Entity> findByQuery(String collectionName, Query origQuery, int skip, int max) {
        Query query = origQuery == null ? new Query() : origQuery;

        query.skip(skip).limit(max);

        if (subDocs.isSubDoc(collectionName)) {
            return subDocs.subDoc(collectionName).findAll(query);
        }

        return findByQuery(collectionName, query);
    }

    @Override
    @MigrateEntity
    public Entity findAndUpdate(String collectionName, NeutralQuery neutralQuery, Update update) {
        Query query = this.getQueryConverter().convert(collectionName, neutralQuery);
        FindAndModifyOptions options = new FindAndModifyOptions();
        return template.findAndModify(query, update, options, getRecordClass(), collectionName);
    }

    private Query addEmbededFields(Query query, Set<String> embededFields) {
        if (query == null) {
            return null;
        }
        DBObject fieldObjects = query.getFieldsObject();
        if (fieldObjects != null) {
            for (String embededField : embededFields) {
                if (!fieldObjects.containsField(embededField)) {
                    fieldObjects.put(embededField, 1);
                }
            }
        }
        return query;
    }

    private NeutralQuery addEmbededFields(NeutralQuery query, Set<String> embededFields) {
        if (query == null) {
            return null;
        }
        List<String> fields = new ArrayList<String>();
        fields.addAll(embededFields);
        query.setEmbeddedFields(fields);
        return query;
    }
}
