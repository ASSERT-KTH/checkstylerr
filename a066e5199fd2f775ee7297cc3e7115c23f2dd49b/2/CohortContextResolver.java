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
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slc.sli.common.constants.EntityNames;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.utils.EdOrgHierarchyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Context resolver for cohorts, be base on ed org association
 * 
 * @author nbrown
 *
 */
@Component
public class CohortContextResolver extends ReferrableResolver {
	
    public final static String EDORG_REFERENCE = "educationOrgId";

    private static final Logger LOG = LoggerFactory.getLogger(SectionContextResolver.class);

    @Autowired
    private EducationOrganizationContextResolver edOrgResolver;
    
    private EdOrgHierarchyHelper edOrgHelper;

    @PostConstruct
    public void init() {
    	edOrgHelper = new EdOrgHierarchyHelper(getRepo());
    }

	@Override
	protected String getCollection() {
        return EntityNames.COHORT;
	}

	@Override
	protected Set<String> resolve(Entity entity) {
		Set<String> leas = new HashSet<String>();

		String id = entity.getEntityId();
		if (id == null) {
			return leas;
		}

		String edorgReference = (String) entity.getBody().get(EDORG_REFERENCE);

		Entity edOrg = getRepo().findById(EntityNames.EDUCATION_ORGANIZATION,
				edorgReference);

		if (edOrg != null && edOrgHelper.isSEA(edOrg)) {
			return leas;
		}
		if (edorgReference != null) {
			leas.addAll(edOrgResolver.findGoverningEdOrgs(edorgReference));
		}

		return leas;
	}
    
}
