/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.common.DataSourceSpec;
import com.sun.gjc.spi.base.AbstractDataSource;
import com.sun.gjc.util.SecurityUtils;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.security.PasswordCredential;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionDefinition;

/**
 * XA <code>ManagedConnectionFactory</code> implementation for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/27
 */
@ConnectionDefinition(
    connectionFactory = javax.sql.DataSource.class,
    connectionFactoryImpl = AbstractDataSource.class,
    connection = java.sql.Connection.class,
    connectionImpl = com.sun.gjc.spi.base.ConnectionHolder.class
)
public class XAManagedConnectionFactory extends ManagedConnectionFactoryImpl {

    private transient javax.sql.XADataSource xaDataSourceObj;

    private static Logger _logger;

    static {
        _logger = LogDomains.getLogger(XAManagedConnectionFactory.class, LogDomains.RSR_LOGGER);
    }

    /**
     * Creates a new physical connection to the underlying EIS resource
     * manager.
     *
     * @param subject       <code>Subject</code> instance passed by the application server
     * @param cxRequestInfo <code>ConnectionRequestInfo</code> which may be created
     *                      as a result of the invocation <code>getConnection(user, password)</code>
     *                      on the <code>DataSource</code> object
     * @return <code>ManagedConnection</code> object created
     * @throws ResourceException           if there is an error in instantiating the
     *                                     <code>DataSource</code> object used for the
     *                                     creation of the <code>ManagedConnection</code> object
     * @throws SecurityException           if there ino <code>PasswordCredential</code> object
     *                                     satisfying this request
     * @throws ResourceAllocationException if there is an error in allocating the
     *                                     physical connection
     */
    public jakarta.resource.spi.ManagedConnection createManagedConnection(javax.security.auth.Subject subject,
                                                                        ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        logFine("In createManagedConnection");
        PasswordCredential pc = SecurityUtils.getPasswordCredential(this, subject, cxRequestInfo);

        javax.sql.XADataSource dataSource = getDataSource();

        javax.sql.XAConnection xaConn = null;
        ManagedConnectionImpl mc = null;

        try {
            /* For the case where the user/passwd of the connection pool is
            * equal to the PasswordCredential for the connection request
            * get a connection from this pool directly.
            * for all other conditions go create a new connection
            */
            if (isEqual(pc, getUser(), getPassword())) {
                xaConn = dataSource.getXAConnection();
            } else {
                xaConn = dataSource.getXAConnection(pc.getUserName(),
                        new String(pc.getPassword()));
            }


        } catch (java.sql.SQLException sqle) {
            //_logger.log(Level.WARNING, "jdbc.exc_create_xa_conn",sqle);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "jdbc.exc_create_xa_conn", sqle);
            }
            StringManager sm = StringManager.getManager(
                    DataSourceObjectBuilder.class);
            String msg = sm.getString("jdbc.cannot_allocate_connection", sqle.getMessage());
            throw new ResourceAllocationException(msg, sqle);
        }

