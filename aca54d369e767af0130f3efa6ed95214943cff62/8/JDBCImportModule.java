/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.jdbc.in;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.data.ArrayCell;
import com.databasepreservation.model.data.BinaryCell;
import com.databasepreservation.model.data.Cell;
import com.databasepreservation.model.data.NullCell;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.data.SimpleCell;
import com.databasepreservation.model.exception.InvalidDataException;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.SQLParseException;
import com.databasepreservation.model.exception.TableNotFoundException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatatypeImporter;
import com.databasepreservation.model.modules.ModuleSettings;
import com.databasepreservation.model.structure.CandidateKey;
import com.databasepreservation.model.structure.CheckConstraint;
import com.databasepreservation.model.structure.ColumnStructure;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.ForeignKey;
import com.databasepreservation.model.structure.PrimaryKey;
import com.databasepreservation.model.structure.PrivilegeStructure;
import com.databasepreservation.model.structure.Reference;
import com.databasepreservation.model.structure.RoleStructure;
import com.databasepreservation.model.structure.RoutineStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.model.structure.Trigger;
import com.databasepreservation.model.structure.UserStructure;
import com.databasepreservation.model.structure.ViewStructure;
import com.databasepreservation.model.structure.type.ComposedTypeArray;
import com.databasepreservation.model.structure.type.ComposedTypeStructure;
import com.databasepreservation.model.structure.type.SimpleTypeBinary;
import com.databasepreservation.model.structure.type.SimpleTypeBoolean;
import com.databasepreservation.model.structure.type.SimpleTypeDateTime;
import com.databasepreservation.model.structure.type.SimpleTypeNumericApproximate;
import com.databasepreservation.model.structure.type.SimpleTypeNumericExact;
import com.databasepreservation.model.structure.type.Type;
import com.databasepreservation.model.structure.type.UnsupportedDataType;
import com.databasepreservation.modules.DefaultExceptionNormalizer;
import com.databasepreservation.modules.SQLHelper;
import com.databasepreservation.utils.ConfigUtils;
import com.databasepreservation.utils.JodaUtils;
import com.databasepreservation.utils.MiscUtils;
import com.databasepreservation.utils.RemoteConnectionUtils;
import com.jcraft.jsch.Session;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class JDBCImportModule implements DatabaseImportModule {

  private final String VIEW_NAME_PREFIX = "VIEW_";
  private final String CUSTOM_VIEW_NAME_PREFIX = "CUSTOM_VIEW_";
  // if fetch size is zero, then the driver decides the best fetch size
  protected static final Integer DEFAULT_ROW_FETCH_BLOCK_SIZE = ConfigUtils.getProperty(0,
    "dbptk.jdbc.fetchsize.default");
  protected static final Integer SMALL_ROW_FETCH_BLOCK_SIZE = ConfigUtils.getProperty(10, "dbptk.jdbc.fetchsize.small");
  protected static final Integer MINIMUM_ROW_FETCH_BLOCK_SIZE = ConfigUtils.getProperty(1,
    "dbptk.jdbc.fetchsize.minimum");

  protected static final String DEFAULT_DATA_TIMESPAN = "(...)";

  protected static final boolean IGNORE_UNSUPPORTED_DATA_TYPES = true;
  protected final String driverClassName;
  protected String connectionURL;
  private static final Logger LOGGER = LoggerFactory.getLogger(JDBCImportModule.class);
  protected Connection connection;
  protected Session session = null;

  protected Statement statement;

  protected DatabaseMetaData dbMetadata;

  protected DatabaseStructure dbStructure;

  // the schema object being built
  protected SchemaStructure actualSchema;

  protected SQLHelper sqlHelper;

  protected DatatypeImporter datatypeImporter;

  private ModuleSettings moduleSettings;

  protected Reporter reporter;

  private Map<String, Map<String, Map<String, String>>> customViews = new HashMap<>();

  private String customViewsPath;

  // SSH Connection Parameters
  private final boolean ssh;

  /**
   * Create a new JDBC import module
   *
   * @param driverClassName
   *          the name of the the JDBC driver class
   * @param connectionURL
   *          the connection url to use in the connection
   */
  public JDBCImportModule(String driverClassName, String connectionURL) {
    this(driverClassName, connectionURL, new SQLHelper(), new JDBCDatatypeImporter());
  }

  protected JDBCImportModule(String driverClassName, String connectionURL, SQLHelper sqlHelper,
    DatatypeImporter datatypeImporter) {
    this.driverClassName = driverClassName;
    this.connectionURL = connectionURL;
    this.sqlHelper = sqlHelper;
    this.datatypeImporter = datatypeImporter;
    connection = null;
    dbMetadata = null;
    dbStructure = null;
    ssh = false;
  }

  protected JDBCImportModule(String driverClassName, String connectionURL, SQLHelper sqlHelper,
    DatatypeImporter datatypeImporter, Path queryList) throws ModuleException {
    this.driverClassName = driverClassName;
    this.connectionURL = connectionURL;
    this.sqlHelper = sqlHelper;
    this.datatypeImporter = datatypeImporter;
    connection = null;
    dbMetadata = null;
    dbStructure = null;
    ssh = false;
    if (queryList != null) {
      customViews = parseCustomViewsList(queryList);
      customViewsPath = queryList.toAbsolutePath().toString();
    }
  }

  protected JDBCImportModule(String driverClassName, String connectionURL, SQLHelper sqlHelper,
    DatatypeImporter datatypeImporter, boolean ssh, String sshHost, String sshUser, String sshPassword,
    String sshPortNumber, Path queryList) throws ModuleException {
    this.driverClassName = driverClassName;
    this.connectionURL = connectionURL;
    this.sqlHelper = sqlHelper;
    this.datatypeImporter = datatypeImporter;
    connection = null;
    dbMetadata = null;
    dbStructure = null;
    this.ssh = ssh;
    if (queryList != null) {
      customViews = parseCustomViewsList(queryList);
      customViewsPath = queryList.toAbsolutePath().toString();
    }
    if (ssh) {
      final Session remoteSession = RemoteConnectionUtils.createRemoteSession(sshHost, sshUser, sshPassword, sshPortNumber, connectionURL);
    }
  }

  /**
   * Connect to the server using the properties defined in the constructor, or
   * return the existing connection
   *
   * @return the connection
   * @throws SQLException
   *           the JDBC driver could not be found in classpath
   */
  public Connection getConnection() throws ModuleException {
    if (connection == null) {
      LOGGER.debug("Loading JDBC Driver " + driverClassName);
      try {
        Class.forName(driverClassName);
      } catch (ClassNotFoundException e) {
        throw normalizeException(e, "Could not find SQL driver class: " + driverClassName);
      }
      LOGGER.debug("Getting connection");


      connection = createConnection();
      datatypeImporter.setConnection(connection);
    }
    return connection;
  }

  /**
   * Connect to the server using the properties defined in the constructor
   * 
   * @return the new connection
   * @throws ModuleException
   */
  protected Connection createConnection() throws ModuleException {
    Connection connection;
    try {
      if (ssh) {
        connectionURL = RemoteConnectionUtils.replaceHostAndPort(connectionURL);
      }
      connection = DriverManager.getConnection(connectionURL);
    } catch (SQLException e) {
      throw normalizeException(e, null);
    }
    LOGGER.debug("Connected");
    return connection;
  }

  protected Statement getStatement() throws SQLException, ModuleException {
    if (statement == null) {
      statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
        ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }
    return statement;
  }

  /**
   * Get the database metadata
   *
   * @return the database metadata
   * @throws SQLException
   */
  public DatabaseMetaData getMetadata() throws SQLException, ModuleException {
    if (dbMetadata == null) {
      dbMetadata = getConnection().getMetaData();
    }
    return dbMetadata;
  }

  public boolean testConnection() throws ModuleException {
    try {
      return (!getConnection().isClosed() && connection != null);
    } catch (SQLException e) {
      throw normalizeException(e, null);
    }
  }

  public DatabaseStructure getSchemaInformation() throws ModuleException {
    moduleSettings = new ModuleSettings();
    getDatabaseStructure();
    return dbStructure;
  }

  /**
   * Close current connection
   *
   * @throws SQLException
   */
  public void closeConnection() throws ModuleException {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        LOGGER.debug("problem closing statement", e);
      }
    }

    Connection connection = null;
    connection = getConnection();
    if (connection != null) {
      try {
        if (session != null)
          session.disconnect();
        connection.close();
      } catch (SQLException e) {
        LOGGER.debug("problem closing connection", e);
      }
    }
    this.connection = null;
    dbMetadata = null;
    dbStructure = null;
  }

  /**
   * Some driver may not report correctly (due to cursor setup, etc) the number of
   * the row currently being processed (ResultSet.getRow).
   * <p>
   * If its known that a particular import module doesn't support it, re-implement
   * this method in that particular module to return false
   *
   * @return true if ResultSet.getRow reports correctly the number of the row
   *         being processed; false otherwise
   */
  protected boolean isGetRowAvailable() {
    return true;
  }

  /**
   * @return the database structure
   * @throws SQLException
   */
  protected DatabaseStructure getDatabaseStructure() throws ModuleException {
    if (dbStructure == null) {
      dbStructure = new DatabaseStructure();
      try {
        LOGGER.debug("driver version: {}", getMetadata().getDriverVersion());
        dbStructure.setName(getDatabaseName());
        dbStructure.setDescription(getDatabaseDescription(dbStructure.getName()));
        dbStructure.setProductName(getMetadata().getDatabaseProductName());
        dbStructure.setProductVersion(getMetadata().getDatabaseProductVersion());
        dbStructure.setDataOwner(System.getProperty("user.name"));
        dbStructure.setDataOriginTimespan(DEFAULT_DATA_TIMESPAN);
        dbStructure.setProducerApplication(MiscUtils.APP_NAME_AND_VERSION);
        String clientMachine = "";
        try {
          clientMachine = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
          LOGGER.debug("UnknownHostException", e);
        }
        dbStructure.setClientMachine(clientMachine);

        dbStructure.setSchemas(getSchemas());
        actualSchema = null;

        dbStructure.setUsers(getUsers(dbStructure.getName()));
        dbStructure.setRoles(getRoles());
        dbStructure.setPrivileges(getPrivileges());

        LOGGER.debug("Database structure obtained");
      } catch (SQLException e) {
        throw normalizeException(e, null);
      }
    }
    return dbStructure;
  }

  protected String getDatabaseDescription(String name) throws ModuleException {
    return null;
  }

  protected String getDatabaseName() throws SQLException, ModuleException {
    return getConnection().getCatalog();
  }

  /**
   * Checks if schema name matches the set of schemas to be ignored.
   *
   * @param schemaName
   *          the schema name
   * @return true if schema is ignored; false if it isn't
   */
  protected boolean isIgnoredImportedSchema(String schemaName) {
    boolean ignoredSchema = false;
    for (String s : getIgnoredImportedSchemas()) {
      if (schemaName.matches(s)) {
        ignoredSchema = true;
      }
    }
    return ignoredSchema;
  }

  /**
   * @return the database schemas (not ignored by default and/or user) @throws
   *         SQLException @throws
   */
  protected List<SchemaStructure> getSchemas() throws SQLException, ModuleException {
    List<SchemaStructure> schemas = new ArrayList<SchemaStructure>();

    try (ResultSet rs = getMetadata().getSchemas()) {
      int schemaIndex = 1;
      while (rs.next()) {
        String schemaName = rs.getString(1);
        // does not import ignored schemas
        if (isIgnoredImportedSchema(schemaName)) {
          continue;
        }
        schemas.add(getSchemaStructure(schemaName, schemaIndex));
        schemaIndex++;
      }
    }
    return schemas;
  }

  /**
   * Get schemas that won't be imported
   * <p>
   * Accepts schemas names in as regular expressions I.e. SYS.* will ignore
   * SYSCAT, SYSFUN, etc
   *
   * @return the schema names not to be imported
   */
  protected Set<String> getIgnoredImportedSchemas() {
    HashSet ignore = new HashSet<String>();
    ignore.add("information_schema");
    ignore.add("pg_catalog");
    return ignore;
  }

  /**
   * @param schemaName
   *          the schema name
   * @return the schema structure of a given schema name
   * @throws ModuleException
   */
  protected SchemaStructure getSchemaStructure(String schemaName, int schemaIndex)
    throws SQLException, ModuleException {
    actualSchema = new SchemaStructure();
    actualSchema.setName(schemaName);
    actualSchema.setIndex(schemaIndex);
    // actualSchema.setUserDefinedTypes(getUDTs(actualSchema));
    actualSchema.setUserDefinedTypesComposed(new ArrayList<ComposedTypeStructure>());
    actualSchema.setTables(getTables(actualSchema));
    actualSchema.setViews(getViews(schemaName));
    actualSchema.setRoutines(getRoutines(schemaName));

    return actualSchema;
  }

  protected ArrayList<ComposedTypeStructure> getUDTs(SchemaStructure schema) throws SQLException, ModuleException {
    // possibleUDT because it may also be a table name, which in some cases may
    // also be used as a type
    ArrayList<String> possibleUDTs = new ArrayList<>();

    try (ResultSet udtTypes = getMetadata().getUDTs(dbStructure.getName(), schema.getName(), null, null)) {
      while (udtTypes.next()) {
        int dataType = udtTypes.getInt(5);
        if (dataType == Types.STRUCT) {
          possibleUDTs.add(udtTypes.getString(3));
        } else {
          StringBuilder debug = new StringBuilder();

          // 1. TYPE_CAT String => the type's catalog (may be null)
          debug.append("\nTYPE_CAT: ").append(udtTypes.getString(1));
          // 2. TYPE_SCHEM String => type's schema (may be null)
          debug.append("\nTYPE_SCHEM: ").append(udtTypes.getString(2));
          // 3. TYPE_NAME String => type name
          debug.append("\nTYPE_NAME: ").append(udtTypes.getString(3));
          // 4. CLASS_NAME String => Java class name
          debug.append("\nCLASS_NAME: ").append(udtTypes.getString(4)).append("\n");
          // 5. DATA_TYPE int => type value defined in java.sql.Types. One of
          // JAVA_OBJECT, STRUCT, or DISTINCT
          switch (dataType) {
            case Types.JAVA_OBJECT:
              debug.append("DATA_TYPE: JAVA_OBJECT");
              break;
            case Types.STRUCT:
              debug.append("DATA_TYPE: STRUCT");
              break;
            case Types.DISTINCT:
              debug.append("DATA_TYPE: DISTINCT");
              break;
            default:
              debug.append("DATA_TYPE: " + dataType + "(unknown)");
          }
          // 6. REMARKS String => explanatory comment on the type
          debug.append("\nREMARKS: ").append(udtTypes.getString(6));
          /*
           * 7. BASE_TYPE short => type code of the source type of a DISTINCT type or the
           * type that implements the user-generated reference type of the
           * SELF_REFERENCING_COLUMN of a structured type as defined in java.sql.Types
           * (null if DATA_TYPE is not DISTINCT or not STRUCT with REFERENCE_GENERATION =
           * USER_DEFINED)
           */
          debug.append("\nBASE_TYPE: ").append(udtTypes.getShort(7));
          LOGGER.debug("Possible UDT is not a STRUCT. " + debug.toString());
          LOGGER.debug("Unsupported UDT found: " + debug.toString());
        }
      }
    }

    ArrayList<ComposedTypeStructure> udts = new ArrayList<>();
    for (String possibleUDT : possibleUDTs) {
      ComposedTypeStructure type = new ComposedTypeStructure(possibleUDT);
      for (ColumnStructure column : getUDTColumns(schema.getName(), possibleUDT)) {
        type.addType(column.getName(), column.getType());
      }
      udts.add(type);
    }

    for (ComposedTypeStructure base : udts) {
      for (ComposedTypeStructure addition : udts) {
        base.completeExistingType(addition);
      }
    }

    for (ComposedTypeStructure udt : udts) {
      // TODO: remove after adding support for LOBs inside UDTs
      if (udt.containsLOBs()) {
        reporter.notYetSupported("LOBs inside UDTs", "the current import module");
        LOGGER.debug(
          "LOBs inside UDTs are not supported yet. Only the first level of hierarchy will be exported. UDT "
            + udt.getOriginalTypeName() + " detected as containing LOBs.",
          new ModuleException().withMessage("UDT containing LOBs:" + udt.toString()));
      }

      // TODO: remove after adding support for hierarchical UDTs
      if (udt.isHierarchical()) {
        reporter.notYetSupported("UDTs inside UDTs", "the current import module");
        LOGGER.debug(
          "UDTs inside UDTs are not supported yet. Only the first level of hierarchy will be exported. UDT "
            + udt.getOriginalTypeName() + " detected as hierarchical.",
          new ModuleException().withMessage("hierarchical UDT:" + udt.toString()));
      }

      // all recursive UDTs are hierarchical, so two warnings are expected on
      // recursive types
      // TODO: remove after adding support for recursive UDTs
      if (udt.isRecursive()) {
        reporter.notYetSupported("Recursive UDTs", "the current import module");
        LOGGER.debug(
          "Recursive UDTs are not supported yet. Only the first level of data will be exported. UDT "
            + udt.getOriginalTypeName() + " detected as recursive.",
          new ModuleException().withMessage("recursive UDT:" + udt.toString()));
      }
    }

    return udts;
  }

  /**
   * @param schema
   *          the schema structure
   * @return the database tables of a given schema
   * @throws SQLException
   */
  protected List<TableStructure> getTables(SchemaStructure schema) throws SQLException, ModuleException {
    List<TableStructure> tables = new ArrayList<TableStructure>();
    int tableIndex = 1;

    // Get custom views first so if there is any error in the queries there is no
    // wasted time

    if (!customViews.isEmpty()) {
      Map<String, Map<String, String>> schemaCustomViews = customViews.get(schema.getName());
      if (schemaCustomViews != null) {
        for (String viewName : schemaCustomViews.keySet()) {
          String viewDescription = schemaCustomViews.get(viewName).get("description");
          String query = schemaCustomViews.get(viewName).get("query");
          LOGGER.info("Obtaining table structure for custom view " + viewName);
          try {
            TableStructure customViewStructureAsTable = getCustomViewStructureAsTable(schema, viewName, tableIndex,
              viewDescription, query);
            tables.add(customViewStructureAsTable);
            tableIndex++;
          } catch (SQLException e) {
            if (e.getSQLState().equals("42S02")) {
              throw new TableNotFoundException().withMessage(e.getMessage() + "\nPlease check if the query in the YAML file is correct");
            } else if (e.getSQLState().equals("42000")) {
              throw new SQLParseException().withMessage(
                "The query has parsing errors\nPlease test the query for custom view '" + viewName + "' in a DBMS");
            } else {
              throw new ModuleException()
                .withMessage("Error getting custom view structure for " + schema.getName() + "." + viewName)
                .withCause(e);
            }
          }

        }
      } else {
        throw new ModuleException().withMessage("The schema '" + schema.getName() + "' was not found in "
          + customViewsPath + "\nPlease check if the schema name in the YAML file is correct");
      }
    }
    try (
      ResultSet rset = getMetadata().getTables(dbStructure.getName(), schema.getName(), "%", new String[] {"TABLE"})) {
      while (rset.next()) {
        String tableName = rset.getString(3);
        String tableDescription = rset.getString(5);

        if (getModuleSettings().isSelectedTable(schema.getName(), tableName)) {
          LOGGER.info("Obtaining table structure for " + schema.getName() + "." + tableName);
          tables.add(getTableStructure(schema, tableName, tableIndex, tableDescription, false));
          tableIndex++;
        } else {
          LOGGER.info("Ignoring table " + schema.getName() + "." + tableName);
        }
      }
    }
    try (
      ResultSet rset = getMetadata().getTables(dbStructure.getName(), schema.getName(), "%", new String[] {"VIEW"})) {
      while (rset.next()) {
        String viewName = rset.getString(3);
        String viewDescription = rset.getString(5);

        if (getModuleSettings().isSelectedTable(schema.getName(), viewName)) {
          LOGGER.info("Obtaining table structure for view " + schema.getName() + "." + viewName);
          tables.add(getViewStructure(schema, viewName, tableIndex, viewDescription));
          tableIndex++;
        } else {
          LOGGER.info("Ignoring view " + schema.getName() + "." + viewName);
        }
      }
    }

    return tables;
  }

  /**
   * @param schemaName
   *          the schema name
   * @return the database views of a given schema
   * @throws SQLException
   */
  protected List<ViewStructure> getViews(String schemaName) throws SQLException, ModuleException {
    List<ViewStructure> views = new ArrayList<>();
    try (ResultSet rset = getMetadata().getTables(dbStructure.getName(), schemaName, "%", new String[] {"VIEW"})) {
      while (rset.next()) {
        String viewName = rset.getString(3);
        ViewStructure view = new ViewStructure();
        view.setName(viewName);
        view.setDescription(rset.getString(5));
        if (getModuleSettings().isSelectedTable(schemaName, viewName)) {
          try {
            view.setColumns(getColumns(schemaName, viewName));
          } catch (SQLException e) {
            reporter.ignored("Columns from view " + viewName + " in schema " + schemaName,
                "there was a problem retrieving them form the database");
          }
          if (view.getColumns().isEmpty()) {
            reporter.ignored("View " + viewName + " in schema " + schemaName, "it contains no columns");
          } else {
            views.add(view);
          }
        }
      }
    }
    views.addAll(getCustomViews(schemaName));
    return views;
  }

  protected List<ViewStructure> getCustomViews(String schemaName) throws ModuleException {
    List<ViewStructure> views = new ArrayList<>();
    Map<String, Map<String, String>> schemaCustomViews = customViews.get(schemaName);

    if (schemaCustomViews != null) {
      for (String viewName : schemaCustomViews.keySet()) {
        ViewStructure view = new ViewStructure();
        view.setName("CUSTOM_" + viewName);
        view.setDescription(schemaCustomViews.get(viewName).get("description"));
        view.setQueryOriginal(schemaCustomViews.get(viewName).get("query"));
        try {
          view.setColumns(getColumnsFromCustomView(viewName, schemaCustomViews.get(viewName).get("query")));
        } catch (SQLException e) {
          reporter.ignored("Columns from custom view " + viewName + " in schema " + schemaName,
            "there was a problem retrieving them form the database");
        }
        if (view.getColumns().isEmpty()) {
          reporter.ignored("Custom view " + viewName + " in schema " + schemaName, "it contains no columns");
        } else {
          views.add(view);
        }
      }
    }

    return views;
  }

  protected Map<String, Map<String, Map<String, String>>> parseCustomViewsList(Path queryListPath) throws ModuleException {
    Map<String, Map<String, Map<String, String>>> queryList = new HashMap<>();
    Yaml yaml = new Yaml();
    if (queryListPath != null) {
      try (InputStream inputStream = Files.newInputStream(queryListPath)) {
        queryList = yaml.load(inputStream);
      } catch (IOException e) {
        throw new ModuleException()
          .withMessage("Could not read custom query list from file " + queryListPath.toAbsolutePath().toString())
          .withCause(e);

      }
    }
    return queryList;
  }

  /**
   * @param schemaName
   * @return
   * @throws SQLException
   */
  protected List<RoutineStructure> getRoutines(String schemaName) throws SQLException, ModuleException {
    // TODO add optional fields to routine (use getProcedureColumns)
    List<RoutineStructure> routines = new ArrayList<RoutineStructure>();

    try (ResultSet rset = getMetadata().getProcedures(dbStructure.getName(), schemaName, "%")) {
      while (rset.next()) {
        String routineName = rset.getString(3);
        RoutineStructure routine = new RoutineStructure();
        routine.setName(routineName);
        if (rset.getString(7) != null) {
          routine.setDescription(rset.getString(7));
        } else {
          if (rset.getShort(8) == 1) {
            routine.setDescription("Routine does not " + "return a result");
          } else if (rset.getShort(8) == 2) {
            routine.setDescription("Routine returns a result");
          }
        }
        routines.add(routine);
      }
    }
    return routines;
  }

  /**
   * @param tableName
   *          the name of the table
   * @return the table structure
   * @throws SQLException
   * @throws ModuleException
   */
  protected TableStructure getTableStructure(SchemaStructure schema, String tableName, int tableIndex,
    String description, boolean view) throws SQLException, ModuleException {
    TableStructure table = new TableStructure();
    table.setId(schema.getName() + "." + tableName);
    table.setName(tableName);
    table.setSchema(schema);
    table.setIndex(tableIndex);
    table.setDescription(description);

    List<ColumnStructure> columns = getColumns(schema.getName(), tableName);
    Iterator<ColumnStructure> columnsIterator = columns.iterator();
    while (columnsIterator.hasNext()) {
      ColumnStructure column = columnsIterator.next();
      if (!moduleSettings.isSelectedColumn(schema.getName(), tableName, column.getName())) {
        columnsIterator.remove();
      }
    }

    table.setColumns(columns);

    if (!view) {
      table.setPrimaryKey(getPrimaryKey(schema.getName(), tableName));
      table.setForeignKeys(getForeignKeys(schema.getName(), tableName));
      table.setCandidateKeys(getCandidateKeys(schema.getName(), tableName));
      table.setCheckConstraints(getCheckConstraints(schema.getName(), tableName));
      table.setTriggers(getTriggers(schema.getName(), tableName));
    }

    table.setRows(getRows(schema.getName(), tableName));

    return table;
  }

  protected TableStructure getViewStructure(SchemaStructure schema, String tableName, int tableIndex,
    String description) throws SQLException, ModuleException {
    TableStructure view = getTableStructure(schema, tableName, tableIndex, description, true);
    view.setFromView(true);
    view.setName(VIEW_NAME_PREFIX+view.getName());
    view.setDescription("Table materialized from view.\n"+view.getDescription());

    return view;
  }

  protected TableStructure getCustomViewStructureAsTable(SchemaStructure schema, String viewName, int tableIndex,
    String description, String query) throws SQLException, ModuleException {
    TableStructure view = new TableStructure();
    view.setId(schema.getName() + "." + viewName);
    view.setName(CUSTOM_VIEW_NAME_PREFIX + viewName);
    view.setSchema(schema);
    view.setIndex(tableIndex);
    view.setDescription(description);

    view.setColumns(getColumnsFromCustomView(viewName, query));
    view.setPrimaryKey(null);
    view.setForeignKeys(new ArrayList<ForeignKey>());
    view.setCandidateKeys(new ArrayList<CandidateKey>());
    view.setCheckConstraints(new ArrayList<CheckConstraint>());
    view.setTriggers(new ArrayList<Trigger>());

    view.setRows(getCustomViewRows(query));
    view.setFromCustomView(true);

    return view;
  }



  private int getRows(String schemaName, String tableName) throws SQLException, ModuleException {
    String query = sqlHelper.getRowsSQL(schemaName, tableName);
    LOGGER.debug("count query: " + query);
    try (ResultSet rs = getStatement().executeQuery(query)) {
      int count;

      count = -1;
      if (rs.next()) {
        count = rs.getInt(1);
      }
      LOGGER.debug("Counted " + count + " rows");

      return count;
    }
  }

  private int getCustomViewRows(String query) throws SQLException, ModuleException {
    try (ResultSet rs = getStatement().executeQuery(query)) {
      int count = rs.last() ? rs.getRow() : 0;
      LOGGER.debug("Counted " + count + " rows");

      return count;
    }
  }

  /**
   * Create the column structure
   *
   * @param tableName
   *          the name of the table which the column belongs to
   * @param columnName
   *          the name of the column
   * @param type
   *          the type of the column
   * @param nillable
   *          is the column nillable
   * @param index
   *          the column index
   * @param description
   *          the column description
   * @param defaultValue
   * @param isAutoIncrement
   * @return the column structure
   */
  protected ColumnStructure getColumnStructure(String tableName, String columnName, Type type, Boolean nillable,
    int index, String description, String defaultValue, Boolean isAutoIncrement) {
    ColumnStructure column = new ColumnStructure(tableName + "." + columnName, columnName, type, nillable, description,
      defaultValue, isAutoIncrement);
    return column;
  }

  protected List<UserStructure> getUsers(String databaseName) throws SQLException, ModuleException {
    List<UserStructure> users = new ArrayList<UserStructure>();
    String query = sqlHelper.getUsersSQL(databaseName);
    if (query != null) {
      try (ResultSet rs = getStatement().executeQuery(query)) {
        while (rs.next()) {
          UserStructure user = new UserStructure();
          user.setName(rs.getString("USER_NAME"));
          users.add(user);
        }
      }
    } else {
      users.add(new UserStructure("UNDEFINED_USER", "DESCRIPTION"));
      LOGGER.debug("Users were not imported: not supported yet on " + getClass().getSimpleName() + "\n"
        + "UNDEFINED_USER will be set as user name");
      reporter.notYetSupported("Importing of users", "this import module");
    }
    return users;
  }

  /**
   * @return the database roles
   * @throws SQLException
   */
  protected List<RoleStructure> getRoles() throws SQLException, ModuleException {
    List<RoleStructure> roles = new ArrayList<RoleStructure>();
    String query = sqlHelper.getRolesSQL();
    if (query != null) {
      try (ResultSet rs = getStatement().executeQuery(query)) {
        while (rs.next()) {
          RoleStructure role = new RoleStructure();
          String roleName;
          try {
            roleName = rs.getString("ROLE_NAME");
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            roleName = "";
          }
          role.setName(roleName);

          String admin = "";
          try {
            admin = rs.getString("ADMIN");
          } catch (SQLException e) {
            LOGGER.trace("handled SQLException", e);
          }
          role.setAdmin(admin);

          roles.add(role);
        }
      }
    } else {
      reporter.notYetSupported("importing roles", "this import module");
    }
    return roles;
  }

  /**
   * @return the database privileges
   * @throws SQLException
   */
  protected List<PrivilegeStructure> getPrivileges() throws SQLException, ModuleException {
    List<PrivilegeStructure> privileges = new ArrayList<PrivilegeStructure>();

    for (SchemaStructure schema : dbStructure.getSchemas()) {
      for (TableStructure table : schema.getTables()) {
        try (
          ResultSet rs = getMetadata().getTablePrivileges(dbStructure.getName(), schema.getName(), table.getName())) {
          while (rs.next()) {
            PrivilegeStructure privilege = new PrivilegeStructure();
            String grantor = rs.getString("GRANTOR");
            if (grantor == null) {
              grantor = "";
            }
            privilege.setGrantor(grantor);

            String grantee = rs.getString("GRANTEE");
            if (grantee == null) {
              grantee = "";
            }
            privilege.setGrantee(grantee);
            privilege.setType(rs.getString("PRIVILEGE"));

            String option = "false";
            String isGrantable = rs.getString("IS_GRANTABLE");
            if (isGrantable != null) {
              if ("yes".equalsIgnoreCase(isGrantable)) {
                option = "true";
              }
            }
            privilege.setOption(option);
            privilege.setObject("TABLE \"" + schema.getName() + "\".\"" + table.getName() + "\"");

            privileges.add(privilege);
          }
        } catch (SQLException e) {
          LOGGER.warn(
            "It was not possible to retrieve the list of all database permissions. Please ensure the current user has permissions to list all database permissions.",
            e);
          break;
        }
      }
    }
    return privileges;
  }

  /**
   * @param schemaName
   *          the schema name
   * @param udtName
   *          the UDT name
   * @return the columns of a given schema.table
   * @throws SQLException
   */
  protected List<ColumnStructure> getUDTColumns(String schemaName, String udtName)
    throws SQLException, ModuleException {

    // LOGGER.debug("id: " + schemaName + "." + udtName);
    List<ColumnStructure> columns = new ArrayList<ColumnStructure>();
    try (ResultSet rs = getMetadata().getColumns(dbStructure.getName(), schemaName, udtName, "%")) {
      LOGGER.debug("Getting structure of (possible) UDT " + schemaName + "." + udtName);
      while (rs.next()) {
        columns.add(getColumn(rs, udtName));
      }
    }

    return columns;
  }

  /**
   * @param schemaName
   *          the schema name
   * @param tableName
   *          the table name
   * @return the columns of a given schema.table
   * @throws SQLException
   */
  protected List<ColumnStructure> getColumns(String schemaName, String tableName) throws SQLException, ModuleException {

    // LOGGER.debug("id: " + schemaName + "." + tableName);
    List<ColumnStructure> columns = new ArrayList<ColumnStructure>();
    try (ResultSet rs = getMetadata().getColumns(dbStructure.getName(), schemaName, tableName, "%")) {
      while (rs.next()) {
        columns.add(getColumn(rs, tableName));
      }
    }

    return columns;
  }

  protected List<ColumnStructure> getColumnsFromCustomView(String viewName, String query)
    throws ModuleException, SQLException {
    List<ColumnStructure> columns = new ArrayList<>();

    ResultSetMetaData metaData = getConnection().prepareStatement(query).getMetaData();
    int nColumns = metaData.getColumnCount();
    for (int i = 1; i <= nColumns; i++) {
      String tableName = metaData.getTableName(i);
      String columnName = metaData.getColumnName(i);
      int columnType = metaData.getColumnType(i);
      String columnTypeName = metaData.getColumnTypeName(i);
      int columnDisplaySize = metaData.getColumnDisplaySize(i);
      int precision = metaData.getPrecision(i);

      Type checkedType = datatypeImporter.getCheckedType(dbStructure, actualSchema, tableName, columnName, columnType,
        columnTypeName, columnDisplaySize, precision, 10);

      ColumnStructure column = new ColumnStructure(viewName + "." + columnName, columnName, checkedType, true, "", "",
        false);

      columns.add(column);
    }
    return columns;
  }

  private ColumnStructure getColumn(ResultSet rs, String tableOrUdtName) throws SQLException {
    StringBuilder cLogMessage = new StringBuilder();
    // 1. Table catalog (may be null)
    // String tableCatalog = rs.getString(1);
    // 2. Table schema (may be null)
    // String tableSchema = rs.getString(2);
    // 3. Table name
    // String tableName = rs.getString(3);
    // 4. Column name
    String columnName = rs.getString(4);
    // cLogMessage.append("Column name: " + columnName + "\n");
    // 5. SQL type from java.sql.Types
    int dataType = rs.getInt(5);
    cLogMessage.append("Data type: " + dataType + "\n");
    // 6. Data source dependent type name, for a UDT the type name is
    // fully qualified
    String typeName = rs.getString(6);
    cLogMessage.append("Type name: " + typeName + "\n");
    // 7. Column size
    // The COLUMN_SIZE column specifies the column size for the given
    // column. For numeric data, this is the maximum precision. For
    // character data, this is the length in characters. For datetime
    // datatypes, this is the length in characters of the String
    // representation (assuming the maximum allowed precision of the
    // fractional seconds component). For binary data, this is the
    // length in bytes. For the ROWID datatype, this is the length in
    // bytes. Null is returned for data types where the column size is
    // not applicable.
    int columnSize = rs.getInt(7);
    cLogMessage.append("Column size: ").append(columnSize).append("\n");
    // 8. BUFFER_LENGTH is not used.
    // 9. the number of fractional digits. Null is returned for data
    // types where DECIMAL_DIGITS is not applicable.
    int decimalDigits = rs.getInt(9);
    cLogMessage.append("Decimal digits: ").append(decimalDigits).append("\n");
    // 10. Radix (typically either 10 or 2)
    int numPrecRadix = rs.getInt(10);
    cLogMessage.append("Radix: ").append(numPrecRadix).append("\n");
    // 11. is NULL allowed (using 18. instead)

    // 12. comment describing column (may be null)
    String remarks = rs.getString(12);
    cLogMessage.append("Remarks: ").append(remarks).append("\n");
    // 13. default value for the column, which should be interpreted as
    // a string when the value is enclosed in single quotes (may be
    // null)
    String defaultValue = rs.getString(13);
    cLogMessage.append("Default value: ").append(defaultValue).append("\n");
    // 14. SQL_DATA_TYPE int => unused
    // 15. SQL_DATETIME_SUB int => unused
    // 16. CHAR_OCTET_LENGTH int => for char types the maximum number of
    // bytes in the column
    // 17. index of column in table (starting at 1)
    int index = rs.getInt(17);
    cLogMessage.append("Index: ").append(index).append("\n");
    // 18. ISO rules are used to determine the nullability for a column.
    // YES --- if the column can include NULLs
    // NO --- if the column cannot include NULLs
    // empty string --- if the nullability for the column is unknown
    Boolean isNullable = true;
    try {
      isNullable = "YES".equals(rs.getString(18));
    } catch (SQLException e) {
      LOGGER.debug("Could not get nullability property of column. current debug message: '" + cLogMessage + "'", e);
    }
    cLogMessage.append("Is Nullable: ").append(isNullable).append("\n");
    // 20. SCOPE_SCHEMA String => schema of table that is the scope of a
    // reference attribute (null if the DATA_TYPE isn't REF)
    // 21. SCOPE_TABLE String => table name that this the scope of a
    // reference attribute (null if the DATA_TYPE isn't REF)
    // 22. SOURCE_DATA_TYPE short => source type of a distinct type or
    // user-generated Ref type, SQL type from java.sql.Types (null if
    // DATA_TYPE isn't DISTINCT or user-generated REF)
    if (dataType == Types.DISTINCT) {
      Integer sourceDataType = (int) rs.getShort(22);
      dataType = sourceDataType;
    }
    // 23. IS_AUTOINCREMENT String => Indicates whether this column is
    // auto incremented
    // YES --- if the column is auto incremented
    // NO --- if the column is not auto incremented
    // empty string --- if it cannot be determined whether the column is
    // auto incremented
    Boolean isAutoIncrement = false;
    try {
      isAutoIncrement = "YES".equals(rs.getString(23));
    } catch (SQLException e) {
      LOGGER.debug("Could not get auto increment property of column. current debug message: '" + cLogMessage + "'", e);
    }
    cLogMessage.append("Is auto increment: ").append(isAutoIncrement).append("\n");
    // 24. IS_GENERATEDCOLUMN String => Indicates whether this is a
    // generated column
    // YES --- if this a generated column
    // NO --- if this not a generated column
    // empty string --- if it cannot be determined whether this is a
    // generated column
    // Boolean isGeneratedColumn = rs.getString(24) == "YES";

    // cLog.append("(" + tableName + ") " + "colName: " + columnName
    // + "; dataType: " + dataType + "; typeName: " + typeName);
    Type columnType = datatypeImporter.getCheckedType(dbStructure, actualSchema, tableOrUdtName, columnName, dataType,
      typeName, columnSize, decimalDigits, numPrecRadix);

    cLogMessage.append("Calculated type: ").append(columnType.getClass().getSimpleName()).append("\n");

    ColumnStructure column = getColumnStructure(tableOrUdtName, columnName, columnType, isNullable, index, remarks,
      defaultValue, isAutoIncrement);

    cLogMessage.append("ColumnType hash: ").append(column.getType().hashCode()).append("\n");
    LOGGER.debug(cLogMessage.toString());

    return column;
  }

  /**
   * Get the table primary key
   *
   * @param tableName
   *          the name of the table
   * @return the primary key
   * @throws SQLException
   * @throws ModuleException
   */
  protected PrimaryKey getPrimaryKey(String schemaName, String tableName) throws SQLException, ModuleException {
    String pkName = null;
    List<String> pkColumns = new ArrayList<String>();

    try (ResultSet rs = getMetadata().getPrimaryKeys(getDatabaseStructure().getName(), schemaName, tableName)) {
      while (rs.next()) {
        pkName = rs.getString(6);
        pkColumns.add(rs.getString(4));
      }
    }

    if (pkName == null) {
      pkName = tableName + "_pkey";
    }

    PrimaryKey pk = new PrimaryKey();
    pk.setName(pkName);
    pk.setColumnNames(pkColumns);
    return pkColumns.isEmpty() ? null : pk;
  }

  /**
   * Get the table foreign keys
   *
   * @param schemaName
   *          the name of the schema
   * @param tableName
   *          the name of the table
   * @return the foreign keys
   * @throws SQLException
   * @throws ModuleException
   */
  protected List<ForeignKey> getForeignKeys(String schemaName, String tableName) throws SQLException, ModuleException {

    List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

    try (ResultSet rs = getMetadata().getImportedKeys(getDatabaseStructure().getName(), schemaName, tableName)) {
      while (rs.next()) {
        List<Reference> references = new ArrayList<Reference>();
        boolean found = false;
        Reference reference = new Reference(rs.getString("FKCOLUMN_NAME"), rs.getString("PKCOLUMN_NAME"));

        String fkeyName = rs.getString("FK_NAME");
        if (fkeyName == null) {
          fkeyName = "FK_" + rs.getString("FKCOLUMN_NAME");
        }

        for (ForeignKey key : foreignKeys) {
          if (key.getName().equals(fkeyName)) {
            references = key.getReferences();
            references.add(reference);
            key.setReferences(references);
            found = true;
            break;
          }
        }

        if (!found) {
          ForeignKey fkey = new ForeignKey();
          fkey.setId(tableName + "." + rs.getString("FKCOLUMN_NAME"));
          fkey.setName(fkeyName);
          fkey.setReferencedSchema(getReferencedSchema(rs.getString("PKTABLE_SCHEM")));
          fkey.setReferencedTable(rs.getString("PKTABLE_NAME"));
          references.add(reference);
          fkey.setReferences(references);
          // TODO add: fkey.setMatchType(??);
          fkey.setUpdateAction(getUpdateRule(rs.getShort("UPDATE_RULE")));
          fkey.setDeleteAction(getDeleteRule(rs.getShort("DELETE_RULE")));
          foreignKeys.add(fkey);
        }
      }
    }
    return foreignKeys;
  }

  protected String getReferencedSchema(String s) throws SQLException, ModuleException {
    return s;
  }

  /**
   * Gets the name of the update rule
   *
   * @param value
   * @return
   */
  protected String getUpdateRule(Short value) {
    String rule = null;
    switch (value) {
      case 0:
        rule = "CASCADE";
        break;
      case 1:
        rule = "RESTRICT";
        break;
      case 2:
        rule = "SET NULL";
        break;
      case 3:
        rule = "NO ACTION";
        break;
      case 4:
        rule = "SET DEFAULT";
        break;
      default:
        rule = "SET DEFAULT";
        break;
    }
    return rule;
  }

  /**
   * Gets the name of the delete rule
   *
   * @param value
   * @return
   */
  protected String getDeleteRule(Short value) {
    return getUpdateRule(value);
  }

  /**
   * Gets the candidate keys of a given schema table
   *
   * @param schemaName
   * @param tableName
   * @return
   * @throws SQLException
   */
  // VERIFY adding PKs
  protected List<CandidateKey> getCandidateKeys(String schemaName, String tableName)
    throws SQLException, ModuleException {
    List<CandidateKey> candidateKeys = new ArrayList<CandidateKey>();

    try (ResultSet rs = getMetadata().getIndexInfo(dbStructure.getName(), schemaName, escapeObjectName(tableName), true,
      true)) {
      while (rs.next()) {
        List<String> columns = new ArrayList<String>();
        boolean found = false;

        for (CandidateKey key : candidateKeys) {
          if (key.getName().equals(rs.getString(6))) {
            columns = key.getColumns();
            columns.add(rs.getString(9));
            key.setColumns(columns);
            found = true;
            break;
          }
        }

        if (!found) {
          if (rs.getString(6) != null) {
            CandidateKey candidateKey = new CandidateKey();
            candidateKey.setName(rs.getString(6));
            columns.add(rs.getString(9));
            candidateKey.setColumns(columns);
            candidateKeys.add(candidateKey);
          }
        }
      }
    }
    return candidateKeys;
  }

  /**
   * Gets the check constraints of a given schema table
   *
   * @param schemaName
   * @param tableName
   * @return
   */
  protected List<CheckConstraint> getCheckConstraints(String schemaName, String tableName) throws ModuleException {
    List<CheckConstraint> checkConstraints = new ArrayList<CheckConstraint>();

    String query = sqlHelper.getCheckConstraintsSQL(schemaName, tableName);
    if (query != null) {
      try (ResultSet rs = getStatement().executeQuery(query)) {
        while (rs.next()) {
          CheckConstraint checkConst = new CheckConstraint();

          String checkName = "";
          try {
            checkName = rs.getString("CHECK_NAME");
          } catch (SQLException e) {
            LOGGER.trace("handled SQLException", e);
          }
          checkConst.setName(checkName);

          String checkCondition = "UNKNOWN";
          try {
            checkCondition = rs.getString("CHECK_CONDITION");
          } catch (SQLException e) {
            LOGGER.trace("handled SQLException", e);
          }
          checkConst.setCondition(checkCondition);

          String checkDescription = null;
          try {
            checkDescription = rs.getString("CHECK_DESCRIPTION");
          } catch (SQLException e) {
            LOGGER.trace("handled SQLException", e);
          }
          if (checkDescription != null) {
            checkConst.setDescription(checkDescription);
          }
          checkConstraints.add(checkConst);
        }
      } catch (SQLException e) {
        String message = "Check constraints were not imported for " + schemaName + "." + tableName + ". ";
        if (StringUtils.isBlank(query)) {
          message += "Not supported by " + sqlHelper.getName();
        } else {
          message += "An error occurred!";
        }
        LOGGER.debug(message, e);
      }
    } else {
      reporter.notYetSupported("importing check constraints", "this import module");
    }
    return checkConstraints;
  }

  /**
   * Gets the triggers of a given schema table
   *
   * @param schemaName
   * @param tableName
   * @return
   */
  protected List<Trigger> getTriggers(String schemaName, String tableName) throws ModuleException {
    List<Trigger> triggers = new ArrayList<Trigger>();

    String query = sqlHelper.getTriggersSQL(schemaName, tableName);
    if (query != null) {
      try (ResultSet rs = getStatement().executeQuery(sqlHelper.getTriggersSQL(schemaName, tableName))) {
        while (rs.next()) {
          Trigger trigger = new Trigger();

          String triggerName;
          try {
            triggerName = rs.getString("TRIGGER_NAME");
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            triggerName = "";
          }
          trigger.setName(triggerName);

          String actionTime;
          try {
            actionTime = processActionTime(rs.getString("ACTION_TIME"));
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            actionTime = "";
          }
          trigger.setActionTime(actionTime);

          String triggerEvent;
          try {
            triggerEvent = processTriggerEvent(rs.getString("TRIGGER_EVENT"));
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            triggerEvent = "";
          }
          trigger.setTriggerEvent(triggerEvent);

          String triggeredAction;
          try {
            triggeredAction = rs.getString("TRIGGERED_ACTION");
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            triggeredAction = "";
          }
          trigger.setTriggeredAction(triggeredAction);

          String description;
          try {
            description = rs.getString("REMARKS");
          } catch (SQLException e) {
            LOGGER.debug("handled SQLException", e);
            description = null;
          }
          if (description != null) {
            trigger.setDescription(description);
          }

          triggers.add(trigger);
        }
      } catch (SQLException e) {
        LOGGER.debug("No triggers imported for " + schemaName + "." + tableName, e);
      }
    } else {
      LOGGER.debug("Triggers were not imported: not supported yet on " + getClass().getSimpleName());
    }
    return triggers;
  }

  /**
   * Sanitizes the trigger event data
   *
   * @param string
   * @return
   */
  protected String processTriggerEvent(String string) {
    return string;
  }

  /**
   * Sanitizes the trigger action time data
   *
   * @param string
   * @return
   */
  protected String processActionTime(String string) {
    return string;
  }

  protected Row convertRawToRow(ResultSet rawData, TableStructure tableStructure)
    throws InvalidDataException, SQLException, ModuleException {
    Row row = null;
    if (isRowValid(rawData, tableStructure)) {
      List<Cell> cells = new ArrayList<Cell>(tableStructure.getColumns().size());

      long currentRow = tableStructure.getCurrentRow();
      if (isGetRowAvailable()) {
        currentRow = rawData.getRow();
      }

      for (int i = 0; i < tableStructure.getColumns().size(); i++) {
        ColumnStructure colStruct = tableStructure.getColumns().get(i);

        try {
          Cell cell = convertRawToCell(tableStructure.getName(), colStruct.getName(), i + 1, currentRow,
            colStruct.getType(), rawData);
          cells.add(cell);
        } catch (Exception e) {
          cells.add(new NullCell(tableStructure.getName() + "." + colStruct.getName() + "." + (i + 1)));
          reporter.cellProcessingUsedNull(tableStructure, colStruct, currentRow, e);
        }
      }
      row = new Row(currentRow, cells);
    } else {
      // insert null in all fields
      List<Cell> cells = new ArrayList<Cell>(tableStructure.getColumns().size());
      for (int i = 0; i < tableStructure.getColumns().size(); i++) {
        ColumnStructure colStruct = tableStructure.getColumns().get(i);
        cells.add(new SimpleCell(tableStructure.getName() + "." + colStruct.getName() + "." + (i + 1), null));
      }
      row = new Row(tableStructure.getCurrentRow(), cells);

      reporter.rowProcessingUsedNull(tableStructure, tableStructure.getCurrentRow(),
        new ModuleException().withMessage("isRowValid returned false"));
    }
    tableStructure.incrementCurrentRow();
    return row;
  }

  protected Cell convertRawToCell(String tableName, String columnName, int columnIndex, long rowIndex, Type cellType,
    ResultSet rawData) throws SQLException, InvalidDataException, ModuleException {
    Cell cell;
    String id = tableName + "." + columnName + "." + rowIndex;

    try {
      if (cellType instanceof ComposedTypeArray) {
        ComposedTypeArray composedTypeArray = (ComposedTypeArray) cellType;
        Array array = rawData.getArray(columnName);
        LOGGER.trace("Parsing array of subtype " + composedTypeArray.getElementType().getClass().getSimpleName());
        cell = parseArray(id, array);
      } else if (cellType instanceof ComposedTypeStructure) {
        cell = rawToCellComposedTypeStructure(id, columnName, cellType, rawData);
      } else if (cellType instanceof SimpleTypeBoolean) {
        boolean booleanValue = rawData.getBoolean(columnName);
        boolean wasNull = rawData.wasNull();
        if (wasNull) {
          cell = new NullCell(id);
        } else {
          cell = new SimpleCell(id, booleanValue ? "true" : "false");
        }

      } else if (cellType instanceof SimpleTypeNumericApproximate) {
        cell = rawToCellSimpleTypeNumericApproximate(id, columnName, cellType, rawData);
      } else if (cellType instanceof SimpleTypeDateTime) {
        cell = rawToCellSimpleTypeDateTime(id, columnName, cellType, rawData);
      } else if (cellType instanceof SimpleTypeBinary) {
        cell = rawToCellSimpleTypeBinary(id, columnName, cellType, rawData);
      } else if (cellType instanceof UnsupportedDataType) {
        cell = rawToCellUnsupportedDataType(id, columnName, cellType, rawData);
      } else if (cellType instanceof SimpleTypeNumericExact) {
        cell = rawToCellSimpleTypeNumericExact(id, columnName, cellType, rawData);
      } else {
        try {
          if (rawData.getString(columnName) == null) {
            cell = new NullCell(id);
          } else {
            cell = new SimpleCell(id, rawData.getString(columnName));
          }
        } catch (SQLException e) {
          LOGGER.debug("Could not export cell of unknown/undefined datatype", e);
          cell = new NullCell(id);
        } catch (NoClassDefFoundError e) {
          try {
            Object[] aStruct = ((Struct) rawData.getObject(columnName)).getAttributes();

            StringBuilder value = new StringBuilder("(");
            String separator = "";
            for (Object o : aStruct) {
              value.append(separator).append(o.toString());
              separator = ",";
            }
            value.append(")");

            cell = new SimpleCell(id, value.toString());
          } catch (SQLException e1) {
            LOGGER.debug("No Class Def Found when trying to getString", e);
            LOGGER.debug("Could not export cell of unknown/undefined datatype", e1);
            cell = new NullCell(id);
          }
        }
      }
    } catch (Exception e) {
      cell = new NullCell(id);
      reporter.cellProcessingUsedNull(tableName, columnName, rowIndex, e);
    }
    return cell;
  }

  protected Cell rawToCellComposedTypeStructure(String id, String columnName, Type cellType, ResultSet rawData)
    throws InvalidDataException {
    throw new InvalidDataException("Convert data of struct type not yet supported");
  }

  protected Cell rawToCellUnsupportedDataType(String id, String columnName, Type cellType, ResultSet rawData)
    throws InvalidDataException {
    Cell cell;
    try {
      cell = new SimpleCell(id, rawData.getString(columnName));
    } catch (SQLException e) {
      LOGGER.debug("Could not export cell of unsupported datatype: OTHER", e);
      cell = new NullCell(id);
    }
    return cell;
  }

  protected Cell rawToCellSimpleTypeNumericExact(String id, String columnName, Type cellType, ResultSet rawData)
    throws SQLException, ModuleException {
    String stringValue = rawData.getString(columnName);
    boolean wasNull = rawData.wasNull();
    Cell cell;
    if (wasNull) {
      cell = new NullCell(id);
    } else {
      int eIndex = stringValue.indexOf('E');
      if (eIndex > 0) {
        String fst = stringValue.substring(0, eIndex);
        String newValue = null;

        if (eIndex < stringValue.length() - 1) {
          String snd = stringValue.substring(eIndex + 1);
          Integer fstNum = null;
          Integer sndNum = null;

          try {
            fstNum = Integer.parseInt(fst);
          } catch (NumberFormatException e) {
            LOGGER.debug("could not parse `" + fst + "` as integer", e);
          }

          try {
            sndNum = Integer.parseInt(snd);
          } catch (NumberFormatException e) {
            LOGGER.debug("could not parse `" + snd + "` as integer", e);
          }

          if (fstNum == null && sndNum == null) {
            // this will save the value as NULL and trigger the Reporter
            throw new ModuleException().withMessage("Could not parse `" + stringValue + "` as an exact numeric value");
          } else {
            if (fstNum != null && sndNum != null) {
              if (fstNum == 0 || sndNum == 0) {
                newValue = "0";
              } else {
                newValue = String.valueOf(Math.pow(fstNum, sndNum));
                reporter.valueChanged(stringValue, newValue, " exact numeric values can not have exponent (`E`) ",
                  "column " + columnName);
              }
            } else if (fstNum != null) {
              // fstNum != null && sndNum == null
              newValue = fst;
              reporter.valueChanged(stringValue, newValue, " exact numeric values can not have exponent (`E`) ",
                "column " + columnName);
            } else {
              // fstNum == null && sndNum != null
              newValue = "0";
              reporter.valueChanged(stringValue, newValue, " exact numeric values can not have exponent (`E`) ",
                "column " + columnName);
            }
            cell = new SimpleCell(id, newValue);
          }
        } else {
          // 'E' is the last character in the string, use only the first part
          cell = new SimpleCell(id, fst);
        }
      } else {
        cell = new SimpleCell(id, stringValue);
      }
    }

    return cell;
  }

  private void arrayToArrayCell(int baseType, ArrayCell arrayCell, int distanceToSimpleArray, Integer[] path,
    Object[] items) {
    if (distanceToSimpleArray == 1) {
      String baseId = arrayCell.getId();
      for (int i = 0; i < items.length; i++) {

        StringBuilder id = new StringBuilder(baseId);
        if (path.length > 0) {
          id.append(".").append(StringUtils.join(path, "."));
        }
        id.append(".").append(i + 1); // 1-based ids

        arrayCell.put(getArrayCell(id.toString(), baseType, items[i]), ArrayUtils.add(path, i + 1));
      }
    } else if (items instanceof Object[][]) {
      Object[][] matrix = (Object[][]) items;
      for (int i = 0; i < items.length; i++) {
        arrayToArrayCell(baseType, arrayCell, distanceToSimpleArray - 1, ArrayUtils.add(path, i + 1), matrix[i]);
      }
    }
  }

  protected Cell getArrayCell(String id, int baseType, Object value) {
    switch (baseType) {
      case Types.CHAR:
      case Types.VARCHAR:
        return new SimpleCell(id, ((String) value));

      case Types.BIT:
        return new SimpleCell(id, ((Boolean) value).toString());

      case Types.DATE:
        return new SimpleCell(id, ((Date) value).toString());

      case Types.INTEGER:
        return new SimpleCell(id, ((Integer) value).toString());

      case Types.DOUBLE:
        return new SimpleCell(id, ((Double) value).toString());

      default:
        return new NullCell(id);
    }
  }

  protected Cell parseArray(String baseid, Array array) throws SQLException, InvalidDataException {
    if (array == null) {
      return new NullCell(baseid);
    }

    int baseType = array.getBaseType();
    try {
      int dimensions = StringUtils.countMatches(array.getArray().getClass().toString(), '[');
      ArrayCell arrayCell = new ArrayCell(baseid);
      arrayToArrayCell(baseType, arrayCell, dimensions, new Integer[] {}, (Object[]) array.getArray());
      return arrayCell;
    } catch (SQLFeatureNotSupportedException e) {
      LOGGER.debug("Got a problem getting Array value", e);
      reporter.customMessage(getClass().getName(),
        "Obtaining array elements as strings as no better type could be identified.");
      try (ResultSet rs = array.getResultSet()) {
        while (rs.next()) {
          String item = rs.getString(1);
          return new SimpleCell(baseid, item);
        }
      }
    }

    return new NullCell(baseid);
  }

  protected Cell rawToCellSimpleTypeNumericApproximate(String id, String columnName, Type cellType, ResultSet rawData)
    throws SQLException {

    String stringValue = rawData.getString(columnName);
    boolean wasNull = rawData.wasNull();
    Cell cell;
    if (wasNull) {
      cell = new NullCell(id);
      LOGGER.trace("rawToCellSimpleTypeNumericApproximate cell: NULL");
    } else {
      cell = new SimpleCell(id, stringValue);
      LOGGER.trace("rawToCellSimpleTypeNumericApproximate cell: " + ((SimpleCell) cell).getSimpleData());
    }

    return cell;
  }

  protected Cell rawToCellSimpleTypeDateTime(String id, String columnName, Type cellType, ResultSet rawData)
    throws SQLException {
    Cell cell = null;
    SimpleTypeDateTime undefinedDate = (SimpleTypeDateTime) cellType;
    if (undefinedDate.getTimeDefined()) {
      if ("TIME".equalsIgnoreCase(cellType.getSql99TypeName())
        || "TIME WITH TIME ZONE".equalsIgnoreCase(cellType.getSql99TypeName())) {
        Time time = rawData.getTime(columnName);
        if (time != null) {
          cell = new SimpleCell(id, time.toString());
        } else {
          cell = new NullCell(id);
        }
      } else {
        Timestamp timestamp = rawData.getTimestamp(columnName);
        if (timestamp != null) {
          cell = new SimpleCell(id, JodaUtils.getDateTime(timestamp).toString());
        } else {
          cell = new NullCell(id);
        }
      }
    } else {
      Date date = rawData.getDate(columnName);
      if (date != null) {
        cell = new SimpleCell(id, date.toString());
      } else {
        cell = new NullCell(id);
      }
    }
    return cell;
  }

  protected Cell rawToCellSimpleTypeBinary(String id, String columnName, Type cellType, ResultSet rawData)
    throws SQLException, ModuleException {
    Cell cell;

    if(cellType instanceof SimpleTypeBinary && ((SimpleTypeBinary) cellType).isOutsideDatabase()) {
      cell = new SimpleCell(id, rawData.getString(columnName));
    } else {
      Blob blob = rawData.getBlob(columnName);
      if (blob != null && !rawData.wasNull()) {
        cell = new BinaryCell(id, blob);
      } else {
        cell = new NullCell(id);
      }
    }
    return cell;
  }

  protected boolean isRowValid(ResultSet raw, TableStructure structure) throws InvalidDataException, SQLException {
    boolean ret;
    ResultSetMetaData metadata = raw.getMetaData();
    if (metadata.getColumnCount() == structure.getColumns().size()) {
      ret = true;
    } else {
      ret = false;
      LOGGER.debug("Invalid row",
        new InvalidDataException("table: " + structure.getName() + " row number: " + raw.getRow()
          + " error: different column number from structure " + metadata.getColumnCount() + "!="
          + structure.getColumns().size()));
    }
    return ret;
  }

  protected ResultSet getTableRawData(TableStructure table) throws SQLException, ModuleException {
    String query = sqlHelper.selectTableSQL(table);
    LOGGER.debug("query: " + query);
    return getTableRawData(query, table.getId());
  }

  protected ResultSet getTableRawData(String query, String tableId) throws SQLException, ModuleException {
    Statement st = getStatement();

    st.setFetchSize(DEFAULT_ROW_FETCH_BLOCK_SIZE);
    try {
      return st.executeQuery(query);
    } catch (SQLException sqlException) {
      LOGGER.debug("Error executing query with default fetch size of {}", st.getFetchSize());
    }

    if (connection.isClosed()) {
      connection = null;
      connection = getConnection();
      statement = null;
    }
    st = getStatement();
    st.setFetchSize(SMALL_ROW_FETCH_BLOCK_SIZE);
    try {
      return st.executeQuery(query);
    } catch (SQLException sqlException) {
      LOGGER.debug("Error executing query with fetch size of {}", st.getFetchSize());
    }

    if (connection.isClosed()) {
      connection = null;
      connection = getConnection();
      statement = null;
    }
    st = getStatement();
    st.setFetchSize(MINIMUM_ROW_FETCH_BLOCK_SIZE);
    try {
      return st.executeQuery(query);
    } catch (SQLException sqlException) {
      LOGGER.debug("Error executing query with fetch size of {}", st.getFetchSize(), sqlException);
    }

    String msg = "Could not retrieve data from table '" + tableId + "'. See log for details.";

    reporter.customMessage(this.getClass().getName(), msg);
    throw new ModuleException().withMessage(msg);
  }

  /**
   * Advances to the next batch of results in a ResultSet. Also tries to adjust
   * the fetch size in case it is too big. Setting it to
   * SMALL_ROW_FETCH_BLOCK_SIZE (if it is bigger than that), and then (if that is
   * still too big) trying with MINIMUM_ROW_FETCH_BLOCK_SIZE. Attempting to adjust
   * the fetch size further is a NO-OP and returns false.
   *
   * @param tableResultSet
   *          the tableResultSet with a big fetch size
   * @return true if there are more results, false if the
   */
  protected boolean resultSetNext(ResultSet tableResultSet) throws ModuleException {
    try {
      return tableResultSet.next();
    } catch (SQLException e) {
      LOGGER.debug("Exception on ResultSet.next()", e);
    }

    int currentFetchSize;

    try {
      currentFetchSize = tableResultSet.getFetchSize();
    } catch (SQLException e) {
      throw new ModuleException().withMessage("Could not obtain the next set of results from this table.").withCause(e);
    }
    LOGGER.debug("Current fetch size: {}", currentFetchSize);

    try {
      if (currentFetchSize <= MINIMUM_ROW_FETCH_BLOCK_SIZE && currentFetchSize > 0) {
        // fail, because we can not reduce the fetch size anymore
        LOGGER.debug("fetch size of '{}' is lower than MINIMUM_ROW_FETCH_BLOCK_SIZE={}", currentFetchSize,
          MINIMUM_ROW_FETCH_BLOCK_SIZE);
        throw new ModuleException().withMessage("Could not obtain the next set of results from this table.");
      } else if (currentFetchSize > SMALL_ROW_FETCH_BLOCK_SIZE || currentFetchSize == 0) {
        // reduce fetch size and try again
        tableResultSet.setFetchSize(SMALL_ROW_FETCH_BLOCK_SIZE);
        return resultSetNext(tableResultSet);
      } else {
        // reduce fetch size and try again
        tableResultSet.setFetchSize(MINIMUM_ROW_FETCH_BLOCK_SIZE);
        return resultSetNext(tableResultSet);
      }
    } catch (SQLException e) {
      throw new ModuleException().withMessage("Could not obtain the next set of results from this table.").withCause(e);
    }
  }

  /**
   * Gets the schemas that won't be exported.
   * <p>
   * Accepts schemas names in as regular expressions I.e. SYS.* will ignore
   * SYSCAT, SYSFUN, etc
   *
   * @return the schemas to be ignored at export
   */
  protected Set<String> getIgnoredExportedSchemas() {
    HashSet ignore = new HashSet<String>();
    ignore.add("information_schema");
    ignore.add("pg_catalog");
    return ignore;
  }

  @Override
  public DatabaseExportModule migrateDatabaseTo(DatabaseExportModule exportModule) throws ModuleException {
    try {
      moduleSettings = exportModule.getModuleSettings();

      exportModule.initDatabase();

      exportModule.setIgnoredSchemas(getIgnoredExportedSchemas());

      exportModule.handleStructure(getDatabaseStructure());

      for (SchemaStructure schema : getDatabaseStructure().getSchemas()) {
        exportModule.handleDataOpenSchema(schema.getName());
        Map<String, Map<String, String>> schemaCustomViews = customViews.get(schema.getName());
        for (TableStructure table : schema.getTables()) {
          exportModule.handleDataOpenTable(table.getId());

          long nRows = 0;
          long tableRows = table.getRows();
          if (moduleSettings.shouldFetchRows()) {

            if(table.isFromCustomView()) {
              if (schemaCustomViews != null) {
                try (ResultSet tableRawData = getTableRawData(
                  schemaCustomViews.get(table.getName().replace(CUSTOM_VIEW_NAME_PREFIX, "")).get("query"), table.getId())) {
                  while (resultSetNext(tableRawData)) {
                    exportModule.handleDataRow(convertRawToRow(tableRawData, table));
                    nRows++;
                  }
                } catch (SQLException | ModuleException e) {
                  if (e.getCause().getClass().equals(IOException.class)) {
                    LOGGER.error(e.getCause().getMessage());
                  }
                  LOGGER.error("Could not obtain all data from the custom view.", e);
                }
              }
              else {
                LOGGER.error("Could not obtain data from the custom view.");
              }
            } else {
              try (ResultSet tableRawData = getTableRawData(table)) {
                while (resultSetNext(tableRawData)) {
                  exportModule.handleDataRow(convertRawToRow(tableRawData, table));
                  nRows++;
                }
              } catch (SQLException | ModuleException e) {
                if (e.getCause().getClass().equals(IOException.class)) {
                  LOGGER.error(e.getCause().getMessage());
                }
                LOGGER.error("Could not obtain all data from the current table.", e);
              }
            }
          }
          LOGGER.debug("Total of {} row(s) processed", nRows);

          if (nRows < tableRows && moduleSettings.shouldFetchRows()) {
            LOGGER.warn("The database reported a total of {} rows. Some data may have been lost.", tableRows);
            reporter.customMessage(this.getClass().getName(),
              "Only processed " + nRows + " out of " + tableRows + " rows contained in table '" + table.getName()
                + "'. The log file may contain more information to help diagnose this problem.");
          }

          getDatabaseStructure().getTableById(table.getId()).setRows(nRows);

          exportModule.handleDataCloseTable(table.getId());
        }
        exportModule.handleDataCloseSchema(schema.getName());
      }
      LOGGER.debug("Freeing resources");
      exportModule.finishDatabase();
    } finally {
      LOGGER.debug("Closing connection to source database");
      closeConnection();
    }
    return null;
  }

  /**
   * Provide a reporter through which potential conversion problems should be
   * reported. This reporter should be provided only once for the export module
   * instance.
   *
   * @param reporter
   *          The initialized reporter instance.
   */
  @Override
  public void setOnceReporter(Reporter reporter) {
    this.reporter = reporter;
    sqlHelper.setOnceReporter(reporter);
    datatypeImporter.setOnceReporter(reporter);
  }

  public ModuleSettings getModuleSettings() {
    return moduleSettings;
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }

  public String escapeObjectName(String objectName) {
    return objectName;
  }
}
