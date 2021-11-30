/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
/**
 *
 */
package com.databasepreservation.modules.jdbc.out;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.data.ArrayCell;
import com.databasepreservation.model.data.BinaryCell;
import com.databasepreservation.model.data.Cell;
import com.databasepreservation.model.data.ComposedCell;
import com.databasepreservation.model.data.NullCell;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.data.SimpleCell;
import com.databasepreservation.model.exception.InvalidDataException;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.ModuleSettings;
import com.databasepreservation.model.structure.ColumnStructure;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.ForeignKey;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.model.structure.type.SimpleTypeBinary;
import com.databasepreservation.model.structure.type.SimpleTypeBoolean;
import com.databasepreservation.model.structure.type.SimpleTypeDateTime;
import com.databasepreservation.model.structure.type.SimpleTypeNumericApproximate;
import com.databasepreservation.model.structure.type.SimpleTypeNumericExact;
import com.databasepreservation.model.structure.type.SimpleTypeString;
import com.databasepreservation.model.structure.type.Type;
import com.databasepreservation.model.structure.type.UnsupportedDataType;
import com.databasepreservation.modules.DefaultExceptionNormalizer;
import com.databasepreservation.modules.SQLHelper;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 * @author Luis Faria
 */
public class JDBCExportModule implements DatabaseExportModule {
  protected static final boolean DEFAULT_CAN_DROP_DATABASE = false;
  protected static int BATCH_SIZE = 100;
  protected final String driverClassName;
  protected final String connectionURL;
  protected final Map<String, Connection> connections;
  protected final boolean canDropDatabase;
  private static final Logger LOGGER = LoggerFactory.getLogger(JDBCExportModule.class);
  protected Connection connection;

  protected Statement statement;

  protected DatabaseStructure databaseStructure;

  protected TableStructure currentTableStructure;

  protected SQLHelper sqlHelper;

  protected int batch_index;

  protected PreparedStatement currentRowBatchInsertStatement;
  protected long currentRowBatchStartIndex = 0;

  protected Set<String> ignoredSchemas;

  protected Set<String> existingSchemas;

  protected boolean currentIsIgnoredSchema;

  private final List<String> batchSQL = new ArrayList<>();

  private Set<String> exportedPrimaryKeys = new HashSet<>();

  private List<CleanResourcesInterface> cleanResourcesList = new ArrayList<>();

  protected Reporter reporter;

  /**
   * Shorthand instance to obtain a no-op (no operation, do nothing)
   * CleanResourcesInterface
   */
  protected static final CleanResourcesInterface noOpCleanResourcesInterface = new CleanResourcesInterface() {
    @Override
    public void clean() throws ModuleException {
      // do nothing
    }
  };

  /**
   * Generic JDBC export module constructor
   *
   * @param driverClassName
   *          the name of the JDBC driver class
   * @param connectionURL
   *          the URL to use in connection
   */
  public JDBCExportModule(String driverClassName, String connectionURL) {
    this(driverClassName, connectionURL, new SQLHelper());
  }

  /**
   * Generic JDBC export module constructor with SQLHelper definition
   *
   * @param driverClassName
   *          the name of the JDBC driver class
   * @param connectionURL
   *          the URL to use in connection
   * @param sqlHelper
   *          the SQLHelper instance to use
   */
  public JDBCExportModule(String driverClassName, String connectionURL, SQLHelper sqlHelper) {
    // LOGGER.debug(driverClassName + ", " + connectionURL);
    this.driverClassName = driverClassName;
    this.connectionURL = connectionURL;
    this.sqlHelper = sqlHelper;
    this.connections = new HashMap<String, Connection>();
    this.canDropDatabase = DEFAULT_CAN_DROP_DATABASE;
    connection = null;
    statement = null;
    databaseStructure = null;
    currentTableStructure = null;
    batch_index = 0;
    currentRowBatchInsertStatement = null;
    ignoredSchemas = new HashSet<String>();
    existingSchemas = null;
    currentIsIgnoredSchema = false;
  }

