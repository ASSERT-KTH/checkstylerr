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

package org.glassfish.jdbcruntime.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.service.ConnectorAdminServicesFactory;
import com.sun.enterprise.connectors.service.ConnectorConnectionPoolAdminServiceImpl;
import com.sun.enterprise.connectors.service.ConnectorService;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.DriverLoader;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;
import javax.naming.NamingException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.logging.Level;

/**
 * Jdbc admin service performs Jdbc related operations for administration.
 *
 * @author shalini
 */
@Service
@Singleton
public class JdbcAdminServiceImpl extends ConnectorService {

    private ConnectorConnectionPoolAdminServiceImpl ccPoolAdmService;
    private static final String DBVENDOR_MAPPINGS_ROOT =
            System.getProperty(ConnectorConstants.INSTALL_ROOT) + File.separator +
            "lib" + File.separator + "install" + File.separator + "databases" +
            File.separator + "dbvendormapping" + File.separator;
    private final static String JDBC40_CONNECTION_VALIDATION =
            "org.glassfish.api.jdbc.validation.JDBC40ConnectionValidation";
    private final static String DS_PROPERTIES = "ds.properties";
    private final static String CPDS_PROPERTIES = "cpds.properties";
    private final static String XADS_PROPERTIES = "xads.properties";
    private final static String DRIVER_PROPERTIES = "driver.properties";
    private final static String CONVAL_PROPERTIES = "validationclassnames.properties";

    private static JdbcAdminServiceImpl jdbcAdminService = new JdbcAdminServiceImpl();

    /**
     * Default constructor
     */
    public JdbcAdminServiceImpl() {
        super();
        //jdbcAdminService = this;
        ccPoolAdmService = (ConnectorConnectionPoolAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.CCP);
    }

    public static JdbcAdminServiceImpl getJdbcAdminService() {
        if (jdbcAdminService == null) {
            throw new RuntimeException("JDBC admin service not initialized");
        }
        return jdbcAdminService;
    }

    /**
     * Get Validation class names list for the classname that the jdbc
     * connection pool refers to. This is used for custom connection validation.
     * @param className
     * @return all validation class names.
     */
    public Set<String> getValidationClassNames(String className) {
        SortedSet classNames = new TreeSet();
        if(className == null) {
            _logger.log(Level.WARNING, "jdbc.admin.service.ds_class_name_null");
            return classNames;
        }
        File validationClassMappingFile;
        String dbVendor = getDatabaseVendorName(className);

        //Retrieve validation classnames from the properties file based on the retrieved
        //dbvendor name
        if (dbVendor != null) {
            validationClassMappingFile = new File(DBVENDOR_MAPPINGS_ROOT + CONVAL_PROPERTIES);
            Properties validationClassMappings = DriverLoader.loadFile(validationClassMappingFile);
            String validationClassName = validationClassMappings.getProperty(dbVendor);
            if (validationClassName != null) {
                classNames.add(validationClassName);
            }
            //If JDBC40 runtime, add the jdbc40 validation classname
            if (detectJDBC40(className)) {
                classNames.add(JDBC40_CONNECTION_VALIDATION);
            }
        }
        return classNames;
    }

    private String getDatabaseVendorName(String className) {
        String dbVendor = getDatabaseVendorName(DriverLoader.loadFile(
                new File(DBVENDOR_MAPPINGS_ROOT + DS_PROPERTIES)), className);
        if(dbVendor == null) {
            dbVendor = getDatabaseVendorName(DriverLoader.loadFile(
                new File(DBVENDOR_MAPPINGS_ROOT + CPDS_PROPERTIES)), className);
        }
        if(dbVendor == null) {
            dbVendor = getDatabaseVendorName(DriverLoader.loadFile(
                new File(DBVENDOR_MAPPINGS_ROOT + XADS_PROPERTIES)), className);
        }
        if(dbVendor == null) {
            dbVendor = getDatabaseVendorName(DriverLoader.loadFile(
                new File(DBVENDOR_MAPPINGS_ROOT + DRIVER_PROPERTIES)), className);
        }
        return dbVendor;
    }

    private String getDatabaseVendorName(Properties classNameProperties, String className) {
        String dbVendor = null;
        Enumeration e = classNameProperties.propertyNames();
        while(e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = classNameProperties.getProperty(key);
            if(className.equalsIgnoreCase(value)){
                //There could be multiple keys for a particular value.
                dbVendor = key;
                break;
            }
        }
        return dbVendor;
    }

