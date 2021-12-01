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
package org.slc.sli.bulk.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slc.sli.bulk.extract.metadata.MetaData;
import org.slc.sli.bulk.extract.zip.OutstreamZipFile;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.dal.repository.connection.TenantAwareMongoDbFactory;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.MongoEntity;
import org.slc.sli.domain.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;


/**
 * Extractor pulls data from mongo and writes it to file.
 *
 * @author tshewchuk
 *
 */
public class EntityExtractor implements Extractor {

    private static final Logger LOG = LoggerFactory.getLogger(EntityExtractor.class);

    private static final int DEFAULT_EXECUTOR_THREADS = 3;
    private static final String ID_STRING = "id";
    private static final String TYPE_STRING = "entityType";
    private static final String ID = "_id";
    private static final String TYPE = "type";
    private static final String BODY = "body";


    private String baseDirectory;

    private List<String> entities;

    private Map<String, String> queriedEntities;

    private Map<String, List<String>> combinedEntities;

    private ExecutorService executor;

    private int executorThreads = DEFAULT_EXECUTOR_THREADS;

    private boolean runOnStartup = false;

    private List<String> tenants;

    private Repository<Entity> entityRepository;

    private BulkExtractMongoDA bulkExtractMongoDA;

    private MetaData metaData;

    @Override
    public void destroy() {
        executor.shutdown();
    }

    @Override
    public void init(List<String> tenants) throws FileNotFoundException {
        setTenants(tenants);
        init();
    }

