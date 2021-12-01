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
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.slc.sli.bulk.extract.BulkExtractMongoDA;
import org.slc.sli.bulk.extract.Launcher;
import org.slc.sli.bulk.extract.files.ExtractFile;
import org.slc.sli.bulk.extract.lea.CourseExtractor;
import org.slc.sli.bulk.extract.lea.CourseOfferingExtractor;
import org.slc.sli.bulk.extract.lea.CourseTranscriptExtractor;
import org.slc.sli.bulk.extract.lea.EdorgExtractor;
import org.slc.sli.bulk.extract.lea.EntityExtract;
import org.slc.sli.bulk.extract.lea.EntityToLeaCache;
import org.slc.sli.bulk.extract.lea.LEAExtractFileMap;
import org.slc.sli.bulk.extract.lea.LEAExtractorFactory;
import org.slc.sli.bulk.extract.lea.SectionExtractor;
import org.slc.sli.bulk.extract.lea.SessionExtractor;
import org.slc.sli.bulk.extract.lea.StaffEdorgAssignmentExtractor;
import org.slc.sli.bulk.extract.lea.StudentExtractor;
import org.slc.sli.bulk.extract.lea.YearlyTranscriptExtractor;
import org.slc.sli.bulk.extract.message.BEMessageCode;
import org.slc.sli.bulk.extract.util.LocalEdOrgExtractHelper;
import org.slc.sli.bulk.extract.util.SecurityEventUtil;
import org.slc.sli.common.util.logging.LogLevelType;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.Repository;

/**
 * Creates local ed org tarballs
 */
