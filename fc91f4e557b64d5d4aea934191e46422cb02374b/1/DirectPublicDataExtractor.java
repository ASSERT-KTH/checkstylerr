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

package org.slc.sli.bulk.extract.pub;

import org.slc.sli.bulk.extract.extractor.EntityExtractor;
import org.slc.sli.bulk.extract.files.ExtractFile;
import org.slc.sli.bulk.extract.util.EdOrgPathDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author ablum
 */
public class DirectPublicDataExtractor implements PublicDataExtractor {

    private EntityExtractor extractor;

    public DirectPublicDataExtractor(EntityExtractor extractor) {
        this.extractor = extractor;
    }


    @Override
    public void extract(String edOrgid, ExtractFile file) {

        for (EdOrgPathDefinition definition : EdOrgPathDefinition.values()) {
            Query query = new Query((new Criteria(definition.getEdOrgRefField())).is(edOrgid));
            extractor.setExtractionQuery(query);
            extractor.extractEntities(file, definition.getEntityName());
        }
     }
}