    public void init() throws FileNotFoundException {
        createBaseDir();
        // create thread pool to process files
        executor = Executors.newFixedThreadPool(executorThreads);
        if (runOnStartup) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    execute();
                }
            });
        }
    }

    public void createBaseDir() {
        new File(baseDirectory).mkdirs();
    }

    @Override
    public void execute() {
        Future<File> call;
        List<Future<File>> futures = new LinkedList<Future<File>>();
        for (String tenant : tenants) {
            try {
                OutstreamZipFile zipFile =new OutstreamZipFile(getTenantDirectory(tenant), tenant);
                call = executor.submit(new ExtractWorker(tenant, zipFile));
                futures.add(call);
            } catch (FileNotFoundException e) {
                LOG.error("Error while extracting data for tenant " + tenant, e);
            } catch (IOException e) {
                LOG.error("Error while extracting data for tenant " + tenant, e);
            }
        }

        // Wait for job to be finished.
        for (Future<File> future : futures) {
            processFuture(future);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.slc.sli.bulk.extract.Extractor#execute()
     */
    @Override
    public void execute(String tenant) {
        // TODO: implement isRunning flag to make sure only one extract is
        // running at a time
        OutstreamZipFile zipFile = null;
        Date startTime = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        String timeStamp = df.format(startTime);
        try {
            zipFile = new OutstreamZipFile(getTenantDirectory(tenant), tenant + "-" + timeStamp);
        } catch (IOException e) {
            LOG.error("Error while extracting data for tenant " + tenant, e);
        }
        TenantContext.setTenantId(tenant);

        for (String entity : entities) {
            extractEntity(tenant, zipFile, entity);
        }

        // Rename temp zip file to permanent.
        try {
            metaData.writeToZip(zipFile, timeStamp);
            zipFile.renameTempZipFile();
            bulkExtractMongoDA.updateDBRecord(tenant, zipFile.getZipFile().getAbsolutePath(), startTime);
        } catch (IOException e) {
            LOG.error("Error attempting to create zipfile " + zipFile.getZipFile().getPath(), e);
        }
    }

    protected void processFuture(Future<File> future) {
        try {
            future.get();
        } catch (Exception e) {
            LOG.error("Error while waiting for extractor job to be finished", e);
        }
    }

    @SuppressWarnings("unchecked")
    public File extractEntity(String tenant, OutstreamZipFile zipFile, String entityName) {

        LOG.info("Extracting " + entityName);

        String collectionName = getCollectionName(entityName);
        Query query = getQuery(entityName);

        try {
            TenantContext.setTenantId(tenant);
            DBCursor cursor = entityRepository.getDBCursor(collectionName, query);

            if (cursor.hasNext()) {
                zipFile.createArchiveEntry(entityName + ".json");
                writeToZip(zipFile, "[");

                while (cursor.hasNext()) {
                    DBObject object = cursor.next();
                    String type = object.get(TYPE).toString();
                    String id = object.get(ID).toString();
                    Map<String, Object> body = (Map<String, Object>) object.get(BODY);
                    Entity record = new MongoEntity(type, id, body, null);

                    // write each record to file
                    addAPIFields(entityName, record);
                    writeToZip(zipFile, JSON.serialize(record.getBody()));

                    if (cursor.hasNext()) {
                        writeToZip(zipFile, ",");
                    }
                }
                writeToZip(zipFile, "]");
            }
            LOG.info("Finished extracting " + entityName);

        } catch (IOException e) {
            LOG.error("Error while extracting " + entityName, e);
        } finally {
            TenantContext.setTenantId(null);
        }
        return zipFile.getZipFile();
    }

    private void writeToZip(OutstreamZipFile zipFile, String data) throws NoSuchElementException, IOException  {
        zipFile.writeData(data);
    }

    private String getCollectionName(String entityName) {
        if (queriedEntities.containsKey(entityName)) {
            return queriedEntities.get(entityName);
        } else {
            return entityName;
        }
    }

    private Query getQuery(String entityName) {
        Query query = new Query();

        if (queriedEntities.containsKey(entityName)) {
            query.addCriteria(Criteria.where("type").is(entityName));
        }

        if (combinedEntities.containsKey(entityName)) {
            query = new Query(Criteria.where("type").in(combinedEntities.get(entityName)));
        }
        return query;
    }

    private void addAPIFields(String archiveName, Entity entity) {
        entity.getBody().put(TYPE_STRING, entity.getType());
        if (combinedEntities.containsKey(archiveName)) {
            entity.getBody().put(ID_STRING, archiveName);
        } else {
            entity.getBody().put(ID_STRING, entity.getEntityId());
        }
    }

    private String getTenantDirectory(String tenant) {

        File tenantDirectory = new File(baseDirectory, TenantAwareMongoDbFactory.getTenantDatabaseName(tenant));
        tenantDirectory.mkdirs();
        return tenantDirectory.getPath();
    }

    public void setExecutorThreads(int executorThreads) {
        this.executorThreads = executorThreads;
    }

    public void setRunOnStartup(boolean runOnStartup) {
        this.runOnStartup = runOnStartup;
    }

    public void setEntityRepository(Repository<Entity> entityRepository) {
        this.entityRepository = entityRepository;
    }

    public void setTenants(List<String> tenants) {
        this.tenants = tenants;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public void setQueriedEntities(Map<String, String> queriedEntities) {
        this.queriedEntities = queriedEntities;
    }

    public void setCombinedEntities(Map<String, List<String>> combinedEntities) {
        this.combinedEntities = combinedEntities;
    }

    public BulkExtractMongoDA getBulkExtractMongoDA() {
        return bulkExtractMongoDA;
    }

    public void setBulkExtractMongoDA(BulkExtractMongoDA bulkExtractMongoDA) {
        this.bulkExtractMongoDA = bulkExtractMongoDA;
    }
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Runnable Thread class to write into file read from Mongo.
     *
     * @author tosako
     *
     */
    private class ExtractWorker implements Callable<File> {

        private final String tenant;

        private final OutstreamZipFile zipFile;

        public ExtractWorker(String tenant, OutstreamZipFile zipFile) throws FileNotFoundException {
            this.tenant = tenant;
            this.zipFile = zipFile;
        }

        @Override
        public File call() throws Exception {
            execute(tenant);
            return zipFile.getZipFile();
        }
    }

    /**
     * @return the metaData
     */
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * @param metaData the metaData to set
     */
    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

}