  /**
   * Connect to the server using the properties defined in the constructor, or
   * return the existing connection
   *
   * @return the connection
   * @throws ModuleException
   *           This exception can be thrown if the JDBC driver class is not found
   *           or an SQL error occurs while connecting
   */
  public Connection getConnection() throws ModuleException {
    if (connection == null) {
      try {
        LOGGER.debug("Loading JDBC Driver " + driverClassName);
        Class.forName(driverClassName);
        LOGGER.debug("Getting connection");
        // LOGGER.debug("Connection URL: " + connectionURL);
        connection = DriverManager.getConnection(connectionURL);
        connection.setAutoCommit(true);
        LOGGER.debug("Connected");
      } catch (ClassNotFoundException e) {
        throw normalizeException(e, "JDBC driver class could not be found");
      } catch (SQLException e) {
        throw normalizeException(e, null);
      }
    }
    return connection;
  }

  /**
   * Get a connection to a database. This connection can be used to create the
   * database
   *
   * @param databaseName
   *          the name of the database to connect
   * @return the JDBC connection
   * @throws ModuleException
   */
  public Connection getConnection(String databaseName, String connectionURL) throws ModuleException {
    Connection connection = null;

    // re-use connection if it exists
    if (connections.containsKey(databaseName)) {
      connection = connections.get(databaseName);
      try {
        if (!connection.isClosed()) {
          return connection;
        } else {
          LOGGER.debug("Re-opening a closed connection to database {}", databaseName);
        }
      } catch (SQLException e) {
        LOGGER.debug("Error checking if connection is closed", e);
      }
    }

    // create it if it does not exist or has been closed
    try {
      LOGGER.debug("Database: {}", databaseName);
      LOGGER.debug("Loading JDBC Driver {}", driverClassName);
      Class.forName(driverClassName);
      LOGGER.debug("Getting admin connection");
      connection = DriverManager.getConnection(connectionURL);
      connection.setAutoCommit(true);
      LOGGER.debug("Connected");
      connections.put(databaseName, connection);
    } catch (ClassNotFoundException e) {
      throw normalizeException(e, "JDBC driver class could not be found");
    } catch (SQLException e) {
      throw normalizeException(e, "SQL error creating connection");
    }
    return connection;
  }

  /**
   * Check if a database exists
   *
   * @param defaultConnectionDb
   *          an existing dbml database to establish the connection
   * @param database
   *          the name of the database to check
   * @param connectionURL
   *          the connection URL needed by getConnection
   * @return true if exists, false otherwise
   * @throws ModuleException
   */
  public boolean databaseExists(String defaultConnectionDb, String database, String connectionURL)
    throws ModuleException {
    boolean found = false;
    ResultSet result = null;
    try {
      result = getConnection(defaultConnectionDb, connectionURL).createStatement()
        .executeQuery(sqlHelper.getDatabases(database));
      while (result.next() && !found) {
        if (result.getString(1).equalsIgnoreCase(database)) {
          found = true;
        }
      }
    } catch (SQLException e) {
      throw normalizeException(e, "Error checking if database " + database + " exists");
    } finally {
      if (result != null) {
        try {
          result.close();
        } catch (SQLException e) {

        }
      }
    }
    return found;
  }

  protected Statement getStatement() throws ModuleException {
    if (statement == null && getConnection() != null) {
      try {
        statement = getConnection().createStatement();
      } catch (SQLException e) {
        throw normalizeException(e, "SQL error creating statement");
      }
    }
    return statement;
  }

  /**
   * Gets custom settings set by the export module that modify behaviour of the
   * import module.
   *
   * @throws ModuleException
   */
  @Override
  public ModuleSettings getModuleSettings() throws ModuleException {
    return new ModuleSettings();
  }

  @Override
  public void initDatabase() throws ModuleException {
    LOGGER.debug("on init db");
    getConnection();
    // nothing to do
  }

  /**
   * Override this method to create the database
   *
   * @param dbName
   *          the database name
   * @throws ModuleException
   */
  protected void createDatabase(String dbName) throws ModuleException {
    // nothing will be done by default
  }

  @Override
  public void handleStructure(DatabaseStructure structure) throws ModuleException {
    this.databaseStructure = structure;
    try {
      this.existingSchemas = getExistingSchemasNames();
    } catch (SQLException e) {
      LOGGER.error("An error occurred while getting the name of existing schemas", e);
    }
    createDatabase(structure.getName());
    int[] batchResult = null;
    if (getStatement() != null) {
      for (SchemaStructure schema : structure.getSchemas()) {
        // won't export ignored schemas
        if (isIgnoredSchema(schema.getName())) {
          LOGGER.warn("Schema not exported because it's defined " + "as ignored (possibily is a system schema): "
            + schema.getName());
        } else {
          LOGGER.info("Exporting schema structure for schema " + schema.getName());
          handleSchemaStructure(schema);
          LOGGER.info("Exporting schema structure for schema " + schema.getName());
        }
      }
      LOGGER.debug("Executing table creation batch");
      statementExecuteAndClearBatch();
    }
  }

