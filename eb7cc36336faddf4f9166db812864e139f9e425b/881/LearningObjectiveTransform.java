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

package org.slc.sli.ingestion.transformation.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.ingestion.NeutralRecord;
import org.slc.sli.ingestion.reporting.impl.CoreMessageCode;
import org.slc.sli.ingestion.reporting.impl.ElementSourceImpl;
import org.slc.sli.ingestion.transformation.AbstractTransformationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Modifies the LearningObjective to match the SLI datamodel.
 *
 * Transform: Instead of a LO pointing to its children, it points to its parent.
 *
 * @author Ryan Farris <rfarris@wgen.net>
 *
 */
@Scope("prototype")
@Component("learningObjectiveTransformationStrategy")
public class LearningObjectiveTransform extends AbstractTransformationStrategy {

    public static final String LEARNING_OBJECTIVE = "learningObjective";
    public static final String LEARNING_OBJECTIVE_TRANSFORMED = "learningObjective_transformed";
    public static final String ID_CODE = "identificationCode";
    public static final String CONTENT_STANDARD_NAME = "contentStandardName";
    public static final String LO_ID_CODE_PATH = "learningObjectiveId." + ID_CODE;
    public static final String LO_CONTENT_STANDARD_NAME_PATH = "learningObjectiveId." + CONTENT_STANDARD_NAME;
    public static final String LS_ID_CODE_PATH = ID_CODE;
    public static final String LS_CONTENT_STANDARD_NAME_PATH = "learningStandardId." + CONTENT_STANDARD_NAME;
    public static final String LEARNING_OBJ_REFS = "learningObjectiveRefs";
    public static final String LEARNING_STD_REFS = "learningStandardRefs";
    public static final String PARENT_LEARNING_OBJ_REF = "LearningObjectiveReference";
    public static final String LOCAL_ID_OBJECTIVE_ID = "parentObjectiveId";
    public static final String LOCAL_ID_LEARNING_STANDARDS = "childLearningStandards";

    private static final Logger LOG = LoggerFactory.getLogger(LearningObjectiveTransform.class);

    @Override
    protected void performTransformation() {

        LOG.info("Loading data for learning objective transformation.");
        Iterable<NeutralRecord> learningObjectives = getCollectionIterableFromDb(LEARNING_OBJECTIVE);

        Map<LearningObjectiveId, NeutralRecord> learningObjectiveIdMap = prepareLearningObjectiveLookupMap(learningObjectives);
        LOG.info("{} is loaded into local storage.  Total Count = {}", LEARNING_OBJECTIVE,
                learningObjectiveIdMap.size());
        List<NeutralRecord> transformedLearningObjectives = new ArrayList<NeutralRecord>();

        for (NeutralRecord parentLO : learningObjectives) {
            // add to working set for persistence
            transformedLearningObjectives.add(parentLO);

            flipLearningObjectiveRelDirection(parentLO, learningObjectiveIdMap, transformedLearningObjectives);

            moveLearningStdRefsToParentIds(parentLO);
        }

        insertTransformedLearningObjectives(transformedLearningObjectives);
    }

    private Map<LearningObjectiveId, NeutralRecord> prepareLearningObjectiveLookupMap(
            Iterable<NeutralRecord> learningObjectives) {

        Map<LearningObjectiveId, NeutralRecord> learningObjectiveIdMap = new HashMap<LearningObjectiveId, NeutralRecord>();

        for (NeutralRecord lo : learningObjectives) {
            Map<String, Object> attributes = lo.getAttributes();
            String objective = getByPath("objective", attributes);
            String academicSubject = getByPath("academicSubject", attributes);
            String objectiveGradeLevel = getByPath("objectiveGradeLevel", attributes);
            if (objective != null && academicSubject != null && objectiveGradeLevel != null) {
                if (learningObjectiveIdMap.containsKey(new LearningObjectiveId(objective, academicSubject,
                        objectiveGradeLevel))) {
                    reportError(lo.getSourceFile(), new ElementSourceImpl(lo), CoreMessageCode.CORE_0037, objective, academicSubject,
                            objectiveGradeLevel);
                    continue;
                }
                learningObjectiveIdMap
                        .put(new LearningObjectiveId(objective, academicSubject, objectiveGradeLevel), lo);
            }
        }
        return learningObjectiveIdMap;
    }

