package com.databasepreservation.modules.siard.in.metadata.typeConverter;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.type.Type;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SQL99StandardDatatypeImporter extends SQLStandardDatatypeImporter {
  private static final Logger LOGGER = LoggerFactory.getLogger(SQL99StandardDatatypeImporter.class);

  @Override
  protected Type getType(DatabaseStructure database, SchemaStructure currentSchema, String tableName,
    String columnName, int dataType, String sql99TypeName, int columnSize, int decimalDigits, int numPrecRadix)
    throws UnknownTypeException, SQLException, ClassNotFoundException {

    SqlStandardType standardType = new SqlStandardType(sql99TypeName);
    String typeNameWithoutParameters = standardType.base + standardType.typeTimezonePart;

    LOGGER.debug("name: {}, stdType: {}", typeNameWithoutParameters, standardType);

    Type type;
    switch (typeNameWithoutParameters) {
      case "CHARACTER":
      case "CHAR":
        type = getCharType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "NATIONAL CHARACTER":
      case "NATIONAL CHAR":
      case "NCHAR":
        type = getNationalCharType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "CHARACTER VARYING":
      case "VARCHAR":
        type = getVarcharType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "NATIONAL CHARACTER VARYING":
      case "NATIONAL CHAR VARYING":
      case "NCHAR VARYING":
        type = getNationalVarcharType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "CHARACTER LARGE OBJECT":
      case "CHAR LARGE OBJECT":
      case "CLOB":
      case "NATIONAL CHARACTER LARGE OBJECT":
      case "NCHAR LARGE OBJECT":
      case "NCLOB":
        type = getClobType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "BINARY VARYING":
      case "VARBINARY":
      case "BIT VARYING":
        type = getVarbinaryType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "BINARY":
      case "BIT":
        type = getBinaryType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "BINARY LARGE OBJECT":
      case "BLOB":
        type = getBlobType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "NUMERIC":
        type = getNumericType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "DECIMAL":
      case "DEC":
        type = getDecimalType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "SMALLINT":
        type = getSmallIntType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "INTEGER":
      case "INT":
        type = getIntegerType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "BIGINT":
        type = getBigIntType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "FLOAT":
        type = getFloatType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "REAL":
        type = getRealType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "DOUBLE PRECISION":
        type = getDoubleType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "BOOLEAN":
        type = getBooleanType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "DATE":
        type = getDateType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "TIME":
      case "TIME WITH TIME ZONE":
      case "TIME WITHOUT TIME ZONE":
        type = getTimeType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      case "TIMESTAMP":
      case "TIMESTAMP WITH TIME ZONE":
      case "TIMESTAMP WITHOUT TIME ZONE":
        type = getTimestampType(typeNameWithoutParameters, columnSize, decimalDigits, numPrecRadix);
        break;

      default:
        type = getFallbackType(sql99TypeName);
    }

    // exceptions
    // TODO: add support for more types
    // TODO: support charsets

    if (StringUtils.isBlank(type.getSql99TypeName(false))) {
      type.setSql99TypeName(sql99TypeName);
    }

    if (StringUtils.isBlank(type.getSql2008TypeName(false))) {
      // TODO: improve conversion from sql99 to sql2008
      String sql2008TypeName;

      switch (typeNameWithoutParameters) {
        case "BIT":
          sql2008TypeName = "BINARY";
          if (standardType.hasColumnSize) {
            sql2008TypeName += "(" + standardType.columnSize + ")";
          }
          break;
        case "BIT VARYING":
          sql2008TypeName = "BINARY VARYING";
          if (standardType.hasColumnSize) {
            sql2008TypeName += "(" + standardType.columnSize + ")";
          }
          break;
        default:
          sql2008TypeName = sql99TypeName;
      }

      type.setSql2008TypeName(sql2008TypeName);
    }

    if (StringUtils.isBlank(type.getOriginalTypeName())) {
      type.setOriginalTypeName(standardType.original);
    }

    return type;
  }
}