  protected Set<String> getDefaultSchemas() {
    return new HashSet<String>();
  }

  protected void handleSchemaStructure(SchemaStructure schema) throws ModuleException, UnknownTypeException {
    LOGGER.debug("Exporting schema structure for " + schema.getName());
    try {
      if (!isExistingSchema(schema.getName())) {
        statementAddBatch(sqlHelper.createSchemaSQL(schema));
        statementExecuteAndClearBatch();
        LOGGER.debug("batch executed: " + schema.getName());
      }

      for (TableStructure table : schema.getTables()) {
        handleTableStructure(table);
      }

      LOGGER.debug("Exporting schema structure " + schema.getName() + " completed");
    } catch (SQLException e) {
      LOGGER.error("Error exporting schema structure", e);
      throw normalizeException(e, "Error while adding schema SQL to batch");
    }
  }

  /**
   * Checks if a schema with 'schemaName' already exists on the database.
   *
   * @param schemaName
   *          the schema name to be checked.
   * @return
   * @throws SQLException
   * @throws ModuleException
   */
  protected boolean isExistingSchema(String schemaName) throws SQLException, ModuleException {
    boolean exists = false;
    for (String existingName : getExistingSchemasNames()) {
      if (existingName.equalsIgnoreCase(schemaName)) {
        exists = true;
        break;
      }
    }
    return exists;
  }

  /**
   * Gets the list of names of the existing schemas on a database.
   *
   * @return The list of schemas names on a database.
   * @throws SQLException
   * @throws ModuleException
   */
  protected Set<String> getExistingSchemasNames() throws SQLException, ModuleException {
    if (existingSchemas == null) {
      existingSchemas = new HashSet<String>();
      ResultSet rs = getConnection().getMetaData().getSchemas();
      while (rs.next()) {
        existingSchemas.add(rs.getString(1));
      }
    }
    return existingSchemas;
  }

  protected void handleTableStructure(TableStructure table) throws ModuleException, UnknownTypeException {
    if (getStatement() != null) {
      LOGGER.info("Exporting table structure for {}", table.getName());
      LOGGER.debug("Adding to batch creation of table {}", table.getName());

      String tableSQL = sqlHelper.createTableSQL(table);
      LOGGER.debug("SQL: {}", tableSQL);
      statementAddBatch(tableSQL);

      if (table.getPrimaryKey() != null) {
        // avoid primary key name conflicts
        String pkeyName = table.getPrimaryKey().getName();
        if (StringUtils.isBlank(pkeyName) || exportedPrimaryKeys.contains(pkeyName)) {
          pkeyName = "pkey_" + exportedPrimaryKeys.size();
        }
        exportedPrimaryKeys.add(pkeyName);
        table.getPrimaryKey().setName(pkeyName);

        // create the primary key
        String pkeySQL = sqlHelper.createPrimaryKeySQL(table.getId(), table.getPrimaryKey());
        if (pkeySQL != null) {
          LOGGER.debug("SQL: {}", pkeySQL);
          statementAddBatch(pkeySQL);
        }
      }
    }
  }

  /**
   * Sets the schemas to be ignored on the export. These schemas won't be exported
   *
   * @param ignoredSchemas
   *          ignored schemas name to be added to the list
   */
  @Override
  public void setIgnoredSchemas(Set<String> ignoredSchemas) {
    for (String s : ignoredSchemas) {
      this.ignoredSchemas.add(s);
    }
  }

