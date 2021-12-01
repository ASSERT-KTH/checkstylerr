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
package org.slc.sli.search.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.BasicBSONObject;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.dal.encrypt.EntityEncryption;
import org.slc.sli.search.config.IndexConfig;
import org.slc.sli.search.config.IndexConfigStore;
import org.slc.sli.search.connector.SourceDatastoreConnector;
import org.slc.sli.search.entity.IndexEntity;
import org.slc.sli.search.entity.IndexEntity.Action;
import org.slc.sli.search.transform.impl.GenericTransformer;
import org.slc.sli.search.util.IndexEntityUtil;
import org.slc.sli.search.util.SearchIndexerException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * IndexEntityConverter handles conversion of IndexEntity to and from entity
 *
 */
public class IndexEntityConverter {
    private EntityEncryption entityEncryption;
    private IndexConfigStore indexConfigStore;
    private SourceDatastoreConnector sourceDatastoreConnector;
    
    private final GenericTransformer transformer = new GenericTransformer();
    // decrypt records flag
    private boolean decrypt = true;

    public List<IndexEntity> fromEntityJson(String index, String entity) {
        return fromEntityJson(index, Action.INDEX, entity);
    }

    public List<IndexEntity> fromEntityJson(String index, Action action, String entity) {
        Map<String, Object> entityMap = IndexEntityUtil.getEntity(entity);
        return fromEntity(index, action, entityMap);
    }

    @SuppressWarnings("unchecked")
	public List<IndexEntity> fromEntity(String index, Action action, Map<String, Object> entityMap) {
    	List<IndexEntity> indexEntities = new ArrayList<IndexEntity>();
        String type = (String)entityMap.get("type");
        TenantContext.setTenantId("Midgar");
        Map<String, Object> metaData = (Map<String, Object>) entityMap.get("metaData");
        Map<String, Object> body = (Map<String, Object>) entityMap.get("body");
        if ("assessmentPeriodDescriptor".equals(type)) {
        	//String indexName = (index == null) ? ((String)metaData.get("tenantId")).toLowerCase() : index.toLowerCase();	
            DBObject query = new BasicDBObject("body.assessmentPeriodDescriptorId", entityMap.get("_id"));
        	IndexConfig config = indexConfigStore.getConfig("assessment");
        	
        	DBCursor cursor = sourceDatastoreConnector.getDBCursor("assessment", config.getFields(), query);	
        	while (cursor.hasNext()) {
        		DBObject obj = cursor.next();
        		Map<String, Object> assessmentMap = obj.toMap();
        		((Map<String, Object>)assessmentMap.get("body")).put("assessmentPeriodDescriptor", Arrays.asList(entityMap.get("body")));
        		((Map<String, Object>)assessmentMap.get("body")).remove("assessmentPeriodDescriptorId");
        		indexEntities.add(transformEntity(index, action, assessmentMap));
        	}
        } if ("assessment".equals(type)) {
        	if (body != null) {
        		String assessmentPeriodDescriptorId = (String) body.remove("assessmentPeriodDescriptorId");
        		if (assessmentPeriodDescriptorId != null) {
        			IndexConfig config = indexConfigStore.getConfig("assessmentPeriodDescriptor");
        			DBObject query = new BasicDBObject("_id", assessmentPeriodDescriptorId);
        			DBCursor cursor = sourceDatastoreConnector.getDBCursor("assessmentPeriodDescriptor", config.getFields(), query);
        			if (cursor.hasNext()) {
        				DBObject obj = cursor.next();
        				Map<String, Object> assessmentPeriodDescriptor = obj.toMap();
        				((Map<String, Object>) entityMap.get("body")).put("assessmentPeriodDescriptor", Arrays.asList(assessmentPeriodDescriptor.get("body")));
        			}
        		}
        	}
        	indexEntities.add(transformEntity(index, action, entityMap));
        }
        else {
        	indexEntities.add(transformEntity(index, action, entityMap));
        }
        return indexEntities;
    }

    
    @SuppressWarnings("unchecked")
	private IndexEntity transformEntity(String index, Action action, Map<String, Object> entityMap) {
        try {
            Map<String, Object> body = (Map<String, Object>) entityMap.get("body");
            Map<String, Object> metaData = (Map<String, Object>) entityMap.get("metaData");
            String type = (String)entityMap.get("type");
            // decrypt body if needed
            Map<String, Object> decryptedMap = null;
            if (body != null) {
                decryptedMap = decrypt ? entityEncryption.decrypt(type, body): body;
            }
            //re-assemble entity map
            entityMap.put("body", decryptedMap);
            // get tenantId
            String indexName = (index == null) ? ((String)metaData.get("tenantId")).toLowerCase() : index.toLowerCase();
            IndexConfig config = indexConfigStore.getConfig(type);

            // filter out
            if (!transformer.isMatch(config, entityMap)) {
                return null;
            }

            // transform the entities
            transformer.transform(config, entityMap);

            String id = (String)entityMap.get("_id");
            String indexType = config.getIndexType() == null ? type : config.getIndexType();
            Action finalAction = config.isChildDoc() ?  IndexEntity.Action.UPDATE : action;
            body = (Map<String, Object>)entityMap.get("body");
            if (body == null && action != Action.DELETE) {
                return null;
            }
            return new IndexEntity(finalAction, indexName, indexType, id, body );

        } catch (Exception e) {
            throw new SearchIndexerException("Unable to convert entity", e);
        }
    }
    
    public void setDecrypt(boolean decrypt) {
        this.decrypt = decrypt;
    }
    public void setEntityEncryption(EntityEncryption entityEncryption) {
        this.entityEncryption = entityEncryption;
    }

    public void setIndexConfigStore(IndexConfigStore indexConfigStore) {
        this.indexConfigStore = indexConfigStore;
    }
    
    public void setSourceDatastoreConnector(SourceDatastoreConnector sourceDatastoreConnector) {
        this.sourceDatastoreConnector = sourceDatastoreConnector;
    }
}