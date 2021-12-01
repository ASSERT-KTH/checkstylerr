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

import org.slc.sli.api.exceptions.APIAccessDeniedException;
import org.slc.sli.api.security.context.resolver.EdOrgHelper;
import org.slc.sli.api.security.resolve.RolesToRightsResolver;
import org.slc.sli.common.constants.ParameterConstants;
import org.slc.sli.domain.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Build roles based on edOrg.
 * User: npandey ablum
 */
@Component
public class EdOrgContextualRoleBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(EdOrgContextualRoleBuilder.class);

    @Autowired
    private EdOrgHelper edorgHelper;

    @Autowired
    private RolesToRightsResolver resolver;

    /**
     * Builds a map of valid roles per associated edOrg
     * @param realmId id of the realm
     * @param staffId id of the staff
     * @param tenant  tenant name
     * @param roles   list of roles
     * @return a map of edOrg to list of roles
     */
    public Map<String, List<String>> buildValidStaffRoles(String realmId, String staffId, String tenant, List<String> roles) {
        Set<String> samlRoleSet = new HashSet<String>(roles);

        Set<Entity> staffEdOrgAssoc = edorgHelper.locateNonExpiredSEOAs(staffId);

        if (staffEdOrgAssoc.size() == 0) {
            LOG.error("Attempted login by a user that did not include any current valid roles in the SAML Assertion.");
            throw new APIAccessDeniedException("Invalid user.  User is not currently associated with any school/edorg", true);
        }

        Map<String, List<String>> sliEdOrgRoleMap = buildEdOrgContextualRoles(staffEdOrgAssoc, samlRoleSet);

        if(sliEdOrgRoleMap.isEmpty()) {
            LOG.error("Attempted login by a user that did not include any valid roles in the SAML Assertion.");
            throw new APIAccessDeniedException("Invalid user. No valid roles specified for user.", true);
        }

        for (Map.Entry<String, List<String>> entry : sliEdOrgRoleMap.entrySet()) {
            sliEdOrgRoleMap.put(entry.getKey(), getRoleNameList(resolver.mapRoles(tenant, realmId, entry.getValue(), false)));
        }

        if (isInValidRoleMap(sliEdOrgRoleMap)) {
            LOG.error("Attempted login by a user that included no roles in the SAML Assertion that mapped to any of the SLI roles.");
            throw new APIAccessDeniedException(
                    "Invalid user.  No valid role mappings exist for the roles specified in the SAML Assertion.", true);
        }

        return sliEdOrgRoleMap;
    }

    /**
     * Builds a map of the users roles for each associated edorg.
     * @param seoas set of seoa entities
     * @param  samlRoleSet set of roles from saml response
     * @return a map of edorg to roles
     */
    private Map<String, List<String>> buildEdOrgContextualRoles(Set<Entity> seoas, Set<String> samlRoleSet) {
        Map<String, List<String>> edOrgRoles = new HashMap<String, List<String>>();
        if (seoas != null) {
            for (Entity seoa : seoas) {
                String edOrgId = (String) seoa.getBody().get(ParameterConstants.EDUCATION_ORGANIZATION_REFERENCE);

                String role = (String) seoa.getBody().get(ParameterConstants.STAFF_EDORG_ASSOC_STAFF_CLASSIFICATION);

                if(isValidRole(role, samlRoleSet)) {
                    if (edOrgRoles.get(edOrgId) == null) {
                        edOrgRoles.put(edOrgId, new ArrayList<String>());
                        edOrgRoles.get(edOrgId).add(role);
                    } else if (!edOrgRoles.get(edOrgId).contains(role)) {
                        edOrgRoles.get(edOrgId).add(role);
                    }
                }
            }
        }
        return edOrgRoles;
    }

    private boolean isValidRole(String role, Set<String> samlRoleSet) {
        return samlRoleSet.contains(role);
    }

    private boolean isInValidRoleMap(Map<String, List<String>> roleMap) {
        boolean isEmpty = false;

        for (Map.Entry<String, List<String>> entry : roleMap.entrySet()) {
           if(entry.getValue().isEmpty()) {
               isEmpty = true;
           } else {
               isEmpty = false;
               break;
           }
        }
        return isEmpty;
    }

    private List<String> getRoleNameList(Set<Role> roleSet) {
        List<String> roles = new ArrayList<String>();
        for (Role role : roleSet) {
            roles.addAll(role.getName());
        }
        return roles;
    }
}
