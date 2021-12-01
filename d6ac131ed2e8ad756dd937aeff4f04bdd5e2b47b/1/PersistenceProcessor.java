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

package org.slc.sli.ingestion.processors;

import static org.slc.sli.ingestion.util.NeutralRecordUtils.getByPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.MongoException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slc.sli.ingestion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.domain.Entity;
import org.slc.sli.ingestion.dal.NeutralRecordMongoAccess;
import org.slc.sli.ingestion.dal.NeutralRecordReadConverter;
import org.slc.sli.ingestion.delta.SliDeltaManager;
import org.slc.sli.ingestion.handler.AbstractIngestionHandler;
import org.slc.sli.ingestion.model.Error;
import org.slc.sli.ingestion.model.Metrics;
import org.slc.sli.ingestion.model.NewBatchJob;
import org.slc.sli.ingestion.model.RecordHash;
import org.slc.sli.ingestion.model.ResourceEntry;
import org.slc.sli.ingestion.model.Stage;
import org.slc.sli.ingestion.model.da.BatchJobDAO;
import org.slc.sli.ingestion.reporting.AbstractMessageReport;
import org.slc.sli.ingestion.reporting.ReportStats;
import org.slc.sli.ingestion.reporting.impl.AggregatedSource;
import org.slc.sli.ingestion.reporting.impl.CoreMessageCode;
import org.slc.sli.ingestion.reporting.impl.ElementSourceImpl;
import org.slc.sli.ingestion.reporting.impl.ProcessorSource;
import org.slc.sli.ingestion.reporting.impl.SimpleReportStats;
import org.slc.sli.ingestion.transformation.EdFi2SLITransformer;
import org.slc.sli.ingestion.transformation.SimpleEntity;
import org.slc.sli.ingestion.util.BatchJobUtils;
import org.slc.sli.ingestion.util.LogUtil;

/**
 * Ingestion Persistence Processor.
 *
 * Specific Ingestion Persistence Processor which provides specific SLI Ingestion instance
 * persistence behavior.
 * Persists data from Staged Database.
 *
 * @author ifaybyshev
 * @author dduran
 * @author shalka
 */