    private boolean detectJDBC40(String className) {
        boolean jdbc40 = true;
        ClassLoader commonClassLoader =
                ConnectorRuntime.getRuntime().getClassLoaderHierarchy().getCommonClassLoader();
        Class cls = null;
        try {
            cls = commonClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            return false;
        }
        Method method;
        try {
            method = cls.getMethod("isWrapperFor", Class.class);
            method.invoke(cls.newInstance(), javax.sql.DataSource.class);
        } catch (NoSuchMethodException e) {
            jdbc40 = false;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
        } catch (InvocationTargetException e) {
            if(e.getCause() instanceof AbstractMethodError) {
                jdbc40 = false;
            }
        } catch (InstantiationException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            jdbc40 = false;
        } catch (IllegalAccessException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "jdbc.admin.service.ex_detect_jdbc40");
            }
            jdbc40 = false;
        }
        return jdbc40;
    }

    /**
     * Get Validation table names list for the database that the jdbc
     * connection pool refers to. This is used for connection validation.
     * @param poolInfo
     * @return all validation table names.
     * @throws jakarta.resource.ResourceException
     * @throws javax.naming.NamingException
     */
    public Set<String> getValidationTableNames(PoolInfo poolInfo)
            throws ResourceException {
        ManagedConnectionFactory mcf = null;
        ManagedConnection mc = null;
        java.sql.Connection con = null;
        try {
            mc = (ManagedConnection) ccPoolAdmService.getUnpooledConnection(poolInfo, null, false);
            mcf = ccPoolAdmService.obtainManagedConnectionFactory(poolInfo);

            if (mc != null) {
                con = (java.sql.Connection) mc.getConnection(null, null);
            }
            return getValidationTableNames(con,
                    getDefaultDatabaseName(poolInfo, mcf));
        } catch(Exception re) {
            _logger.log(Level.WARNING, "pool.get_validation_table_names_failure", re.getMessage());
            throw new ResourceException(re);
        } finally {
            try {
                if(mc != null) {
                    mc.destroy();
                }
            } catch(Exception ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "pool.get_validation_table_names_mc_destroy", poolInfo);
                }
            }
        }
    }

    /**
     * Returns a databaseName that is populated in pool's default DATABASENAME
     * @param poolInfo
     * @param mcf
     * @return
     * @throws javax.naming.NamingException if poolName lookup fails
     */
    private String getDefaultDatabaseName(PoolInfo poolInfo, ManagedConnectionFactory mcf)
            throws NamingException {
        // All this to get the default user name and principal
        String databaseName = null;
        ConnectorConnectionPool connectorConnectionPool = null;
        try {
            String jndiNameForPool = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool(poolInfo);
            connectorConnectionPool = (ConnectorConnectionPool)
                    _runtime.getResourceNamingService().
                            lookup(poolInfo, jndiNameForPool, null);
        } catch (NamingException ne) {
            throw ne;
        }

        databaseName = ccPoolAdmService.getPropertyValue("DATABASENAME", connectorConnectionPool);

        // To avoid using "" as the default databasename, try to get
        // the databasename from MCF.
        if (databaseName == null || databaseName.trim().equals("")) {
            databaseName = ConnectionPoolObjectsUtils.getValueFromMCF("DatabaseName", poolInfo, mcf);
        }
        return databaseName;
    }

    /**
     * Get Validation table names list for the catalog that the jdbc
     * connection pool refers to. This is used for connection validation.
     * @param con
     * @param catalog database name used.
     * @return
     * @throws jakarta.resource.ResourceException
     */
    public static Set<String> getValidationTableNames(java.sql.Connection con, String catalog)
            throws ResourceException {


        SortedSet<String> tableNames = new TreeSet();
        if(catalog.trim().equals("")) {
            catalog = null;
        }

        if (con != null) {
            java.sql.ResultSet rs = null;
            try {
                DatabaseMetaData dmd = con.getMetaData();
                rs = dmd.getTables(catalog, null, null, null);
                while(rs.next()) {
                    String schemaName = rs.getString(2);
                    String tableName = rs.getString(3);
                    String actualTableName = tableName;
                    if(schemaName != null && !schemaName.equals("")){
                        actualTableName = schemaName + "." + tableName;
                    }
                    tableNames.add(actualTableName);
                }
            } catch (Exception sqle) {
                _logger.log(Level.INFO, "pool.get_validation_table_names");
                throw new ResourceException(sqle);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (Exception e1) {}
            }
        } else {
            throw new ResourceException("The connection is not valid as "
                    + "the connection is null");
        }
        return tableNames;
    }

    /**
     * Utility method to check if the retrieved table is accessible, since this
     * will be used for connection validation method "table".
     * @param tableName
     * @param con
     * @return accessibility status of the table.
     */
    /*private static boolean isPingable(String tableName, java.sql.Connection con) {
        java.sql.PreparedStatement stmt = null;
        java.sql.ResultSet rs = null;
        final String sql = "SELECT COUNT(*) FROM " + tableName;
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (Exception sqle) {
            _logger.log(Level.INFO, "pool.exc_is_pingable", tableName);
            return false;

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e1) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e2) {
            }
        }
        return true;
    } */
}
