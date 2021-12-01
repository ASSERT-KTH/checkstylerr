package com.qihoo.qsql.metadata.collect;

import com.qihoo.qsql.metadata.collect.dto.JdbcProp;
import com.qihoo.qsql.metadata.entity.ColumnValue;
import com.qihoo.qsql.metadata.entity.DatabaseParamValue;
import com.qihoo.qsql.metadata.entity.DatabaseValue;
import com.qihoo.qsql.metadata.entity.TableValue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class OracleCollector extends MetadataCollector {

    private JdbcProp prop;
    private Connection connection;
    OracleCollector(JdbcProp prop, String filterRegexp) throws SQLException, ClassNotFoundException {
        super(filterRegexp);
        this.prop = prop;
        Class.forName(prop.getJdbcDriver());
        connection = DriverManager.getConnection(prop.getJdbcUrl(),
            prop.getJdbcUser(), prop.getJdbcPassword());
    }

    @Override
    protected DatabaseValue convertDatabaseValue() {
        DatabaseValue value = new DatabaseValue();
        value.setDbType("oracle");
        value.setDesc("Who am I");
        value.setName(getDatabasePosition());
        return value;
    }

    @Override
    protected List<DatabaseParamValue> convertDatabaseParamValue(Long dbId) {
        DatabaseParamValue[] paramValues = new DatabaseParamValue[4];
        for (int i = 0; i < paramValues.length; i++) {
            paramValues[i] = new DatabaseParamValue(dbId);
        }
        paramValues[0].setParamKey("jdbcUrl").setParamValue(prop.getJdbcUrl());
        paramValues[1].setParamKey("jdbcDriver").setParamValue(prop.getJdbcDriver());
        paramValues[2].setParamKey("jdbcUser").setParamValue(prop.getJdbcUser());
        paramValues[3].setParamKey("jdbcPassword").setParamValue(prop.getJdbcPassword());
        return Arrays.stream(paramValues).collect(Collectors.toList());
    }

    @Override
    protected TableValue convertTableValue(Long dbId, String tableName) {
        TableValue value = new TableValue();
        value.setTblName(tableName);
        value.setDbId(dbId);
        value.setCreateTime(new Date().toString());
        return value;
    }

    @Override
    protected List<ColumnValue> convertColumnValue(Long tbId, String tableName, String dbName) {
        String sql = String.format("SELECT COLUMN_NAME, DATA_TYPE, COLUMN_ID "
                + "FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '%s'", tableName);
        List<ColumnValue> columns = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ColumnValue value = new ColumnValue();
                value.setColumnName(resultSet.getString(1));
                value.setTypeName(resultSet.getString(2));
                value.setCdId(tbId);
                value.setIntegerIdx(resultSet.getInt(3));
                value.setComment("Who am I");
                columns.add(value);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return columns;
    }

    @Override
    protected List<String> getTableNameList() {
        if (StringUtils.isEmpty(filterRegexp)) {
            throw new RuntimeException("`Filter regular expression` needed to be set");
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format("SELECT table_name FROM all_tables "
                + "WHERE table_name LIKE '%s'", filterRegexp))) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
            return tableNames;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDatabasePosition() {
        try (PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT name FROM v$database")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (! resultSet.next()) {
                throw new RuntimeException("Execute `SELECT name FROM v$database` failed!!");
            }
            String database = resultSet.getString(1);
            if (Objects.isNull(database)) {
                throw new RuntimeException("Please add db_name in `jdbcUrl`");
            }
            return database;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
