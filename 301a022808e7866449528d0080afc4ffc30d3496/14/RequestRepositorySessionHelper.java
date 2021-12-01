/**
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.repository.core.impl;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.eclipse.vorto.repository.core.FatalModelRepositoryException;
import org.eclipse.vorto.repository.core.UserLoginException;
import org.eclipse.vorto.repository.core.WorkspaceNotFoundException;
import org.eclipse.vorto.repository.core.security.SpringSecurityCredentials;
import org.eclipse.vorto.repository.domain.IRole;
import org.eclipse.vorto.repository.services.PrivilegeService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.jcr.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequestScope
public class RequestRepositorySessionHelper implements DisposableBean, InitializingBean {

    private static final Logger myLogger = Logger.getLogger(RequestRepositorySessionHelper.class);

    private Map<String, Session> repositorySessionMap;
    private String workspaceId;
    private Authentication authentication;
    private Repository repository;
    private Collection<IRole> roles;
    private Supplier<Session> internalSessionSupplier;

    @Autowired
    private PrivilegeService privilegeService;

    public RequestRepositorySessionHelper() {
        this(true, null);
    }

    public RequestRepositorySessionHelper(boolean isAutowired, PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
        if(isAutowired) {
            internalSessionSupplier = () -> {
                try {
                    return getSessionInternal(workspaceId, authentication);
                } catch (LoginException e) {
                    throw new UserLoginException(authentication.getName(), e);
                } catch (NoSuchWorkspaceException e) {
                    throw new WorkspaceNotFoundException(workspaceId, e);
                } catch (RepositoryException e) {
                    throw new FatalModelRepositoryException("Error while getting repository given workspace ID ["
                            + workspaceId + "] and user [" + authentication.getName() + "]", e);
                }
            };
        } else {
            internalSessionSupplier = () -> {
                try {
                    return login(workspaceId, authentication);
                } catch (RepositoryException e) {
                    throw new FatalModelRepositoryException("Error while getting repository given workspace ID ["
                            + workspaceId + "] and user [" + authentication.getName() + "]", e);
                }
            };
        }
    }

    @Override
    public void destroy() {
        myLogger.debug("destroy");
        if (this.repositorySessionMap == null)
            return;
        logoutAssociatedRepositorySessions();
        this.repositorySessionMap = null;
    }

    private void logoutAssociatedRepositorySessions() {
        for (Map.Entry<String, Session> entry : this.repositorySessionMap.entrySet()) {
            myLogger.debug("logging out session: " + entry.getValue().getUserID() +
                    " session live: " + entry.getValue().isLive());
            entry.getValue().logout();
        }
    }

    @Override
    public void afterPropertiesSet() {
        myLogger.debug("afterPropertiesSet: reinit session map");
        this.repositorySessionMap = new HashMap<>();
    }

    public Session getSession() {
        return internalSessionSupplier.get();
    }

    private synchronized Session getSessionInternal(String workspaceId, Authentication user) throws RepositoryException {
        Session mySession;
        mySession = this.repositorySessionMap.get(workspaceId);
        if (mySession == null || !mySession.isLive()) {
            mySession = login(workspaceId, user);
            this.repositorySessionMap.put(workspaceId, mySession);
        }
        return mySession;
    }

    private Session login(String workspaceId, Authentication user) throws RepositoryException {
        return repository.login(new SpringSecurityCredentials(user, roles, privilegeService), workspaceId);
    }

    public void logoutSessionIfNotReusable(Session session) {
        // if the session is in the session map - do not logout the session. It will be logged out
        //  after the request is finished.
        if (this.repositorySessionMap != null && this.repositorySessionMap.get(workspaceId) != null)
            return;
        session.logout();
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setUserRoles(Collection<IRole> userRoles) {
        this.roles = userRoles;
    }
}
