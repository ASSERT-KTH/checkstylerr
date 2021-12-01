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


import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.common.encrypt.security.CertificateValidationHelper;
import org.slc.sli.common.util.tenantdb.TenantContext;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.domain.Repository;


/**
 * Mongo access to bulk extract data.
 * @author tke
 *
 */
public class BulkExtractMongoDA {
    private static final Logger LOG = LoggerFactory.getLogger(BulkExtractMongoDA.class);

    private static final String TENANT = "tenant";
    /**
     * name of bulkExtract collection.
     */
    public static final String BULK_EXTRACT_COLLECTION = "bulkExtractFiles";
    private static final String FILE_PATH = "path";
    private static final String DATE = "date";
    private static final String TENANT_ID = "tenantId";
    private static final String IS_DELTA = "isDelta";
    private static final String IS_PUBLIC_DATA = "isPublicData";

    private static final String APP_AUTH_COLLECTION = "applicationAuthorization";
    private static final String TENANT_EDORG_FIELD = "edorgs";
    private static final String EDORG = "edorg";
    private static final String AUTH_EDORGS_FIELD = "authorized_ed_orgs";
    private static final String APP_COLLECTION = "application";
    private static final String APP_ID = "applicationId";
    private static final String APP_APPROVE_STATUS = "APPROVED";
    private static final String REGISTRATION_STATUS_FIELD = "registration.status";
    private static final String IS_BULKEXTRACT = "isBulkExtract";

    private Repository<Entity> entityRepository;
    private CertificateValidationHelper certHelper;

    /** Insert a new record is the tenant doesn't exist. Update if existed
     * @param tenantId tenant id
     * @param path  path to the extracted file.
     * @param date  the date when the bulk extract was created
     * @param appId the id for the application
     */
    public void updateDBRecord(String tenantId, String path, String appId, Date date) {
        updateDBRecord(tenantId, path, appId, date, false, null, false);
    }

    /** Insert a new record is the tenant doesn't exist. Update if existed
     * @param tenantId tenant id
     * @param path  path to the extracted file.
     * @param appId the id for the application
     * @param date  the date when the bulk extract was created
     * @param isDelta indicates whether or not extract is delta
     * @param edorg the id of the education organization
     * @param isPublicData indicates if the extract is for public data
     */
    public void updateDBRecord(String tenantId, String path, String appId, Date date,  boolean isDelta, String edorg, boolean isPublicData) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put(TENANT_ID, tenantId);
        body.put(FILE_PATH, path);
        body.put(DATE, date);
        body.put(IS_DELTA, isDelta);
        body.put(EDORG, edorg);
        body.put(IS_PUBLIC_DATA, isPublicData);
        body.put(APP_ID, appId);

        String entityId;
        if (isDelta) {
            entityId = tenantId + "-" + appId + "-" + edorg + "-" + date.getTime();
        } else if (edorg != null) {
            entityId = tenantId + "-" + appId + "-" + edorg;
        } else {
            entityId = tenantId + "-" + appId;
        }
        BulkExtractEntity bulkExtractEntity = new BulkExtractEntity(body, entityId);

        entityRepository.update(BULK_EXTRACT_COLLECTION, bulkExtractEntity, false);