    @SuppressWarnings("unchecked")
    private void flipLearningObjectiveRelDirection(NeutralRecord parentLO,
            Map<LearningObjectiveId, NeutralRecord> learningObjectiveIdMap,
            List<NeutralRecord> transformedLearningObjectives) {

        Map<String, Object> attributes = parentLO.getAttributes();
        List<Map<String, Object>> childLearningObjRefs = (List<Map<String, Object>>) parentLO.getAttributes().get(
                LEARNING_OBJ_REFS);

        Map<String, Object> parentLearningObjRefs = new HashMap<String, Object>();

        Map<String, Object> learningObjIdentity = new HashMap<String, Object>();
        learningObjIdentity.put("Objective", getByPath("objective", attributes));
        learningObjIdentity.put("AcademicSubject", getByPath("academicSubject", attributes));
        learningObjIdentity.put("ObjectiveGradeLevel", getByPath("objectiveGradeLevel", attributes));
        parentLearningObjRefs.put("LearningObjectiveIdentity", learningObjIdentity);

        for (Map<String, Object> childLORef : childLearningObjRefs) {

            String objective = getByPath("LearningObjectiveIdentity.Objective", childLORef);
            String academicSubject = getByPath("LearningObjectiveIdentity.AcademicSubject", childLORef);
            String objectiveGradeLevel = getByPath("LearningObjectiveIdentity.ObjectiveGradeLevel", childLORef);

            LearningObjectiveId loId = new LearningObjectiveId(objective, academicSubject, objectiveGradeLevel);
            NeutralRecord childNR = learningObjectiveIdMap.get(loId);

            if (childNR != null) {
                setParentObjectiveRef(childNR, parentLearningObjRefs);
            } else {
                // try sli db
                NeutralQuery query = new NeutralQuery();
                query.addCriteria(new NeutralCriteria("objective", NeutralCriteria.OPERATOR_EQUAL, objective));
                query.addCriteria(new NeutralCriteria("academicSubject", NeutralCriteria.OPERATOR_EQUAL,
                        academicSubject));
                query.addCriteria(new NeutralCriteria("objectiveGradeLevel", NeutralCriteria.OPERATOR_EQUAL,
                        objectiveGradeLevel));

                Entity childEntity = getMongoEntityRepository().findOne("learningObjective", query);

                if (childEntity != null) {
                    NeutralRecord childEntityNR = new NeutralRecord();
                    childEntityNR.setAttributes(childEntity.getBody());
                    childEntityNR.setRecordType(childEntity.getType());
                    childEntityNR.setBatchJobId(parentLO.getBatchJobId());
                    setParentObjectiveRef(childEntityNR, parentLearningObjRefs);

                    // add this entity to our NR working set
                    transformedLearningObjectives.add(childEntityNR);
                } else {
                    reportError(parentLO.getSourceFile(), new ElementSourceImpl(parentLO), CoreMessageCode.CORE_0034, objective, academicSubject,
                            objectiveGradeLevel);
                }
            }
        }

        parentLO.getAttributes().remove(LEARNING_OBJ_REFS);
    }