        try{
            mc = constructManagedConnection(
                    xaConn, null, pc, this);

            mc.initializeConnectionType(ManagedConnectionImpl.ISXACONNECTION);
            //GJCINT
            validateAndSetIsolation(mc);
        } finally {
            if (mc == null) {
                if (xaConn != null) {
                    try {
                        xaConn.close();
                    } catch (SQLException e) {
                        _logger.log(Level.FINEST, "Exception while closing connection : createManagedConnection" + xaConn);
                    }
                }
            }
        }
        return mc;
    }

    /**
     * Returns the underlying datasource
     *
     * @return DataSource of jdbc vendor
     * @throws ResourceException
     */
    public javax.sql.XADataSource getDataSource() throws ResourceException {
        if (xaDataSourceObj == null) {
            try {
                xaDataSourceObj = (javax.sql.XADataSource) super.getDataSource();
            } catch (ClassCastException cce) {
                _logger.log(Level.SEVERE, "jdbc.exc_cce_XA", cce);
                throw new ResourceException(cce.getMessage());
            }
        }
        return xaDataSourceObj;
    }

    /**
     * Check if this <code>ManagedConnectionFactory</code> is equal to
     * another <code>ManagedConnectionFactory</code>.
     *
     * @param other <code>ManagedConnectionFactory</code> object for checking equality with
     * @return true    if the property sets of both the
     *         <code>ManagedConnectionFactory</code> objects are the same
     *         false    otherwise
     */
    public boolean equals(Object other) {
        logFine("In equals");
        /**
         * The check below means that two ManagedConnectionFactory objects are equal
         * if and only if their properties are the same.
         */
        if (other instanceof com.sun.gjc.spi.XAManagedConnectionFactory) {
            com.sun.gjc.spi.XAManagedConnectionFactory otherMCF =
                    (com.sun.gjc.spi.XAManagedConnectionFactory) other;
            return this.spec.equals(otherMCF.spec);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * 7 + (spec.hashCode());
    }

    /**
     * Sets the class name of the data source
     *
     * @param className <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "org.apache.derby.jdbc.ClientXADataSource")
    @Override
    public void setClassName(String className) {
        spec.setDetail(DataSourceSpec.CLASSNAME, className);
    }

    /**
     * Sets the max statements.
     *
     * @param maxStmts <code>String</code>
     * @see <code>getMaxStatements</code>
     */
    public void setMaxStatements(String maxStmts) {
        spec.setDetail(DataSourceSpec.MAXSTATEMENTS, maxStmts);
    }

    /**
     * Gets the max statements.
     *
     * @return maxStmts
     * @see <code>setMaxStatements</code>
     */
    public String getMaxStatements() {
        return spec.getDetail(DataSourceSpec.MAXSTATEMENTS);
    }

    /**
     * Sets the initial pool size.
     *
     * @param initPoolSz <code>String</code>
     * @see <code>getInitialPoolSize</code>
     */
    public void setInitialPoolSize(String initPoolSz) {
        spec.setDetail(DataSourceSpec.INITIALPOOLSIZE, initPoolSz);
    }

    /**
     * Gets the initial pool size.
     *
     * @return initPoolSz
     * @see <code>setInitialPoolSize</code>
     */
    public String getInitialPoolSize() {
        return spec.getDetail(DataSourceSpec.INITIALPOOLSIZE);
    }

    /**
     * Sets the minimum pool size.
     *
     * @param minPoolSz <code>String</code>
     * @see <code>getMinPoolSize</code>
     */
    public void setMinPoolSize(String minPoolSz) {
        spec.setDetail(DataSourceSpec.MINPOOLSIZE, minPoolSz);
    }

    /**
     * Gets the minimum pool size.
     *
     * @return minPoolSz
     * @see <code>setMinPoolSize</code>
     */
    public String getMinPoolSize() {
        return spec.getDetail(DataSourceSpec.MINPOOLSIZE);
    }

    /**
     * Sets the maximum pool size.
     *
     * @param maxPoolSz <code>String</code>
     * @see <code>getMaxPoolSize</code>
     */
    public void setMaxPoolSize(String maxPoolSz) {
        spec.setDetail(DataSourceSpec.MAXPOOLSIZE, maxPoolSz);
    }

    /**
     * Gets the maximum pool size.
     *
     * @return maxPoolSz
     * @see <code>setMaxPoolSize</code>
     */
    public String getMaxPoolSize() {
        return spec.getDetail(DataSourceSpec.MAXPOOLSIZE);
    }

    /**
     * Sets the maximum idle time.
     *
     * @param maxIdleTime String
     * @see <code>getMaxIdleTime</code>
     */
    public void setMaxIdleTime(String maxIdleTime) {
        spec.setDetail(DataSourceSpec.MAXIDLETIME, maxIdleTime);
    }

    /**
     * Gets the maximum idle time.
     *
     * @return maxIdleTime
     * @see <code>setMaxIdleTime</code>
     */
    public String getMaxIdleTime() {
        return spec.getDetail(DataSourceSpec.MAXIDLETIME);
    }

    /**
     * Sets the property cycle.
     *
     * @param propCycle <code>String</code>
     * @see <code>getPropertyCycle</code>
     */
    public void setPropertyCycle(String propCycle) {
        spec.setDetail(DataSourceSpec.PROPERTYCYCLE, propCycle);
    }

    /**
     * Gets the property cycle.
     *
     * @return propCycle
     * @see <code>setPropertyCycle</code>
     */
    public String getPropertyCycle() {
        return spec.getDetail(DataSourceSpec.PROPERTYCYCLE);
    }
}
