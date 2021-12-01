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
package org.slc.sli.bulk.extract.context.resolver.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slc.sli.bulk.extract.context.resolver.ContextResolver;
import org.slc.sli.domain.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Context resolver for sections
 * 
 * @author nbrown
 * 
 */
@Component
public class SectionContextResolver implements ContextResolver {
    private static final Logger LOG = LoggerFactory.getLogger(SectionContextResolver.class);
    
    @Autowired
    private EducationOrganizationContextResolver edOrgResolver;
    
    @Autowired
    private StudentContextResolver studentResolver;
    
    @Override
    public Set<String> findGoverningLEA(Entity entity) {
        String schoolId = (String) entity.getBody().get("schoolId");
        Set<String> leas = new HashSet<String>();
        if(schoolId == null) {
            LOG.warn("Section found without a school id: {}", entity);
        } else {
            leas.addAll(edOrgResolver.findGoverningLEA(schoolId));
        }
        List<Entity> studentAssociations = entity.getEmbeddedData().get("studentSectionAssociation");
        LOG.debug("studentSectionAssociations being process for {} are {}", entity, studentAssociations);
        if (studentAssociations != null) {
            for(Entity studentAssociation: studentAssociations) {
                String studentId = (String) studentAssociation.getBody().get("studentId");
                if (studentId == null) {
                    LOG.warn("Student Section association without a student id: {}", studentAssociation);
                } else {
                    leas.addAll(studentResolver.findGoverningLEA(studentId));
                }
            }
        }
        return leas;
    }
    
}
