/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.deployment.common;

import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.security.common.Role;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * This interface defines the protocol used by the DOL to access the role
 * mapping information of a J2EE application. This class is implemented by
 * other modules and their instanciation is done through the
 * SecurityRoleMapperFactory class.
 *
 * @author Jerome Dochez
 */
public interface SecurityRoleMapper {

    /**
     * Set the role mapper application name
     * @param the app name
     */
    public void setName(String name);

    /**
     * @return the role mapper application name
     */
    public String getName();

    /**
     * @return an iterator on all the assigned roles
     */
    public Iterator getRoles();

    /**
     * @rturns an enumeration of Principals assigned to the given role
     * @param The Role to which the principals are assigned to.
     */
    public Enumeration getUsersAssignedTo(Role r);


    /**
     * Returns an enumeration of Groups assigned to the given role
     * @param The Role to which the groups are assigned to.
     */
    public Enumeration getGroupsAssignedTo(Role r);

    /**
     * Assigns a Principal to the specified role.
     *
     * @param p The principal that needs to be assigned to the role.
     * @param r The Role the principal is being assigned to.
     * @param rdd The descriptor of the module calling assignRole.
     */
    public void assignRole(Principal p, Role r, RootDeploymentDescriptor rdd);

    /**
     * Remove the given role-principal mapping
     * @param role, Role object
     * @param principal, the principal
     */
    public void unassignPrincipalFromRole(Role role, Principal principal);

    /**
     *  Remove all the role mapping information for this role
     * @param role, the role object
     */
    public void unassignRole(Role role);
    /*
     * @Map a map of roles to the corresponding subjects
     */
    public Map getRoleToSubjectMapping();
}
