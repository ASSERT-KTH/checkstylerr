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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
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
import org.slc.sli.bulk.extract.util.LocalEdOrgExtractHelper;
import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.common.util.tenantdb.TenantContext;
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
 * This class should be concerned about how to generate the delta files per LEA per app
 *
 * It gets an iterator of deltas, and determine which app / LEA would need this delta
 * entity. It does not care about how to retrieve the deltas nor how the delta files
 * are generated.
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
    
    @Value("${sli.bulk.extract.output.directory:extract}")
    private String baseDirectory;

    private Map<String, ExtractFile> appPerLeaExtractFiles = new HashMap<String, ExtractFile>();
    private Map<String, EntityExtractor.CollectionWrittenRecord> appPerLeaCollectionRecords = new HashMap<String, EntityExtractor.CollectionWrittenRecord>();

    public void execute(String tenant, DateTime deltaUptoTime, String baseDirectory) {
        TenantContext.setTenantId(tenant);
        Map<String, Set<String>> appsPerTopLEA = reverse(filter(helper.getBulkExtractLEAsPerApp()));
        deltaEntityIterator.init(tenant, deltaUptoTime);
        while (deltaEntityIterator.hasNext()) {
            DeltaRecord delta = deltaEntityIterator.next();
            if (delta.getOp() == Operation.UPDATE) {
                if (delta.isSpamDelete()) {
                    spamDeletes(delta, delta.getBelongsToLEA(), tenant, deltaUptoTime, appsPerTopLEA);
                }
                for (String lea : delta.getBelongsToLEA()) {
                    // we have apps for this lea
                    if (appsPerTopLEA.containsKey(lea)) {
                        for (String appId : appsPerTopLEA.get(lea)) {
                            ExtractFile extractFile = getExtractFile(appId, lea, tenant, deltaUptoTime);
                            EntityExtractor.CollectionWrittenRecord record = getCollectionRecord(appId, lea, delta.getType());
                            try {
                                entityExtractor.write(delta.getEntity(), extractFile, record);
                            } catch (IOException e) {
                                LOG.error("Error while extracting for " + lea + "with app " + appId, e);
                            }
                        }
                    }
                }
            } else if (delta.getOp() == Operation.DELETE) {
                spamDeletes(delta, Collections.<String> emptySet(), tenant, deltaUptoTime, appsPerTopLEA);
            }
        }

        finalizeExtraction(tenant, deltaUptoTime);
    }


    private void spamDeletes(DeltaRecord delta, Set<String> exceptions, String tenant, DateTime deltaUptoTime, Map<String, Set<String>> appsPerLEA) {
        for (Map.Entry<String, Set<String>> entry : appsPerLEA.entrySet()) {
            String lea = entry.getKey();
            Set<String> apps = entry.getValue();

            if (exceptions.contains(lea)) {
                continue;
            }

            for (String appId : apps) {
                ExtractFile extractFile = getExtractFile(appId, lea, tenant, deltaUptoTime);
                // for some entities we have to spam delete the same id in two collections
                // since we cannot reliably retrieve the "type". For example, teacher/staff
                // edorg/school, if the entity has been deleted, all we know if it a staff
                // or edorg, but it may be stored as teacher or school in vendor db, so
                // we must spam delete the id in both teacher/staff or edorg/school collection
                Entity entity = delta.getEntity();
                Set<String> types = typeResolver.resolveType(entity.getType());
                for (String type : types) {
                    Entity e = new MongoEntity(type, entity.getEntityId(), new HashMap<String, Object>(), null);
                    entityWriteManager.writeDelete(e, extractFile);
                }
            }
        }
    }

    // finalize the extraction, if any error occured, do not wipe the delta collections so we could
    // rerun it if we decided to
    private void finalizeExtraction(String tenant, DateTime startTime) {
        boolean allSuccessful = true;
        for (ExtractFile extractFile : appPerLeaExtractFiles.values()) {
            extractFile.closeWriters();
            boolean success = extractFile.finalizeExtraction(startTime);

            if (success) {
                for (Entry<String, File> archiveFile : extractFile.getArchiveFiles().entrySet()) {
                    bulkExtractMongoDA.updateDBRecord(tenant, archiveFile.getValue().getAbsolutePath(),
                        archiveFile.getKey(), startTime.toDate(), true, extractFile.getEdorg(), false);
                }
            }
            allSuccessful &= success;
        }

        if (allSuccessful) {
            // delta files are generated successfully, we can safely remove those deltas now
            LOG.info("Delta generation succeed.  Clearing delta collections for any entities before: " + startTime);
            deltaEntityIterator.removeAllDeltas(tenant, startTime);
        }
    }

    private CollectionWrittenRecord getCollectionRecord(String appId, String lea, String type) {
        String key = appId + "_" + lea + "_" + type;
        if (!appPerLeaCollectionRecords.containsKey(key)) {
            EntityExtractor.CollectionWrittenRecord collectionRecord = new EntityExtractor.CollectionWrittenRecord(type);
            appPerLeaCollectionRecords.put(key, collectionRecord);
            return collectionRecord;
        }

        return appPerLeaCollectionRecords.get(key);
    }

    private ExtractFile getExtractFile(String appId, String lea, String tenant, DateTime deltaUptoTime) {
        String key = appId + "_" + lea;
        if (!appPerLeaExtractFiles.containsKey(key)) {
            ExtractFile appPerLeaExtractFile = getExtractFilePerAppPerLEA(tenant, appId, lea, deltaUptoTime, true);
            appPerLeaExtractFiles.put(key, appPerLeaExtractFile);
        }

        return appPerLeaExtractFiles.get(key);
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
     * Given the tenant, appId, the LEA id been extracted and a timestamp,
     * give me an extractFile for this combo
     * 
     * @param
     */
    public ExtractFile getExtractFilePerAppPerLEA(String tenant, String appId, String edorg, DateTime startTime,
            boolean isDelta) {
        ExtractFile extractFile = new ExtractFile(getAppSpecificDirectory(tenant, appId), getArchiveName(edorg,
                startTime.toDate(), isDelta), bulkExtractMongoDA.getClientIdAndPublicKey(appId, Arrays.asList(edorg)));
        extractFile.setEdorg(edorg);
        return extractFile;
    }
    
    private String getArchiveName(String edorg, Date startTime, boolean isDelta) {
        return edorg + "-" + Launcher.getTimeStamp(startTime) + (isDelta ? "-delta" : "");
    }

    private File getAppSpecificDirectory(String tenant, String app) {
        String tenantPath = baseDirectory + File.separator + TenantAwareMongoDbFactory.getTenantDatabaseName(tenant);
        File appDirectory = new File(tenantPath, app);
        appDirectory.mkdirs();
        return appDirectory;
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
}
