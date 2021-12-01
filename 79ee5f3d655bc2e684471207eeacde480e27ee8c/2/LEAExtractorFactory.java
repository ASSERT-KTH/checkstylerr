/*
 * Copyright 2012 Shared Learning Collaborative, LLC
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

package org.slc.sli.bulk.extract.lea;

import java.io.File;
import java.security.PublicKey;
import java.util.Map;

import org.slc.sli.bulk.extract.extractor.EntityExtractor;
import org.slc.sli.bulk.extract.files.ExtractFile;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.Repository;

public class LEAExtractorFactory {
    
    public EdorgExtractor buildEdorgExtractor(EntityExtractor extractor, LEAExtractFileMap map) {
        return new EdorgExtractor(extractor, map);
    }
    
    public StudentExtractor buildStudentExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new StudentExtractor(extractor, map, repo, new ExtractorHelper(), new EntityToLeaCache(),
                new EntityToLeaCache());
    }
    
    public EntityExtract buildStudentAssessmentExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new StudentAssessmentExtractor(extractor, map, repo);
    }
    
    public EntityExtract buildYearlyTranscriptExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new YearlyTranscriptExtractor(extractor, map, repo);
    }
    
    public EntityExtract buildParentExtractor(EntityExtractor extractor, LEAExtractFileMap map, Repository<Entity> repo) {
        return new ParentExtractor(extractor, map, repo);
    }

    public StaffEdorgAssignmentExtractor buildStaffAssociationExtractor(EntityExtractor extractor,
            LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new StaffEdorgAssignmentExtractor(extractor, map, repo, new ExtractorHelper(), new EntityToLeaCache());
    }
    
    public EntityExtract buildStaffExtractor(EntityExtractor extractor, LEAExtractFileMap map, Repository<Entity> repo) {
        return new StaffExtractor(extractor, map, repo);
    }
    
    public EntityExtract buildTeacherSchoolExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new TeacherSchoolExtractor(extractor, map, repo);
    }

    public EntityExtract buildAttendanceExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo, EntityToLeaCache studentCache) {
        return new AttendanceExtractor(extractor, map, repo, new ExtractorHelper(), studentCache);
    }
    
    public EntityExtract buildStudentSchoolAssociationExractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo, EntityToLeaCache studentCache) {
        return new StudentSchoolAssociationExtractor(extractor, map, repo, studentCache);
    }
    
    public SessionExtractor buildSessionExtractor(EntityExtractor extractor, LEAExtractFileMap map, Repository<Entity> repo) {
    	return new SessionExtractor(extractor, map, repo, new ExtractorHelper(), new EntityToLeaCache());
    }
    
    public EntityExtract buildGradingPeriodExtractor(EntityExtractor extractor, LEAExtractFileMap map, Repository<Entity> repo) {
    	return new GradingPeriodExtractor(extractor, map, repo);
    }

    public ExtractFile buildLEAExtractFile(String path, String lea, String archiveName,
            Map<String, PublicKey> appPublicKeys) {
        File leaDirectory = new File(path, lea);
        leaDirectory.mkdirs();
        return new ExtractFile(leaDirectory, archiveName, appPublicKeys);
    }
    
    public EntityExtract buildCohortExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new CohortExtractor(extractor, map, repo);
    }
    public EntityExtract buildStaffCohortAssociationExtractor(EntityExtractor extractor, LEAExtractFileMap map,
            Repository<Entity> repo) {
        return new StaffCohortAssociationExtractor(extractor, map, repo);
    }

    public EntityExtract buildSectionExtractor(EntityExtractor entityExtractor, LEAExtractFileMap leaToExtractFileMap, Repository<Entity> repository, EntityToLeaCache entityCache, EntityToLeaCache edorgCache) {
        return new SectionExtractor(entityExtractor, leaToExtractFileMap, repository, entityCache, edorgCache);
    }
}