  /**
   * Checks if a given schema is set to be ignored
   *
   * @param schema
   *          The schema structure to be checked
   * @return
   */
  protected boolean isIgnoredSchema(String schema) {
    for (String s : ignoredSchemas) {
      if (schema.matches(s)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void handleDataOpenSchema(String schemaName) throws ModuleException {
    currentIsIgnoredSchema = isIgnoredSchema(schemaName);
  }

  @Override
  public void handleDataOpenTable(String tableId) throws ModuleException {
    LOGGER.debug("Started data open: " + tableId);
    if (databaseStructure != null) {
      TableStructure table = databaseStructure.getTableById(tableId);
      this.currentTableStructure = table;
      if (currentTableStructure != null) {
        if (!currentIsIgnoredSchema) {
          try {
            getConnection().setAutoCommit(false);
            currentRowBatchInsertStatement = getConnection()
              .prepareStatement(sqlHelper.createRowSQL(currentTableStructure));
            currentRowBatchStartIndex = 0;
            LOGGER.debug("sql: " + sqlHelper.createRowSQL(currentTableStructure));

          } catch (SQLException e) {
            throw normalizeException(e, "Error creating table " + tableId + " prepared statement");
          }
        }
      } else {
        throw new ModuleException().withMessage("Could not find table id '" + tableId + "' in database structure");
      }
    } else {
      throw new ModuleException().withMessage("Cannot open table before database structure is created");
    }
  }

  @Override
  public void handleDataCloseTable(String tableId) throws ModuleException {
    currentTableStructure = null;
    if (batch_index > 0) {
      try {
        currentRowBatchInsertStatement.executeBatch();
      } catch (SQLException e) {
        LOGGER.error("Error closing table {}", tableId, e);
      }
      batch_index = 0;
      currentIsIgnoredSchema = false;
      cleanAndClearResources(cleanResourcesList);
    }

    try {
      commit();
    } catch (SQLException e) {
      LOGGER.error("Could not commit data insertion for table " + tableId, e);
    }

    try {
      currentRowBatchInsertStatement.close();
    } catch (SQLException e) {
      LOGGER.debug("Failed to close prepared statement", e);
    }
    currentRowBatchInsertStatement = null;
  }

  @Override
  public void handleDataCloseSchema(String schemaName) throws ModuleException {
    // do nothing
  }

  @Override
  public void handleDataRow(Row row) throws ModuleException {
    if (!currentIsIgnoredSchema) {
      if (currentTableStructure != null && currentRowBatchInsertStatement != null) {
        Iterator<ColumnStructure> columnIterator = currentTableStructure.getColumns().iterator();
        int index = 1;
        for (Cell cell : row.getCells()) {
          ColumnStructure column = columnIterator.next();
          CleanResourcesInterface cleanResources = handleDataCell(currentRowBatchInsertStatement, index, cell, column);
          cleanResourcesList.add(cleanResources);
          index++;
        }

        long currentRowBatchEndIndex = row.getIndex();
        try {
          currentRowBatchInsertStatement.addBatch();
          if (++batch_index > BATCH_SIZE) {
            batch_index = 0;
            currentRowBatchInsertStatement.executeBatch();
            currentRowBatchInsertStatement.clearBatch();
            commit();
          }
        } catch (SQLException e) {
          LOGGER.error("Error executing part of a batch of queries.");
          LOGGER.debug("This is the SQLException@{} for the previous error.", e.hashCode(), e);
          if (e.getNextException() != null) {
            LOGGER.debug("This is the corresponding SQLException@{}.getNextException", e.hashCode(),
              e.getNextException());
          }

          reporter.failed(
            "In table `" + currentTableStructure.getId() + "`, inserting rows with index from "
              + currentRowBatchStartIndex + " to " + currentRowBatchEndIndex + " ",
            " there was an error with at least one of the rows");
        } finally {
          if (batch_index == 0) {
            cleanAndClearResources(cleanResourcesList);
            currentRowBatchStartIndex = currentRowBatchEndIndex + 1;
          }
        }
      } else if (databaseStructure != null) {
        throw new ModuleException()
          .withMessage("Cannot build data row before a table is open and insert statement created");
      }
    }
  }

  private void cleanAndClearResources(List<CleanResourcesInterface> resourcesList) {
    for (CleanResourcesInterface clean : resourcesList) {
      try {
        clean.clean();
      } catch (ModuleException e) {
        LOGGER.debug("Ignored CleanResourcesInterface.clean exception: ", e);
      }
    }
    resourcesList.clear();
  }

  protected CleanResourcesInterface handleDataCell(PreparedStatement ps, int index, Cell cell, ColumnStructure column)
    throws InvalidDataException, ModuleException {
    CleanResourcesInterface ret = noOpCleanResourcesInterface;
    Type type = column.getType();
    try {
      // TODO: better null handling
      if (cell instanceof NullCell) {
        cell = new SimpleCell(cell.getId(), null);
      }

      if (cell instanceof SimpleCell) {
        SimpleCell simple = (SimpleCell) cell;
        String data = simple.getSimpleData();
        // LOGGER.debug("data: " + data);
        // LOGGER.debug("type: " + type.getOriginalTypeName());
        if (type instanceof SimpleTypeString) {
          handleSimpleTypeStringDataCell(data, ps, index, cell, column);
        } else if (type instanceof SimpleTypeNumericExact) {
          handleSimpleTypeNumericExactDataCell(data, ps, index, cell, column);
        } else if (type instanceof SimpleTypeNumericApproximate) {
          handleSimpleTypeNumericApproximateDataCell(data, ps, index, cell, column);
        } else if (type instanceof SimpleTypeDateTime) {
          handleSimpleTypeDateTimeDataCell(data, ps, index, cell, column);
        } else if (type instanceof SimpleTypeBoolean) {
          handleSimpleTypeBooleanDataCell(data, ps, index, cell, column);
        } else if (type instanceof UnsupportedDataType) {
          handleSimpleTypeStringDataCell(data, ps, index, cell, column);
        } else if (type instanceof SimpleTypeBinary) {
          if (data != null) {
            ps.setString(index, data);
          } else {
            ps.setNull(index, Types.BINARY);
          }
        } else {
          throw new InvalidDataException(
            type.getClass().getSimpleName() + " not applicable to simple cell or " + "not yet supported");
        }
      } else if (cell instanceof BinaryCell) {
        final BinaryCell bin = (BinaryCell) cell;

        if (type instanceof SimpleTypeBinary) {
          final InputStream inputStream = bin.createInputStream();
          ps.setBinaryStream(index, inputStream, bin.getSize());
          ret = new CleanResourcesInterface() {
            @Override
            public void clean() throws ModuleException {
              IOUtils.closeQuietly(inputStream);
              bin.cleanResources();
            }
          };
        } else if (type instanceof SimpleTypeString) {
          final InputStream inputStream = handleSimpleTypeString(ps, index, bin, column);
          ret = new CleanResourcesInterface() {
            @Override
            public void clean() throws ModuleException {
              IOUtils.closeQuietly(inputStream);
              bin.cleanResources();
            }
          };
        } else {
          LOGGER.error("Binary cell found when column type is " + type.getClass().getSimpleName());
        }

      } else if (cell instanceof ArrayCell) {
        ArrayCell arrayCell = (ArrayCell) cell;
        handleComposedTypeArrayDataCell(arrayCell, ps, index, column);
      } else if (cell instanceof ComposedCell) {
        // ComposedCell comp = (ComposedCell) cell;
        // TODO export composed data
        throw new ModuleException().withMessage("Composed data not yet supported");
      } else {
        throw new ModuleException().withMessage("Unsuported cell type " + cell.getClass().getName());
      }
    } catch (SQLException e) {
      throw normalizeException(e, "SQL error while handling cell " + cell.getId());
    }
    return ret;
  }

  protected void handleComposedTypeArrayDataCell(ArrayCell arrayCell, PreparedStatement ps, int index,
    ColumnStructure column) throws SQLException, ModuleException {
    throw new ModuleException().withMessage("Arrays are not supported for this DBMS");
  }

  protected void handleSimpleTypeStringDataCell(String data, PreparedStatement ps, int index, Cell cell,
    ColumnStructure column) throws SQLException, ModuleException {
    if (data != null) {
      ps.setString(index, data);
    } else {
      ps.setNull(index, Types.VARCHAR);
    }
  }

  protected void handleSimpleTypeNumericExactDataCell(String data, PreparedStatement ps, int index, Cell cell,
    ColumnStructure column) throws NumberFormatException, SQLException, ModuleException {
    if (data != null) {
      // LOGGER.debug("big decimal: " + data);
      BigDecimal bd = new BigDecimal(data);
      ps.setBigDecimal(index, bd);
    } else {
      ps.setNull(index, Types.INTEGER);
    }
  }

  protected void handleSimpleTypeNumericApproximateDataCell(String data, PreparedStatement ps, int index, Cell cell,
    ColumnStructure column) throws NumberFormatException, SQLException, ModuleException {
    if (data != null) {
      ps.setString(index, data);
    } else {
      ps.setNull(index, Types.FLOAT);
    }
  }

  protected void handleSimpleTypeDateTimeDataCell(String data, PreparedStatement ps, int index, Cell cell,
    ColumnStructure column) throws SQLException, ModuleException {
    SimpleTypeDateTime type = (SimpleTypeDateTime) column.getType();
    if (type.getTimeDefined()) {
      if ("TIMESTAMP".equalsIgnoreCase(type.getSql99TypeName())
        || "TIMESTAMP WITH TIME ZONE".equalsIgnoreCase(type.getSql99TypeName())) {
        if (data != null) {
          // LOGGER.debug("timestamp before: " + data);
          Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(data);
          Timestamp sqlTimestamp = new Timestamp(cal.getTimeInMillis());
          LOGGER.trace("timestamp after: " + sqlTimestamp.toString());
          ps.setTimestamp(index, sqlTimestamp);
        } else {
          ps.setNull(index, Types.TIMESTAMP);
        }
      } else {
        if (data != null) {
          // LOGGER.debug("TIME before: " + data);
          Time sqlTime = Time.valueOf(data);
          // LOGGER.debug("TIME after: " + sqlTime.toString());
          ps.setTime(index, sqlTime);
        } else {
          ps.setNull(index, Types.TIME);
        }
      }
    } else {
      if (data != null) {
        // LOGGER.debug("DATE before: " + data);
        java.sql.Date sqlDate = java.sql.Date.valueOf(data);
        // LOGGER.debug("DATE after: " + sqlDate.toString());
        ps.setDate(index, sqlDate);
      } else {
        ps.setNull(index, Types.DATE);
      }
    }
  }

  protected void handleSimpleTypeBooleanDataCell(String data, PreparedStatement ps, int index, Cell cell,
    ColumnStructure column) throws SQLException, ModuleException {
    if (data != null) {
      // LOGGER.debug("boolData: " + data);
      ps.setBoolean(index, Boolean.valueOf(data));
    } else {
      ps.setNull(index, Types.BOOLEAN);
    }
  }

  /**
   * @return the created InputStream, so it can be closed.
   */
  protected InputStream handleSimpleTypeString(PreparedStatement ps, int index, BinaryCell bin, ColumnStructure column)
    throws SQLException, ModuleException {
    InputStream inputStream = bin.createInputStream();
    ps.setClob(index, new InputStreamReader(inputStream), bin.getSize());
    return inputStream;
  }

  @Override
  public void finishDatabase() throws ModuleException {
    if (databaseStructure != null) {
      try {
        commit();
        getConnection().setAutoCommit(true);
      } catch (SQLException e) {
        throw normalizeException(e, "Could not enable autocommit before creating foreign keys");
      }
      handleForeignKeys();
    }
    closeConnections();
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
  }

  public void closeConnections() throws ModuleException {
    for (Map.Entry<String, Connection> databaseConnectionEntry : connections.entrySet()) {
      try {
        databaseConnectionEntry.getValue().close();
      } catch (SQLException e) {
        LOGGER.debug("Could not close connection to database '{}'", databaseConnectionEntry.getKey());
      }
    }

    if (connection != null) {
      LOGGER.debug("Closing connection");
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        throw normalizeException(e, "Error while closing connection");
      }
    }
  }

  protected void handleForeignKeys() throws ModuleException {
    LOGGER.debug("Adding foreign keys");
    for (SchemaStructure schema : databaseStructure.getSchemas()) {
      if (isIgnoredSchema(schema.getName())) {
        continue;
      }
      for (TableStructure table : schema.getTables()) {

        LOGGER.info("Adding foreign keys for table " + table.getId());
        for (ForeignKey fkey : table.getForeignKeys()) {
          String originalReferencedSchema = fkey.getReferencedSchema();

          String tableId = originalReferencedSchema + "." + fkey.getReferencedTable();

          TableStructure tableAux = databaseStructure.getTableById(tableId);
          if (tableAux != null) {
            if (isIgnoredSchema(tableAux.getSchema())) {
              LOGGER.debug("Foreign key not exported: referenced schema (" + fkey.getReferencedSchema()
                + ") is ignored at export.");
              continue;
            }
          }

          String fkeySQL = sqlHelper.createForeignKeySQL(table, fkey);
          LOGGER.debug("Foreign key SQL: " + fkeySQL);
          statementAddBatch(fkeySQL);
        }
        statementExecuteAndClearBatch();
      }
    }
    statementExecuteAndClearBatch();
    LOGGER.info("Finished adding foreign keys");
  }

  protected void commit() throws SQLException {
    LOGGER.trace("Committing");
    try {
      getConnection().commit();
    } catch (ModuleException e) {
      LOGGER.debug("Module exception obtaining the connection to commit.", e);
    }
  }

  /**
   * Get the SQLHelper used by this instance
   *
   * @return the SQLHelper
   */
  public SQLHelper getSqlHelper() {
    return sqlHelper;
  }

  public interface CleanResourcesInterface {
    void clean() throws ModuleException;
  }

  /**
   * Executes the queries in the batch, recovering from failures as best as
   * possible and ensuring that all queries in the batch are executed.
   * 
   * @throws ModuleException
   */
  protected void statementExecuteAndClearBatch() throws ModuleException {
    Statement statement = getStatement();
    int[] result;
    String reasonForFailing;

    while (!batchSQL.isEmpty()) {
      // error handling
      result = null;
      reasonForFailing = null;

      try {
        // best case scenario, all queries run OK
        statement.executeBatch();
        batchSQL.clear();
        break;
      } catch (BatchUpdateException e) {
        // not-good case scenario, some queries fail
        result = e.getUpdateCounts();
        reasonForFailing = e.getMessage();
        LOGGER.debug("Got a batch update exception while executing a batch statement", e);

        // build next-exceptions
        int maxNextException = 10;
        SQLException sqlException = e.getNextException();
        while (sqlException != null && maxNextException >= 0) {
          // log exception
          LOGGER.debug("Next exception", sqlException);
          // go deeper
          sqlException = sqlException.getNextException();
          maxNextException--;
        }

        // some implementations continue running the queries in the batch,
        // others do not. for those which do, at least one element in the result
        // array will have a value of EXECUTE_FAILED. Dealing with that here:

        // find out what failed, warn about it
        int successes = 0;
        for (int i = result.length - 1; i >= 0; i--) {
          if (result[i] > 0 || result[i] == Statement.SUCCESS_NO_INFO) {
            batchSQL.remove(i);
            successes++;
          } else if (result[i] == Statement.EXECUTE_FAILED) {
            String failedQuery = batchSQL.get(i);
            batchSQL.remove(i);
            LOGGER.error("Error executing query: " + failedQuery);
            reporter.failed("Execution of query ``" + failedQuery + "``",
              "of the following error: " + reasonForFailing);
          } else {
            String strangeQuery = batchSQL.get(i);
            batchSQL.remove(i);
            LOGGER.debug("Error executing query: " + strangeQuery,
              new ModuleException().withMessage("Query returned result of " + result[i]));
            reporter.failed("Execution of query ``" + strangeQuery + "``",
              "of the following error: " + reasonForFailing);
          }
        }

        // some implementations stop running the queries in the batch when a
        // query fails. returning an array with a length equal to the number of
        // queries that executed successfully. If this happens, then the next
        // query is the problematic one, and is removed from the batch before
        // retrying
        if (successes == result.length && batchSQL.size() > 0) {
          String failedQuery = batchSQL.get(0);
          batchSQL.remove(0);
          LOGGER.error("Error executing query: " + failedQuery);
          reporter.failed("Execution of query ``" + failedQuery + "``", "of the following error: " + reasonForFailing);
        }

        // clear batch and re-add queries that were left out
        if (!batchSQL.isEmpty()) {
          List<String> notExecuted = new ArrayList<>(batchSQL);
          batchSQL.clear();
          try {
            statement.clearBatch();
          } catch (SQLException e1) {
            LOGGER.debug("Connection to database stopped working", e1);
          }

          for (String query : notExecuted) {
            statementAddBatch(query);
          }
        }
      } catch (SQLException e) {
        // worst case scenario, something else failed
        throw normalizeException(e, "Error executing batch statement");
      }
    }

    // everything ran. clear the batch
    try {
      statement.clearBatch();
    } catch (SQLException e) {
      throw normalizeException(e, "Connection to database stopped working");
    }
  }

  protected void statementAddBatch(String sql) throws ModuleException {
    try {
      getStatement().addBatch(sql);
      batchSQL.add(sql);
    } catch (SQLException e) {
      throw normalizeException(e, "Could not add SQL to batch '" + sql + "'");
    }
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }
}