        LOG.info("Finished creating bulk extract record");
    }

    /**
     * Get the public keys for all the bulk extract applications.
     * @return A map from clientId to public key
     */
    @SuppressWarnings("unchecked")
    public Map<String, PublicKey> getAppPublicKeys() {
        Map<String, PublicKey> appKeys = new HashMap<String, PublicKey>();

        Iterator<Entity> cursor = entityRepository.findEach(APP_AUTH_COLLECTION, new NeutralQuery());
        while(cursor.hasNext()){
            Entity appAuth = cursor.next();
            String appId = (String) appAuth.getBody().get(APP_ID);
            List<String> edorgs = (List<String>) appAuth.getBody().get(TENANT_EDORG_FIELD);

            appKeys.putAll(getClientIdAndPublicKey(appId, edorgs));
        }

        return appKeys;
    }

    @SuppressWarnings("boxing")
    public Map<String, PublicKey> getClientIdAndPublicKey(String appId, List<String> edorgs) {
        Map<String, PublicKey> clientPubKeys = new HashMap<String, PublicKey>();

        NeutralQuery query = new NeutralQuery(new NeutralCriteria("_id", NeutralCriteria.OPERATOR_EQUAL, appId));
        query.addCriteria(new NeutralCriteria(REGISTRATION_STATUS_FIELD, NeutralCriteria.OPERATOR_EQUAL, APP_APPROVE_STATUS));
        query.addCriteria(new NeutralCriteria(IS_BULKEXTRACT, NeutralCriteria.OPERATOR_EQUAL, true));

        TenantContext.setIsSystemCall(true);
        Entity app = this.entityRepository.findOne(APP_COLLECTION, query);
        TenantContext.setIsSystemCall(false);

        if(app != null){
            Map<String, Object> body = app.getBody();
            if (body.containsKey(IS_BULKEXTRACT)) {
                if((Boolean) body.get(IS_BULKEXTRACT)) {
                	String clientId = (String) body.get("client_id");
                    try {
                        List<String> authorizedTenantEdorgs = getAuthorizedTenantEdorgs(app, edorgs);

                        if(authorizedTenantEdorgs.isEmpty()) {
                            LOG.info("No education organization is authorized, skipping application {}", appId);
                        }

                    	PublicKey key = certHelper.getPublicKeyForApp(clientId);
						if (null != key) {
							clientPubKeys.put(appId, key);
						} else {
							LOG.error("X509 Certificate for alias {} does not contain a public key", clientId);
						}

                    } catch (IllegalStateException e) {
                    	LOG.error("App {} doesn't have X509 Certificate or public key", appId);
                    	LOG.error("", e);
                    }
                }
            }
        }

        return clientPubKeys;
    }

    /**
     * check if a tenant exists.
     * @param tenant tenant ID
     * @return
     *      the tenant entity
     */
    public Entity getTenant(String tenant) {
        NeutralQuery query = new NeutralQuery();
        query.addCriteria(new NeutralCriteria("tenantId", NeutralCriteria.OPERATOR_EQUAL ,tenant));
        query.addCriteria(new NeutralCriteria("tenantIsReady", NeutralCriteria.OPERATOR_EQUAL, true));
        TenantContext.setIsSystemCall(true);
        Entity tenantEntity = entityRepository.findOne(TENANT, query);
        TenantContext.setIsSystemCall(false);
        return tenantEntity;
    }

    @SuppressWarnings({ "unchecked" })
    private static List<String> getAuthorizedTenantEdorgs(Entity app, List<String> tenantEdorgs) {
        List<String> authorizedTenantEdorgs = (List<String>) app.getBody().get(AUTH_EDORGS_FIELD);

        authorizedTenantEdorgs.retainAll(tenantEdorgs);

        return authorizedTenantEdorgs;
    }

    /**
     * get entity repository.
     * @return repository
     */
    public Repository<Entity> getEntityRepository() {
        return entityRepository;
    }

    /**
     * set entity repository.
     * @param entityRepository the entityRepository to set
     */
    public void setEntityRepository(Repository<Entity> entityRepository) {
        this.entityRepository = entityRepository;
    }


    public CertificateValidationHelper getCertHelper() {
		return certHelper;
	}

	public void setCertHelper(CertificateValidationHelper certHelper) {
		this.certHelper = certHelper;
	}

    public Map<String, List> getEdOrgLineages() {
        Map<String, List> edOrgLineages = new HashMap<String, List>();
        NeutralQuery query = new NeutralQuery();
        query.setIncludeFields(Lists.newArrayList("_id", "metaData.edOrgs"));
        Iterable<Entity> edOrgs = entityRepository.findAll("educationOrganization", query);
        if(edOrgs != null) {
            for(Entity edOrg:edOrgs) {
                if( edOrg.getMetaData().containsKey(ParameterConstants.EDORGS_ARRAY) &&
                        edOrg.getMetaData().get( ParameterConstants.EDORGS_ARRAY) instanceof List ) {
                    edOrgLineages.put(edOrg.getEntityId(), (List<String>)edOrg.getMetaData().get(ParameterConstants.EDORGS_ARRAY));
                }
            }
        }
        return edOrgLineages;
    }
}