    private void setParentObjectiveRef(NeutralRecord childLo, Map<String, Object> childLearningObjRefs) {
        if (childLo.getAttributes() == null) {
            childLo.setAttributes(new HashMap<String, Object>());
        }
        if (!childLo.getAttributes().containsKey(PARENT_LEARNING_OBJ_REF)) {
            childLo.getAttributes().put(PARENT_LEARNING_OBJ_REF, childLearningObjRefs);
        } else {
            reportError(childLo.getSourceFile(), new ElementSourceImpl(childLo), CoreMessageCode.CORE_0030, childLearningObjRefs.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private void moveLearningStdRefsToParentIds(NeutralRecord parentLO) {
        List<Map<String, Object>> childLearningStdRefs = (List<Map<String, Object>>) parentLO.getAttributes().get(
                LEARNING_STD_REFS);

        List<Map<String, Object>> lsRefList = new ArrayList<Map<String, Object>>();
        parentLO.getLocalParentIds().put(LOCAL_ID_LEARNING_STANDARDS, lsRefList);

        ArrayList<String> uuidRefs = new ArrayList<String>();
        if (childLearningStdRefs != null) {
            for (Map<String, Object> learnStdRef : childLearningStdRefs) {
                if (learnStdRef == null) {
                    reportError(parentLO.getSourceFile(), new ElementSourceImpl(parentLO), CoreMessageCode.CORE_0031,
                            getByPath(LO_ID_CODE_PATH, parentLO.getAttributes()));
                } else {
                    String idCode = getByPath(LS_ID_CODE_PATH, learnStdRef);
                    String csn = getByPath(LS_CONTENT_STANDARD_NAME_PATH, learnStdRef);

                    if (idCode != null) {
                        Map<String, Object> ref = new HashMap<String, Object>();
                        ref.put(ID_CODE, idCode);
                        ref.put(CONTENT_STANDARD_NAME, csn);
                        lsRefList.add(ref);
                        uuidRefs.add(null); // this is slightly hacky, but the
                                            // IdNormalizer will
                        // throw ArrayIndexOutOfBounds unless we pre-populate
                        // the list the UUIDs will be added to.
                    }
                }
            }
        }

        parentLO.getAttributes().remove(LEARNING_STD_REFS);
        parentLO.getAttributes().put("learningStandards", uuidRefs);
    }

    private void insertTransformedLearningObjectives(List<NeutralRecord> transformedLearningObjectives) {
        for (NeutralRecord nr : transformedLearningObjectives) {
            nr.setRecordType(nr.getRecordType() + "_transformed");
            nr.setCreationTime(getWorkNote().getRangeMinimum());
        }
        insertRecords(transformedLearningObjectives, LEARNING_OBJECTIVE_TRANSFORMED);
    }

    @SuppressWarnings("unchecked")
    private static String getByPath(String name, Map<String, Object> map) {
        // how many times have I written this code? Not enough, I say!
        String[] path = name.split("\\.");
        Map<String, Object> pathMap = map;
        for (int i = 0; i < path.length; i++) {
            Object obj = pathMap.get(path[i]);
            if (obj == null) {
                return null;
            } else if ((i == (path.length - 1)) && (obj instanceof String)) {
                return (String) obj;
            } else if (obj instanceof Map) {
                pathMap = (Map<String, Object>) obj;
            } else {
                return null;
            }
        }
        return null;
    }

    private static class LearningObjectiveId {
        private final String objective;
        private final String academicSubject;
        private final String objectiveGradeLevel;

        public LearningObjectiveId(String objective, String academicSubject, String objectiveGradeLevel) {
            this.objective = objective;
            this.academicSubject = academicSubject;
            this.objectiveGradeLevel = objectiveGradeLevel;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((objective == null) ? 0 : objective.hashCode());
            result = prime * result + ((academicSubject == null) ? 0 : academicSubject.hashCode());
            result = prime * result + ((objectiveGradeLevel == null) ? 0 : objectiveGradeLevel.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LearningObjectiveId other = (LearningObjectiveId) obj;
            if (objective == null) {
                if (other.objective != null) {
                    return false;
                }
            } else if (!objective.equals(other.objective)) {
                return false;
            }
            if (academicSubject == null) {
                if (other.academicSubject != null) {
                    return false;
                }
            } else if (!academicSubject.equals(other.academicSubject)) {
                return false;
            }
            if (objectiveGradeLevel == null) {
                if (other.objectiveGradeLevel != null) {
                    return false;
                }
            } else if (!objectiveGradeLevel.equals(other.objectiveGradeLevel)) {
                return false;
            }
            return true;
        }
    }
}
