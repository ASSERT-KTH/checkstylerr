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
package org.slc.sli.bulk.extract.extractor;

import static org.slc.sli.bulk.extract.LogUtil.audit;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slc.sli.bulk.extract.BulkExtractMongoDA;
import org.slc.sli.bulk.extract.Launcher;
import org.slc.sli.bulk.extract.context.resolver.TypeResolver;
import org.slc.sli.bulk.extract.context.resolver.impl.EducationOrganizationContextResolver;
import org.slc.sli.bulk.extract.delta.DeltaEntityIterator;
import org.slc.sli.bulk.extract.delta.DeltaEntityIterator.DeltaRecord;
import org.slc.sli.bulk.extract.delta.DeltaEntityIterator.Operation;
import org.slc.sli.bulk.extract.extractor.EntityExtractor.CollectionWrittenRecord;
import org.slc.sli.bulk.extract.files.EntityWriterManager;
import org.slc.sli.bulk.extract.files.ExtractFile;
import org.slc.sli.bulk.extract.message.BEMessageCode;
import org.slc.sli.bulk.extract.util.LocalEdOrgExtractHelper;
import org.slc.sli.bulk.extract.util.SecurityEventUtil;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.domain.EmbeddedDocumentRelations;
import org.slc.sli.common.util.logging.LogLevelType;
import org.slc.sli.common.util.logging.SecurityEvent;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.dal.repository.DeltaJournal;
import org.slc.sli.dal.repository.connection.TenantAwareMongoDbFactory;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.MongoEntity;
import org.slc.sli.domain.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class should be concerned about how to generate the delta files per LEA
 * per app
 *
 * It gets an iterator of deltas, and determine which app / LEA would need this
 * delta entity. It does not care about how to retrieve the deltas nor how the
 * delta files are generated.
 *
 * @author ycao
 *
 */
