package com.databasepreservation.modules.db2.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.ForeignKey;
import com.databasepreservation.modules.db2.DB2Helper;
import com.databasepreservation.modules.jdbc.in.JDBCImportModule;

/**
 * @author Miguel Coutada
 */
public class DB2JDBCImportModule extends JDBCImportModule {

  private List<String> aliasTables = null;

  private String dbName;

  /**
   * Db2 JDBC import module constructor
   *
   * @param hostname
   *          the hostname of the Db2 server
   * @param port
   *          the port that the Db2 server is listening
   * @param database
   *          the name of the database to import from
   * @param username
   *          the name of the user to use in connection
   * @param password
   *          the password of the user to use in connection
   */
  public DB2JDBCImportModule(String hostname, int port, String database, String username, String password) {
    super("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://" + hostname + ":" + port + "/" + database + ":user=" + username
      + ";password=" + password + ";", new DB2Helper(), new DB2JDBCDatatypeImporter());
    dbName = database;
  }

  /**
   * @return the database structure
   * @throws SQLException
   * @throws UnknownTypeException
   *           the original data type is unknown
   * @throws ClassNotFoundException
   */
  @Override
  protected DatabaseStructure getDatabaseStructure() throws SQLException {
    if (dbStructure == null) {
      dbStructure = super.getDatabaseStructure();
      dbStructure.setName(dbName);
    }
    return dbStructure;
  }

  /**
   * Returns the default ignored schemas for DB2 These schemas won't be imported
   */
  @Override
  protected Set<String> getIgnoredImportedSchemas() {
    Set<String> ignored = new HashSet<String>();
    ignored.add("SQLJ");
    ignored.add("NULLID");
    ignored.add("SYSCAT");
    ignored.add("SYSFUN");
    ignored.add("SYSIBM");
    ignored.add("SYSIBMADM");
    ignored.add("SYSIBMINTERNAL");
    ignored.add("SYSIBMTS");
    ignored.add("SYSPROC");
    ignored.add("SYSPUBLIC");
    ignored.add("SYSSTAT");
    ignored.add("SYSTOOLS");
    return ignored;
  }

  /**
   * @return the db2 database alias tables
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  protected List<String> getAliasTables() throws SQLException, ClassNotFoundException {
    List<String> aliasTables = new ArrayList<String>();

    ResultSet rset = getMetadata().getTables(dbStructure.getName(), null, "%", new String[] {"ALIAS"});
    while (rset.next()) {
      aliasTables.add(rset.getString("TABLE_NAME"));
    }
    return aliasTables;
  }

  /**
   * @param tableName
   *          the table name
   * @return the foreign keys
   * @throws SQLException
   * @throws UnknownTypeException
   * @throws ClassNotFoundException
   */
  // VERIFY Need of custom getForeignKeys
  protected List<ForeignKey> getForeignKeys(String tableName) throws SQLException, UnknownTypeException,
    ClassNotFoundException {
    List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
    if (aliasTables == null) {
      aliasTables = getAliasTables();
    }

    ResultSet rs = getMetadata().getImportedKeys(getDatabaseStructure().getName(), null, tableName);

    while (rs.next()) {
      String name = rs.getString(8);
      String refTable = rs.getString(3);
      String refColumn = rs.getString(4);
      if (!aliasTables.contains(refTable)) {
        ForeignKey fk = new ForeignKey(tableName + "." + name, name, refTable, refColumn);
        foreignKeys.add(fk);
      }
    }
    return foreignKeys;
  }

  @Override
  protected String processTriggerEvent(String string) {
    String res = "";
    if ("I".equals(string)) {
      res = "INSERT";
    } else if ("U".equals(string)) {
      res = "UPDATE";
    } else if ("D".equals(string)) {
      res = "DELETE";
    }
    return res;
  }

  @Override
  protected String processActionTime(String string) {
    String res = "";
    if ("B".equals(string)) {
      res = "BEFORE";
    } else if ("A".equals(string)) {
      res = "AFTER";
    } else if ("I".equals(string)) {
      res = "INSTEAD OF";
    }
    return res;
  }
}
