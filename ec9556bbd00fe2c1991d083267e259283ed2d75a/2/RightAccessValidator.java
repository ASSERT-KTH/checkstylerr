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
package org.slc.sli.api.security.roles;

import org.slc.sli.api.config.EntityDefinition;
import org.slc.sli.api.representation.EntityBody;
import org.slc.sli.api.security.SLIPrincipal;
import org.slc.sli.api.security.schema.SchemaDataProvider;
import org.slc.sli.api.util.SecurityUtil;
import org.slc.sli.domain.Entity;
import org.slc.sli.domain.NeutralCriteria;
import org.slc.sli.domain.NeutralQuery;
import org.slc.sli.domain.QueryParseException;
import org.slc.sli.domain.enums.Right;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

/** Class to validate if the context has the right to access the entity.
 *
 * @author: tke
 */
@Component
public class RightAccessValidator {

    private static final String ADMIN_SPHERE = "Admin";

    @Autowired
    EntityEdOrgRightBuilder entityEdOrgRightBuilder;

    @Autowired
    private SchemaDataProvider provider;

    /**
     * Validates that user roles allow access to fields.
     *
     * @param isRead  whether operation is "read" or "write"
     * @param isSelf  whether operation is being done in "self" context
     * @param entity item under inspection
     */
    public void checkAccess(boolean isRead, boolean isSelf, Entity entity) {
        SecurityUtil.ensureAuthenticated();
        Set<Right> neededRights = new HashSet<Right>();

        Collection<GrantedAuthority> auths = getAuthorities(isSelf, entity);

        boolean allow = false;
        if (auths.contains(Right.FULL_ACCESS)) {
            debug("User has full access");
            allow = true;
        } else if (ADMIN_SPHERE.equals(provider.getDataSphere(entity.getType()))) {
            neededRights = new HashSet<Right>(Arrays.asList(Right.ADMIN_ACCESS));
            allow = intersection(auths, neededRights);
        } else if (!isRead) {
            debug("Evaluating rights for write...");
            if (entity == null) {
                neededRights.addAll(provider.getAllFieldRights(entity.getType(), isRead));
                allow = intersection(auths, neededRights);
            } else {
                allow = determineWriteAccess(entity.getBody(), "", auths, entity.getType());
            }
        } else if (isRead) {
            debug("Evaluating rights for read...");
            neededRights.addAll(provider.getAllFieldRights(entity.getType(), isRead));
            allow = intersection(auths, neededRights);
        } else {
            throw new IllegalStateException("Unknown security validation path for Read/Write/Admin");
        }

        if (!allow) {
            throw new AccessDeniedException("Insufficient Privileges");
        }
    }

    /**
     * Checks query params for access restrictions
     *
     * @param query   The query to check
     * @param isSelf  whether operation is being done in "self" context
     * @param entity item under inspection
     */
    public void checkFieldAccess(NeutralQuery query, boolean isSelf, Entity entity) {

        if (query != null) {
            // get the authorities
            Collection<GrantedAuthority> auths = new HashSet<GrantedAuthority>();
            auths.addAll(getAuthorities(isSelf, entity));
            if (isSelf) {
                auths.addAll(SecurityUtil.getSLIPrincipal().getSelfRights());
            }

            if (!auths.contains(Right.FULL_ACCESS) && !auths.contains(Right.ANONYMOUS_ACCESS)) {
                for (NeutralCriteria criteria : query.getCriteria()) {
                    // get the needed rights for the field
                    Set<Right> neededRights = getNeededRights(criteria.getKey(), entity.getType());

                    if (!neededRights.isEmpty() && !intersection(auths, neededRights)) {
                        debug("Denied user searching on field {}", criteria.getKey());
                        throw new QueryParseException("Cannot search on restricted field", criteria.getKey());
                    }
                }
            }
        }
    }

    /**
     *  Get the authorities from the context
     *
     * @param isSelf  whether operation is being done in "self" context
     * @param entity item under inspection
     *
     * @return a set of granted authorities
     */
    public Collection<GrantedAuthority> getAuthorities(boolean isSelf, Entity entity){
        Collection<GrantedAuthority> auths = new HashSet<GrantedAuthority>();

        if (isSelf) {
            SLIPrincipal principal = SecurityUtil.getSLIPrincipal();
            auths.addAll(principal.getSelfRights());
        }

        if(entity == null) {
            debug("No authority for null");
        } else {
            SLIPrincipal principal = SecurityUtil.getSLIPrincipal();
            auths.addAll(entityEdOrgRightBuilder.buildEntityEdOrgRights(principal.getEdOrgRights(), entity));
        }

        return auths;
    }

    /**
     * Returns the needed right for a field by examining the schema
     *
     * @param fieldPath The field name
     *
     * @return a set of rights needed.
     */
    public Set<Right> getNeededRights(String fieldPath, String entityType) {
        Set<Right> neededRights = provider.getRequiredReadLevels(entityType, fieldPath);

        if (ADMIN_SPHERE.equals(provider.getDataSphere(entityType))) {
            neededRights.add(Right.ADMIN_ACCESS);
        }

        return neededRights;
    }

    /**
     * Determines if there is an intersection of a single needed right within the user's collection
     * of granted authorities.
     *
     * @param authorities  User's collection of granted authorities.
     * @param neededRights Set of rights needed for accessing a given field.
     * @return True if the user can access the field, false otherwise.
     */
    public boolean intersection(Collection<GrantedAuthority> authorities, Set<Right> neededRights) {
        return neededRights.isEmpty() || !Collections.disjoint(authorities, neededRights);
    }

    @SuppressWarnings("unchecked")
    private boolean determineWriteAccess(Map<String, Object> eb, String prefix, Collection<GrantedAuthority> auths, String entityType) {

        boolean allow = true;
        if (!ADMIN_SPHERE.equals(provider.getDataSphere(entityType))) {
            for (Map.Entry<String, Object> entry : eb.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                List<Object> list = null;
                if (value instanceof List) {
                    list = (List<Object>) value;
                } else {
                    list = Collections.singletonList(value);
                }

                for (Object obj : list) {
                    String fieldPath = prefix + fieldName;
                    Set<Right> neededRights = provider.getRequiredWriteLevels(entityType, fieldPath);

                    debug("Field {} requires {}", fieldPath, neededRights);

                    if (neededRights.isEmpty() && obj instanceof Map) {
                        allow &= determineWriteAccess((Map<String, Object>) obj, prefix + "." + fieldName + ".", auths, entityType);    // Mixing recursion and iteration, very bad
                    } else {
                        if (!intersection(auths, neededRights)) {
                            allow = false;
                            break;
                        }
                    }

                }
            }
        }

        return allow;
    }
}
