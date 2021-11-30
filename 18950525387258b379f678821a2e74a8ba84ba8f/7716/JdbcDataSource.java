/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbcruntime.service;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class JdbcDataSource implements DataSource {
    private ResourceInfo resourceInfo;
    private PrintWriter logWriter;
    private int loginTimeout;

    public void setResourceInfo(ResourceInfo resourceInfo) throws ConnectorRuntimeException {
        validateResource(resourceInfo);
        this.resourceInfo = resourceInfo;
    }

    private void validateResource(ResourceInfo resourceInfo) throws ConnectorRuntimeException {
        ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();
        String jndiName = resourceInfo.getName();
        String suffix = ConnectorsUtil.getValidSuffix(jndiName);

        if(suffix != null){
            //Typically, resource is created without suffix. Try without suffix.
            String tmpJndiName = jndiName.substring(0, jndiName.lastIndexOf(suffix));
            if(resourcesUtil.getResource(tmpJndiName, resourceInfo.getApplicationName(),
                    resourceInfo.getModuleName(), JdbcResource.class) != null){
                return;
            }
        }

        if(resourcesUtil.getResource(resourceInfo, JdbcResource.class) == null){
            throw new ConnectorRuntimeException("Invalid resource : " + resourceInfo);
        }
    }

    public Connection getConnection() throws SQLException {
        return ConnectorRuntime.getRuntime().getConnection(resourceInfo);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return ConnectorRuntime.getRuntime().getConnection(resourceInfo, username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
       this.logWriter = out;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
       loginTimeout = seconds;
    }

    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }
    public boolean isWrapperFor(Class<?> iface) throws SQLException{
       throw new SQLException("Not supported operation");
    }
    public <T> T unwrap(Class<T> iface) throws SQLException{
       throw new SQLException("Not supported operation");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not supported operation");
    }
}