@Component
public class PersistenceProcessor extends IngestionProcessor<NeutralRecordWorkNote, Resource> implements BatchJobStage {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceProcessor.class);

    public static final BatchJobStageType BATCH_JOB_STAGE = BatchJobStageType.PERSISTENCE_PROCESSOR;

    private static final String BATCH_JOB_STAGE_DESC = "Persists records to sli database";

    private static final String BATCH_JOB_ID = "batchJobId";
    private static final String CREATION_TIME = "creationTime";

    private EdFi2SLITransformer transformer;

    private Map<String, Set<String>> entityPersistTypeMap;

    private AbstractIngestionHandler<SimpleEntity, Entity> entityPersistHandler;

    @Autowired
    private NeutralRecordReadConverter neutralRecordReadConverter;

    @Autowired
    private NeutralRecordMongoAccess neutralRecordMongoAccess;

    @Autowired
    private BatchJobDAO batchJobDAO;

    private Set<String> recordLvlHashNeutralRecordTypes;

    @Autowired
    private AbstractMessageReport databaseMessageReport;

    // Paths for id field and ref fields for self-referencing entities (for DE1950)
    // TODO: make it work for entities with multiple field keys.
    // TODO: make it configurable. From schema, maybe.
    /**
     * represents the configuration of a self-referencing entity schema
     *
     */
    static class SelfRefEntityConfig {
        String idPath;              // path to the id field
        // Exactly one of the following fields can be non-null:
        String parentAttributePath; // if parent reference is stored in attribute, path to
                                    // the parent reference field,
        String localParentIdKey;    // if parent reference is stored in localParentId map, key
                                 // to the parent reference field

        SelfRefEntityConfig(String i, String p, String k) {
            idPath = i;
            parentAttributePath = p;
            localParentIdKey = k;
        }
    }

    public static final Map<String, SelfRefEntityConfig> SELF_REF_ENTITY_CONFIG;
    static {
        HashMap<String, SelfRefEntityConfig> m = new HashMap<String, SelfRefEntityConfig>();
        // localEducationAgency's parent reference is stored in a field an attribute
        m.put("localEducationAgency", new SelfRefEntityConfig("stateOrganizationId", "localEducationAgencyReference",
                null));
        SELF_REF_ENTITY_CONFIG = Collections.unmodifiableMap(m);
    }

    // End Self-referencing entity

    /**
     * Camel Exchange process callback method
     *
     * @param exchange
     *            camel exchange.
     * @param args
     *            neutral record list coming in from delta processor
     */
    @Override
    protected void process(Exchange exchange, IngestionProcessor.ProcessorArgs<NeutralRecordWorkNote> args) {
        NeutralRecordWorkNote workNote = args.workNote;

        if (workNote == null || workNote.getBatchJobId() == null) {
            handleNoBatchJobIdInExchange(exchange);
        } else {
            processPersistence(workNote, exchange);
        }
    }

    /**
     * Process the persistence of the entity specified by the work note.
     *
     * @param workNote
     *            specifies the entity to be persisted.
     * @param exchange
     *            camel exchange.
     */
    private void processPersistence(NeutralRecordWorkNote workNote, Exchange exchange) {
        Stage stage = initializeStage(workNote);

        String batchJobId = workNote.getBatchJobId();
        NewBatchJob newJob = null;
        try {
            newJob = batchJobDAO.findBatchJobById(batchJobId);

            TenantContext.setTenantId(newJob.getTenantId());
            TenantContext.setJobId(batchJobId);

            LOG.debug("processing persistence: {}", newJob);

            processWorkNote(workNote, newJob, stage);

        } catch (Exception exception) {
            handleProcessingExceptions(exception, exchange, batchJobId);
        } finally {
            if (newJob != null) {
                BatchJobUtils.stopStageAndAddToJob(stage, newJob);
                batchJobDAO.saveBatchJobStage(batchJobId, stage);
            }
        }
    }

    /**
     * Initialize the current (persistence) stage.
     *
     * @param workNote
     *            specifies the neutral records to be persisted.
     * @return current (started) stage.
     */
    private Stage initializeStage(NeutralRecordWorkNote workNote) {
        Stage stage = Stage.createAndStartStage(BATCH_JOB_STAGE, BATCH_JOB_STAGE_DESC);
        stage.setProcessingInformation("processing neutral record work note of size " +  workNote.getNeutralRecords().size());
        return stage;
    }

    /**
     * Processes the work note by persisting the entity (with range) specified in the work note.
     *
     * @param workNote
     *            specifies the entity (and range) to be persisted.
     * @param job
     *            current batch job.
     * @param stage
     *            persistence stage.
     */
    private void processWorkNote(NeutralRecordWorkNote workNote, NewBatchJob job, Stage stage) {
        String currentEntityType = null;

        Map<String, Metrics> perFileMetrics = new HashMap<String, Metrics>();
        ReportStats reportStatsForCollection = new SimpleReportStats();
        try {
            ReportStats reportStatsForNrEntity = null;

            Iterable<NeutralRecord> records = null;

            List<NeutralRecord> recordHashStore = new ArrayList<NeutralRecord>();
            List<NeutralRecord> recordStore = new ArrayList<NeutralRecord>();
            List<SimpleEntity> persist = new ArrayList<SimpleEntity>();
            for (NeutralRecord neutralRecord : records) {
                currentEntityType = neutralRecord.getRecordType();

                if (reportStatsForNrEntity == null) {
                    reportStatsForNrEntity = createReportStats(job.getId(), neutralRecord.getSourceFile(),
                            stage.getStageName());
                }
                recordHashStore.add(neutralRecord);

                reportStatsForCollection = createReportStats(job.getId(), neutralRecord.getSourceFile(),
                        stage.getStageName());
                Metrics currentMetric = getOrCreateMetric(perFileMetrics, neutralRecord, workNote);

                SimpleEntity xformedEntity = transformNeutralRecord(neutralRecord, job,
                        reportStatsForCollection);

                if (dbConfirmed(xformedEntity)) {

                    recordStore.add(neutralRecord);

                    // queue up for bulk insert
                    persist.add(xformedEntity);

                } else {
                    currentMetric.setErrorCount(currentMetric.getErrorCount() + 1);
                }
                currentMetric.setRecordCount(currentMetric.getRecordCount() + 1);

                perFileMetrics.put(currentMetric.getResourceId(), currentMetric);
            }

            try {
                if (persist.size() > 0) {
                    List<Entity> failed = entityPersistHandler.handle(persist, databaseMessageReport,
                            reportStatsForNrEntity);
                    for (Entity entity : failed) {
                        NeutralRecord record = recordStore.get(persist.indexOf(entity));
                        Metrics currentMetric = getOrCreateMetric(perFileMetrics, record, workNote);
                        currentMetric.setErrorCount(currentMetric.getErrorCount() + 1);

                        // TODO report partial deletions on cascade delete error

                        if (recordHashStore.contains(record)) {
                            recordHashStore.remove(record);
                        }
                    }

                    for (SimpleEntity entity : subtract(persist, failed)) {
                        if (entity.getAction().doDelete()) {
                            NeutralRecord record = recordStore.get(persist.indexOf(entity));
                            Metrics currentMetric = getOrCreateMetric(perFileMetrics, record, workNote);
                            currentMetric.setDeletedCount(currentMetric.getDeletedCount() + 1);
                            // TODO report child delete counts separately if needed when cascade delete occurs
                            currentMetric.setDeletedChildCount(currentMetric.getDeletedChildCount() + Long.parseLong(entity.getDeleteAffectedCount()) - 1);
                        }
                    }
                }
                for (NeutralRecord neutralRecord2 : recordHashStore) {
                    upsertRecordHash(neutralRecord2);

                }

            } catch (DataAccessResourceFailureException darfe) {
                LOG.error("Exception processing record with entityPersistentHandler", darfe);
            }
        } catch (Exception e) {
            databaseMessageReport.error(reportStatsForCollection, new ProcessorSource(currentEntityType),
                    CoreMessageCode.CORE_0005, currentEntityType);
            LogUtil.error(LOG, "Exception when attempting to ingest NeutralRecords in: " + currentEntityType, e);
        } finally {
            Iterator<Metrics> it = perFileMetrics.values().iterator();
            while (it.hasNext()) {
                Metrics m = it.next();
                stage.getMetrics().add(m);
            }
        }
    }

    /**
     *
     * If record was successfully matched against db and if we should proceed with operation
     */
    private boolean dbConfirmed(SimpleEntity e) {

        if (e == null) {
            return false;
        }


        if (e.getAction().doDelete() && e.getEntityId() == null &&
                e.getUUID() == null) {
            return false;
        }

        return true;
    }

    private SimpleEntity transformNeutralRecord(NeutralRecord record, NewBatchJob job, ReportStats reportStats) {
        LOG.debug("processing transformable neutral record of type: {}", record.getRecordType());

        String tenantId = job.getTenantId();
        record.setRecordType(record.getRecordType().replaceFirst("_transformed", ""));
        record.setSourceId(tenantId);

        transformer.setBatchJobId(job.getId());
        List<SimpleEntity> transformed = transformer.handle(record, databaseMessageReport, reportStats);

        if (transformed == null || transformed.isEmpty()) {
            databaseMessageReport.error(reportStats, new ElementSourceImpl(record), CoreMessageCode.CORE_0004,
                    record.getRecordType());
            return null;
        }
        transformed.get(0).setSourceFile(record.getSourceFile());
        return transformed.get(0);
    }

    /**
     * Creates metrics for persistence of work note.
     *
     * @param perFileMetrics
     *            current metrics on a per file basis.
     * @param neutralRecord
     *            neutral record to be persisted.
     * @param workNote
     *            work note specifying entities to be persisted.
     * @return
     */
    private static Metrics getOrCreateMetric(Map<String, Metrics> perFileMetrics, NeutralRecord neutralRecord,
                                             NeutralRecordWorkNote workNote) {

        String sourceFile = neutralRecord.getSourceFile();
        if (sourceFile == null) {
            sourceFile = "unknown_" + neutralRecord.getRecordType() + "_file";
        }

        Metrics currentMetric = perFileMetrics.get(sourceFile);
        if (currentMetric == null) {
            // establish new metrics
            currentMetric = Metrics.newInstance(sourceFile);
        }
        return currentMetric;
    }

    /**
     * Creates an error source for the specified batch job id and resource id.
     *
     * @param batchJobId
     *            current batch job.
     * @param resourceId
     *            current resource id.
     * @return database logging error report.
     */
    private static ReportStats createReportStats(String batchJobId, String resourceId, String stageName) {
        return new SimpleReportStats();
    }

    private static String getCollectionToPersistFrom(String collectionNameAsStaged,
            EntityPipelineType entityPipelineType) {
        String collectionToPersistFrom = collectionNameAsStaged;
        if (entityPipelineType == EntityPipelineType.TRANSFORMED) {
            collectionToPersistFrom = collectionNameAsStaged + "_transformed";
        }
        return collectionToPersistFrom;
    }

    private EntityPipelineType getEntityPipelineType(String collectionName) {
        EntityPipelineType entityPipelineType = EntityPipelineType.NONE;
        if (entityPersistTypeMap.get("passthroughEntities").contains(collectionName)) {
            entityPipelineType = EntityPipelineType.PASSTHROUGH;
        } else if (entityPersistTypeMap.get("transformedEntities").contains(collectionName)) {
            entityPipelineType = EntityPipelineType.TRANSFORMED;
        }
        return entityPipelineType;
    }

    /**
     * Handles the absence of a batch job id in the camel exchange.
     *
     * @param exchange
     *            camel exchange.
     */
    private void handleNoBatchJobIdInExchange(Exchange exchange) {
        LOG.error("Error:", "No BatchJobId specified in " + this.getClass().getName() + " exchange message header.");
    }

    /**
     * Handles the existence of any processing exceptions in the exchange.
     *
     * @param exception
     *            processing exception in camel exchange.
     * @param exchange
     *            camel exchange.
     * @param batchJobId
     *            current batch job id.
     */
    private void handleProcessingExceptions(Exception exception, Exchange exchange, String batchJobId) {
        LogUtil.error(LOG, "Error persisting batch job " + batchJobId, exception);

        Error error = Error.createIngestionError(batchJobId, null, BATCH_JOB_STAGE.getName(), null, null, null,
                FaultType.TYPE_ERROR.getName(), "Exception", exception.getMessage());
        batchJobDAO.saveError(error);
    }

    public Map<String, Set<String>> getEntityPersistTypeMap() {
        return entityPersistTypeMap;
    }

    public void setEntityPersistTypeMap(Map<String, Set<String>> entityPersistTypeMap) {
        this.entityPersistTypeMap = entityPersistTypeMap;
    }

    public void setTransformer(EdFi2SLITransformer transformer) {
        this.transformer = transformer;
    }

    public void setDefaultEntityPersistHandler(
            AbstractIngestionHandler<SimpleEntity, Entity> defaultEntityPersistHandler) {
        this.entityPersistHandler = defaultEntityPersistHandler;
    }

    public NeutralRecordReadConverter getNeutralRecordReadConverter() {
        return neutralRecordReadConverter;
    }

    public void setNeutralRecordReadConverter(NeutralRecordReadConverter neutralRecordReadConverter) {
        this.neutralRecordReadConverter = neutralRecordReadConverter;
    }

    public void setRecordLvlHashNeutralRecordTypes(Set<String> recordLvlHashNeutralRecordTypes) {
        this.recordLvlHashNeutralRecordTypes = recordLvlHashNeutralRecordTypes;
    }

    public Iterable<NeutralRecord> queryBatchFromDb(String collectionName, String jobId, RangedWorkNote workNote) {
        Criteria batchJob = Criteria.where(BATCH_JOB_ID).is(jobId);
        @SuppressWarnings("boxing")
        Criteria limiter = Criteria.where(CREATION_TIME).gte(workNote.getRangeMinimum()).lt(workNote.getRangeMaximum());

        Query query = new Query().limit(0);
        query.addCriteria(batchJob);
        query.addCriteria(limiter);

        return neutralRecordMongoAccess.getRecordRepository().findAllByQuery(collectionName, query);
    }

    private static enum EntityPipelineType {
        PASSTHROUGH, TRANSFORMED, NONE;
    }

    private static Collection<SimpleEntity> subtract(Collection<SimpleEntity> a, List<Entity> failed) {
        Collection<SimpleEntity> result = new ArrayList<SimpleEntity>(a);
        result.removeAll(failed);
        return result;
    }

    @SuppressWarnings({ "unchecked" })
    void upsertRecordHash(NeutralRecord nr) throws DataAccessResourceFailureException {

        /*
         * metaData: {
         * ...
         * "rhData": [ {"rhId": <blahId0>, "rhHash": <blahHash0>}, {"rhId": <blahId1>, "rhHash":
         * <blahHash1>}, ... ],
         * "rhTenantId": <tenantId>
         * }
         */
        if (!recordLvlHashNeutralRecordTypes.contains(nr.getRecordType())) {
            return;
        }

        Object rhDataObj = nr.getMetaDataByName(SliDeltaManager.RECORDHASH_DATA);

 /*
        if ( nr.getActionVerb().doDelete() ) {
            return;
        }
*/
        // Make sure complete metadata is present
        if (null == rhDataObj) {
            return;
        }

        List<Map<String, Object>> rhData = (List<Map<String, Object>>) rhDataObj;

        for (Map<String, Object> rhDataElement : rhData) {

            String newHashValue = (String) rhDataElement.get(SliDeltaManager.RECORDHASH_HASH);
            String recordId = (String) rhDataElement.get(SliDeltaManager.RECORDHASH_ID);
            Map<String, Object> rhCurrentHash = (Map<String, Object>) rhDataElement
                    .get(SliDeltaManager.RECORDHASH_CURRENT);

            // Make sure complete metadata is present
            if ((null == recordId || null == newHashValue) || recordId.isEmpty() || newHashValue.isEmpty()) {
                continue;
            }

            // Consider DE2002, removing a query per record vs. tracking version
            // RecordHash rh = batchJobDAO.findRecordHash(tenantId, recordId);
            if (rhCurrentHash == null) {
                if( nr.getActionVerb().doDelete()) {
                    return;
                }
                batchJobDAO.insertRecordHash(recordId, newHashValue);
            } else {
                RecordHash rh = new RecordHash();
                rh.importFromSerializableMap(rhCurrentHash);
                if( nr.getActionVerb().doDelete()) {
                    batchJobDAO.removeRecordHash( rh );
                } else {
                    batchJobDAO.updateRecordHash(rh, newHashValue);
                }
            }
        }

    }

    public void setBatchJobDAO(BatchJobDAO batchJobDAO) {
        this.batchJobDAO = batchJobDAO;
    }

    public BatchJobDAO getBatchJobDAO() {
        return this.batchJobDAO;
    }

    @Override
    public String getStageName() {
        return BATCH_JOB_STAGE.getName();
    }

    @Override
    protected BatchJobStageType getStage() {
        return BATCH_JOB_STAGE;
    }

    @Override
    protected String getStageDescription() {
        return BATCH_JOB_STAGE_DESC;
    }

}