@Component
public class DeltaExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DeltaExtractor.class);

    @Autowired
    DeltaEntityIterator deltaEntityIterator;

    @Autowired
    LocalEdOrgExtractor leaExtractor;

    @Autowired
    LocalEdOrgExtractHelper helper;

    @Autowired
    EntityExtractor entityExtractor;

    @Autowired
    BulkExtractMongoDA bulkExtractMongoDA;

    @Autowired
    EntityWriterManager entityWriteManager;

    @Autowired
    TypeResolver typeResolver;

    @Autowired
    EducationOrganizationContextResolver edorgContextResolver;

    @Autowired
    @Qualifier("secondaryRepo")
    Repository<Entity> repo;

    @Autowired
    private SecurityEventUtil securityEventUtil;

    Set<String> subdocs = EmbeddedDocumentRelations.getSubDocuments();

    @Value("${sli.bulk.extract.output.directory:extract}")
    private String baseDirectory;

    private Map<String, ExtractFile> appPerLeaExtractFiles = new HashMap<String, ExtractFile>();
    private Map<String, EntityExtractor.CollectionWrittenRecord> appPerLeaCollectionRecords = new HashMap<String, EntityExtractor.CollectionWrittenRecord>();

    public static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public void execute(String tenant, DateTime deltaUptoTime, String baseDirectory) {

        TenantContext.setTenantId(tenant);

        audit(securityEventUtil.createSecurityEvent(this.getClass().getName(),
                "Delta Extract Initiation", LogLevelType.TYPE_INFO,
                BEMessageCode.BE_SE_CODE_0019, DATE_TIME_FORMATTER.print(deltaUptoTime)));

        Map<String, Set<String>> appsPerTopLEA = reverse(filter(helper.getBulkExtractLEAsPerApp()));
        deltaEntityIterator.init(tenant, deltaUptoTime);
        while (deltaEntityIterator.hasNext()) {
            DeltaRecord delta = deltaEntityIterator.next();
            if (delta.getOp() == Operation.UPDATE) {
                if (delta.isSpamDelete()) {
                    spamDeletes(delta, delta.getBelongsToLEA(), tenant, deltaUptoTime,
                            appsPerTopLEA);
                }
                for (String lea : delta.getBelongsToLEA()) {
                    // we have apps for this lea
                    if (appsPerTopLEA.containsKey(lea)) {
                        ExtractFile extractFile = getExtractFile(lea, tenant, deltaUptoTime,
                                appsPerTopLEA.get(lea));
                        EntityExtractor.CollectionWrittenRecord record = getCollectionRecord(lea,
                                delta.getType());
                        try {
                            entityExtractor.write(delta.getEntity(), extractFile, record, null);
                        } catch (IOException e) {
                            LOG.error("Error while extracting for " + lea, e);
                            SecurityEvent event = securityEventUtil.createSecurityEvent(this.getClass().getName(), "Delta Extract for LEA", LogLevelType.TYPE_ERROR,
                                    BEMessageCode.BE_SE_CODE_0020, delta.getEntity().getType(), lea, e.getMessage());
                            event.setTargetEdOrg(lea);
                            audit(event);
                            throw new RuntimeException("Delta extraction failed, quitting without clearing delta collections...", e);
                        }
                    }
                }
            } else if (delta.getOp() == Operation.DELETE) {
                spamDeletes(delta, Collections.<String> emptySet(), tenant, deltaUptoTime,
                        appsPerTopLEA);
            } else if (delta.getOp() == Operation.PURGE) {
                logPurge(delta, Collections.<String> emptySet(), tenant, deltaUptoTime, appsPerTopLEA);
            }
        }

        logEntityCounts();

        audit(securityEventUtil.createSecurityEvent(this.getClass().getName(),
                "Delta Extract Finished", LogLevelType.TYPE_INFO,
                BEMessageCode.BE_SE_CODE_0021, DATE_TIME_FORMATTER.print(deltaUptoTime)));
        finalizeExtraction(tenant, deltaUptoTime);
    }

    private void logEntityCounts() {
        for (Map.Entry<String, EntityExtractor.CollectionWrittenRecord> entry : appPerLeaCollectionRecords
                .entrySet()) {
            EntityExtractor.CollectionWrittenRecord record = entry.getValue();
            LOG.info(String.format("Processed for %s: %s", entry.getKey(), record.toString()));
        }
    }

    private void spamDeletes(DeltaRecord delta, Set<String> exceptions, String tenant,
            DateTime deltaUptoTime, Map<String, Set<String>> appsPerLEA) {
        for (Map.Entry<String, Set<String>> entry : appsPerLEA.entrySet()) {
            String lea = entry.getKey();

            if (exceptions.contains(lea)) {
                continue;
            }

            ExtractFile extractFile = getExtractFile(lea, tenant, deltaUptoTime, entry.getValue());
            // for some entities we have to spam delete the same id in two
            // collections
            // since we cannot reliably retrieve the "type". For example,
            // teacher/staff
            // edorg/school, if the entity has been deleted, all we know if it a
            // staff
            // or edorg, but it may be stored as teacher or school in vendor db,
            // so
            // we must spam delete the id in both teacher/staff or edorg/school
            // collection
            Entity entity = delta.getEntity();
            Set<String> types = typeResolver.resolveType(entity.getType());
            for (String type : types) {
                // filter out obvious subdocs that don't make sense...
                // a subdoc must have an id that is double the normal id size
                if (!subdocs.contains(type) || entity.getEntityId().length() == lea.length() * 2) {
                    Entity e = new MongoEntity(type, entity.getEntityId(),
                            new HashMap<String, Object>(), null);
                    entityWriteManager.writeDelete(e, extractFile);
                }
            }
        }
    }

    private void logPurge(DeltaRecord delta, Set<String> exceptions, String tenant,
                             DateTime deltaUptoTime, Map<String, Set<String>> appsPerLEA) {
        for (Map.Entry<String, Set<String>> entry : appsPerLEA.entrySet()) {
            String lea = entry.getKey();

            if (exceptions.contains(lea)) {
                continue;
            }

            ExtractFile extractFile = getExtractFile(lea, tenant, deltaUptoTime, entry.getValue());

            DateTime date =  new DateTime((Long)delta.getEntity().getBody().get("t"));

            Entity purgeEntity = new MongoEntity(DeltaJournal.PURGE, null, new HashMap<String, Object>(), null);
            purgeEntity.getBody().put("date", DATE_TIME_FORMATTER.print(date));

            entityWriteManager.writeDelete(purgeEntity, extractFile);
        }
    }

    // finalize the extraction, if any error occured, do not wipe the delta
    // collections so we could
    // rerun it if we decided to
    private void finalizeExtraction(String tenant, DateTime startTime) {
        boolean allSuccessful = true;
        for (ExtractFile extractFile : appPerLeaExtractFiles.values()) {
            extractFile.closeWriters();
            boolean success = extractFile.finalizeExtraction(startTime);

            if (success) {
                for (Entry<String, File> archiveFile : extractFile.getArchiveFiles().entrySet()) {
                    bulkExtractMongoDA.updateDBRecord(tenant, archiveFile.getValue()
                            .getAbsolutePath(), archiveFile.getKey(), startTime.toDate(), true,
                            extractFile.getEdorg(), false);
                }
            }
            allSuccessful &= success;
        }

        if (allSuccessful) {
            // delta files are generated successfully, we can safely remove
            // those deltas now
            LOG.info("Delta generation succeed.  Clearing delta collections for any entities before: "
                    + startTime);
            deltaEntityIterator.removeAllDeltas(tenant, startTime);
        }

    }

    private CollectionWrittenRecord getCollectionRecord(String lea, String type) {
        String key = lea + "|" + type;
        if (!appPerLeaCollectionRecords.containsKey(key)) {
            EntityExtractor.CollectionWrittenRecord collectionRecord = new EntityExtractor.CollectionWrittenRecord(
                    type);
            appPerLeaCollectionRecords.put(key, collectionRecord);
        }

        return appPerLeaCollectionRecords.get(key);
    }

    private ExtractFile getExtractFile(String lea, String tenant, DateTime deltaUptoTime,
            Set<String> appsForLEA) {
        if (!appPerLeaExtractFiles.containsKey(lea)) {
            ExtractFile appPerLeaExtractFile = getExtractFilePerLEA(tenant, lea, deltaUptoTime,
                    appsForLEA);
            appPerLeaExtractFiles.put(lea, appPerLeaExtractFile);
        }

        return appPerLeaExtractFiles.get(lea);
    }

    /* filter out all non top level LEAs */
    private Map<String, Set<String>> filter(Map<String, Set<String>> bulkExtractLEAsPerApp) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry : bulkExtractLEAsPerApp.entrySet()) {
            String app = entry.getKey();
            Set<String> topLEA = new HashSet<String>();
            for (String edorg : entry.getValue()) {
                Entity edorgEntity = repo.findById(EntityNames.EDUCATION_ORGANIZATION, edorg);
                topLEA.addAll(edorgContextResolver.findGoverningLEA(edorgEntity));
            }
            entry.getValue().retainAll(topLEA);
            result.put(app, entry.getValue());
        }

        return result;
    }

    private Map<String, Set<String>> reverse(Map<String, Set<String>> leasPerApp) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry : leasPerApp.entrySet()) {
            for (String lea : entry.getValue()) {
                if (!result.containsKey(lea)) {
                    Set<String> apps = new HashSet<String>();
                    apps.add(entry.getKey());
                    result.put(lea, apps);
                }
                result.get(lea).add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Given the tenant, appId, the LEA id been extracted and a timestamp, give
     * me an extractFile for this combo
     *
     * @param
     */
    public ExtractFile getExtractFilePerLEA(String tenant, String edorg, DateTime startTime,
            Set<String> appsForLEA) {
        List<String> edorgList = Arrays.asList(edorg);
        Map<String, PublicKey> appKeyMap = new HashMap<String, PublicKey>();
        for (String appId : appsForLEA) {
            appKeyMap.putAll(bulkExtractMongoDA.getClientIdAndPublicKey(appId, edorgList));
        }
        ExtractFile extractFile = new ExtractFile(getTenantDirectory(tenant), getArchiveName(edorg,
                startTime.toDate()), appKeyMap, securityEventUtil);
        extractFile.setEdorg(edorg);
        return extractFile;
    }

    private String getArchiveName(String edorg, Date startTime) {
        return edorg + "-" + Launcher.getTimeStamp(startTime) + "-delta";
    }

    private File getTenantDirectory(String tenant) {
        String tenantPath = baseDirectory + File.separator;
        File tenantDirectory = new File(tenantPath,
                TenantAwareMongoDbFactory.getTenantDatabaseName(tenant));
        tenantDirectory.mkdirs();
        return tenantDirectory;
    }

    /**
     * Set base dir.
     *
     * @param baseDirectory
     *            Base directory of all bulk extract processes
     */
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Set securityEventUtil.
     *
     * @param securityEventUtil
     *          securityEventUtil
     */
    public void setSecurityEventUtil(SecurityEventUtil securityEventUtil) {
        this.securityEventUtil = securityEventUtil;
    }
}