public class LocalEdOrgExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(LocalEdOrgExtractor.class);
    private Repository<Entity> repository;

    @Autowired
    private LocalEdOrgExtractHelper helper;

    private LEAExtractFileMap leaToExtractFileMap;
    private EntityExtractor entityExtractor;
    private Map<String, String> entitiesToCollections;
    private BulkExtractMongoDA bulkExtractMongoDA;
    private LEAExtractorFactory factory;

    @Autowired
    private SecurityEventUtil securityEventUtil;

    private File tenantDirectory;
    private DateTime startTime;

    /**
     * Creates unencrypted LEA bulk extract files if any are needed for the given tenant
     *
     * @param tenant
     *            name of tenant to extract
     */
    public void execute(String tenant, File tenantDirectory, DateTime startTime) {

        // 1. SETUP
        TenantContext.setTenantId(tenant);
        this.tenantDirectory = tenantDirectory;
        this.startTime = startTime;

        audit(securityEventUtil.createSecurityEvent(this.getClass().getName(), "LEA level extract initiated", LogLevelType.TYPE_INFO, BEMessageCode.BE_SE_CODE_0008));

        if (factory == null) {
            factory = new LEAExtractorFactory();
        }
        if (leaToExtractFileMap == null) {
            leaToExtractFileMap = new LEAExtractFileMap(buildLEAToExtractFile());
        }
        // 2. EXTRACT
        EntityToLeaCache edorgCache = buildEdOrgCache();

        EdorgExtractor edorg = factory.buildEdorgExtractor(entityExtractor, leaToExtractFileMap, helper);
        edorg.extractEntities(edorgCache);

        // Student
        StudentExtractor student = factory.buildStudentExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        student.extractEntities(null);

        EntityExtract genericExtractor = factory.buildAttendanceExtractor(entityExtractor, leaToExtractFileMap,
                repository, student.getEntityCache(), helper);
        genericExtractor.extractEntities(null);

        EntityExtract studentSchoolAssociation = factory.buildStudentSchoolAssociationExractor(entityExtractor,
                leaToExtractFileMap, repository, student.getEntityCache(), helper);
        studentSchoolAssociation.extractEntities(null);

        genericExtractor = factory.buildStudentAssessmentExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(student.getEntityCache());

        genericExtractor = factory.buildStudentGradebookEntryExtractor(entityExtractor, leaToExtractFileMap,
                repository, helper);
        genericExtractor.extractEntities(student.getEntityCache());

        // Yearly Transcript
        genericExtractor = factory.buildYearlyTranscriptExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(student.getEntityCache());
        EntityToLeaCache studentAcademicRecordCache = ((YearlyTranscriptExtractor)genericExtractor).getStudentAcademicRecordCache();

        genericExtractor = factory.buildParentExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(student.getParentCache());

        // Section
        SectionExtractor sectionExtractor = factory.buildSectionExtractor(entityExtractor,leaToExtractFileMap,repository, student.getEntityCache(), edorgCache, helper);
        sectionExtractor.extractEntities(null);

        // Staff
        StaffEdorgAssignmentExtractor seaExtractor = factory.buildStaffAssociationExtractor(entityExtractor,
                leaToExtractFileMap, repository, helper);
        seaExtractor.extractEntities(edorgCache);

        genericExtractor = factory.buildStaffExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(seaExtractor.getEntityCache());

        genericExtractor = factory.buildTeacherSchoolExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(seaExtractor.getEntityCache());

        //Session and gradingPeriod
        SessionExtractor sessionExtractor = factory.buildSessionExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        sessionExtractor.extractEntities(edorgCache);

        EntityExtract gradingPeriodExtractor = factory.buildGradingPeriodExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        gradingPeriodExtractor.extractEntities(sessionExtractor.getEntityToLeaCache());

        genericExtractor = factory.buildStaffProgramAssociationExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(seaExtractor.getEntityCache());

        genericExtractor = factory.buildStaffCohortAssociationExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(seaExtractor.getEntityCache());

        genericExtractor = factory.buildCohortExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        genericExtractor.extractEntities(edorgCache);

        CourseOfferingExtractor courseOfferingExtractor = factory.buildCourseOfferingExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        courseOfferingExtractor.extractEntities(edorgCache, sectionExtractor.getCourseOfferingCache());

        CourseExtractor courseExtractor = factory.buildCourseExtractor(entityExtractor, leaToExtractFileMap, repository, helper);
        courseExtractor.extractEntities(edorgCache, courseOfferingExtractor.getCourseCache());

        CourseTranscriptExtractor courseTranscriptExtractor = factory.buildCourseTranscriptExtractor(entityExtractor, leaToExtractFileMap, repository);
        courseTranscriptExtractor.extractEntities(edorgCache, courseOfferingExtractor.getCourseCache(), studentAcademicRecordCache);

        leaToExtractFileMap.closeFiles();

        leaToExtractFileMap.buildManifestFiles(startTime);
        leaToExtractFileMap.archiveFiles();

        // 3. ARCHIVE
        updateBulkExtractDb(tenant, startTime);
        LOG.info("Finished LEA based extract in: {} seconds",
                (new DateTime().getMillis() - this.startTime.getMillis()) / 1000);
        audit(securityEventUtil.createSecurityEvent(this.getClass().getName(), "Marks the end of LEA level extract", LogLevelType.TYPE_INFO, BEMessageCode.BE_SE_CODE_0009));
    }

    private void updateBulkExtractDb(String tenant, DateTime startTime) {
        for (String lea : helper.getBulkExtractLEAs()) {
            // update db to point to new archive
            for (Entry<String, File> archiveFile : leaToExtractFileMap.getExtractFileForLea(lea).getArchiveFiles()
                    .entrySet()) {
                bulkExtractMongoDA.updateDBRecord(tenant, archiveFile.getValue().getAbsolutePath(), archiveFile.getKey(),
                        startTime.toDate(), false, lea, false);
            }
        }
    }

    private Map<String, ExtractFile> buildLEAToExtractFile() {
        Map<String, ExtractFile> edOrgToLEAExtract = new HashMap<String, ExtractFile>();

        Map<String, PublicKey> appPublicKeys = bulkExtractMongoDA.getAppPublicKeys();
        for (String lea : helper.getBulkExtractLEAs()) {
            ExtractFile file = factory.buildLEAExtractFile(tenantDirectory.getAbsolutePath(), lea,
                    getArchiveName(lea, startTime.toDate()), appPublicKeys, securityEventUtil);
            edOrgToLEAExtract.put(lea, file);
            for (String child : helper.getChildEdOrgs(Arrays.asList(lea))) {
                edOrgToLEAExtract.put(child, file);
            }

        }
        return edOrgToLEAExtract;
    }

    /**
     * Returns a map that maps an edorg to it's top level LEA, used as a cache
     * to speed up extract
     *
     * @return a map that has the lea to the set of all it's child edorgs
     */
    private EntityToLeaCache buildEdOrgCache() {
        EntityToLeaCache cache = new EntityToLeaCache();
        for (String lea : helper.getBulkExtractLEAs()) {
            Set<String> children = helper.getChildEdOrgs(Arrays.asList(lea));
            children.add(lea);
            for (String child : children) {
                cache.addEntry(lea, child);
            }

        }
        return cache;
    }

    public void setRepository(Repository<Entity> repository) {
        this.repository = repository;
    }

    public Repository<Entity> getRepository() {
        return repository;
    }

    private String getArchiveName(String edOrg, Date startTime) {
        return edOrg + "-" + Launcher.getTimeStamp(startTime);
    }

    public EntityExtractor getEntityExtractor() {
        return entityExtractor;
    }

    public void setEntityExtractor(EntityExtractor entityExtractor) {
        this.entityExtractor = entityExtractor;
    }

    public Map<String, String> getEntitiesToCollections() {
        return entitiesToCollections;
    }

    public void setEntitiesToCollections(Map<String, String> entitiesToCollections) {
        this.entitiesToCollections = entitiesToCollections;
    }

    public BulkExtractMongoDA getBulkExtractMongoDA() {
        return bulkExtractMongoDA;
    }

    /**
     * Set bulkExtractMongoDA.
     *
     * @param bulkExtractMongoDA
     *            the bulkExtractMongoDA to set
     */
    public void setBulkExtractMongoDA(BulkExtractMongoDA bulkExtractMongoDA) {
        this.bulkExtractMongoDA = bulkExtractMongoDA;
    }

    public void setHelper(LocalEdOrgExtractHelper helper) {
        this.helper = helper;
    }

    public void setFactory(LEAExtractorFactory factory) {
        this.factory = factory;
    }

    public void setLeaToExtractMap(LEAExtractFileMap map) {
        this.leaToExtractFileMap = map;
    }

    /**
     * Set securityEventUtil.
     * @param securityEventUtil the securityEventUtil to set
     */
    public void setSecurityEventUtil(SecurityEventUtil securityEventUtil) {
        this.securityEventUtil = securityEventUtil;
    }
}
