package com.databasepreservation.modules.siard.out.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.structure.CandidateKey;
import com.databasepreservation.model.structure.CheckConstraint;
import com.databasepreservation.model.structure.ColumnStructure;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.ForeignKey;
import com.databasepreservation.model.structure.Parameter;
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
import com.databasepreservation.model.structure.type.ComposedTypeStructure;
import com.databasepreservation.modules.siard.SIARDHelper;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;
import com.databasepreservation.modules.siard.common.path.MetadataPathStrategy;
import com.databasepreservation.modules.siard.out.content.Sql99toXSDType;
import com.databasepreservation.modules.siard.out.path.ContentPathExportStrategy;
import com.databasepreservation.modules.siard.out.write.WriteStrategy;
import com.databasepreservation.utils.JodaUtils;
import com.databasepreservation.utils.XMLUtils;

import ch.admin.bar.xmlns.siard._1_0.metadata.ActionTimeType;
import ch.admin.bar.xmlns.siard._1_0.metadata.CandidateKeyType;
import ch.admin.bar.xmlns.siard._1_0.metadata.CandidateKeysType;
import ch.admin.bar.xmlns.siard._1_0.metadata.CheckConstraintType;
import ch.admin.bar.xmlns.siard._1_0.metadata.CheckConstraintsType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ColumnType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ColumnsType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ForeignKeyType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ForeignKeysType;
import ch.admin.bar.xmlns.siard._1_0.metadata.MatchTypeType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ParameterType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ParametersType;
import ch.admin.bar.xmlns.siard._1_0.metadata.PrimaryKeyType;
import ch.admin.bar.xmlns.siard._1_0.metadata.PrivOptionType;
import ch.admin.bar.xmlns.siard._1_0.metadata.PrivilegeType;
import ch.admin.bar.xmlns.siard._1_0.metadata.PrivilegesType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ReferenceType;
import ch.admin.bar.xmlns.siard._1_0.metadata.RoleType;
import ch.admin.bar.xmlns.siard._1_0.metadata.RolesType;
import ch.admin.bar.xmlns.siard._1_0.metadata.RoutineType;
import ch.admin.bar.xmlns.siard._1_0.metadata.RoutinesType;
import ch.admin.bar.xmlns.siard._1_0.metadata.SchemaType;
import ch.admin.bar.xmlns.siard._1_0.metadata.SchemasType;
import ch.admin.bar.xmlns.siard._1_0.metadata.SiardArchive;
import ch.admin.bar.xmlns.siard._1_0.metadata.TableType;
import ch.admin.bar.xmlns.siard._1_0.metadata.TablesType;
import ch.admin.bar.xmlns.siard._1_0.metadata.TriggerType;
import ch.admin.bar.xmlns.siard._1_0.metadata.TriggersType;
import ch.admin.bar.xmlns.siard._1_0.metadata.UserType;
import ch.admin.bar.xmlns.siard._1_0.metadata.UsersType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ViewType;
import ch.admin.bar.xmlns.siard._1_0.metadata.ViewsType;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARD1MetadataExportStrategy implements MetadataExportStrategy {
  private static final String ENCODING = "UTF-8";
  private static final String METADATA_FILENAME = "metadata";
  private static final String METADATA_RESOURCE_FILENAME = "siard1-metadata";
  private static final Logger LOGGER = LoggerFactory.getLogger(SIARD1MetadataExportStrategy.class);
  private final ContentPathExportStrategy contentPathStrategy;
  private final MetadataPathStrategy metadataPathStrategy;

  public SIARD1MetadataExportStrategy(MetadataPathStrategy metadataPathStrategy, ContentPathExportStrategy paths) {
    this.contentPathStrategy = paths;
    this.metadataPathStrategy = metadataPathStrategy;
  }

  @Override
  public void writeMetadataXML(DatabaseStructure dbStructure, SIARDArchiveContainer container,
    WriteStrategy writeStrategy) throws ModuleException {
    JAXBContext context;
    try {
      context = JAXBContext.newInstance(SiardArchive.class.getPackage().getName());
    } catch (JAXBException e) {
      throw new ModuleException("Error loading JAXBContext", e);
    }

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema xsdSchema = null;
    try {
      xsdSchema = schemaFactory.newSchema(new StreamSource(SiardArchive.class.getResourceAsStream(metadataPathStrategy
        .getXsdResourcePath(METADATA_RESOURCE_FILENAME))));
    } catch (SAXException e) {
      throw new ModuleException("XSD file has errors: "
        + metadataPathStrategy.getXsdResourcePath(METADATA_RESOURCE_FILENAME), e);
    }

    SiardArchive xmlroot = jaxbSiardArchive(dbStructure);
    Marshaller m;
    try {
      m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      m.setProperty(Marshaller.JAXB_ENCODING, ENCODING);
      m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
        "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd metadata.xsd");

      m.setSchema(xsdSchema);
      OutputStream writer = writeStrategy.createOutputStream(container,
        metadataPathStrategy.getXmlFilePath(METADATA_FILENAME));
      m.marshal(xmlroot, writer);
      writer.close();
    } catch (JAXBException e) {
      throw new ModuleException("Error while Marshalling JAXB", e);
    } catch (IOException e) {
      throw new ModuleException("Error while closing the data writer", e);
    }
  }

  @Override
  public void writeMetadataXSD(DatabaseStructure dbStructure, SIARDArchiveContainer container,
    WriteStrategy writeStrategy) throws ModuleException {
    // prepare to write
    OutputStream out = writeStrategy.createOutputStream(container,
      metadataPathStrategy.getXsdFilePath(METADATA_FILENAME));

    // prepare to read
    InputStream in = SiardArchive.class.getResourceAsStream(metadataPathStrategy
      .getXsdResourcePath(METADATA_RESOURCE_FILENAME));

    // read everything from reader into writer
    try {
      IOUtils.copy(in, out);
    } catch (IOException e) {
      throw new ModuleException("Could not write " + metadataPathStrategy.getXsdFilePath(METADATA_FILENAME)
        + " in container " + container.toString(), e);
    }

    // close input
    try {
      in.close();
    } catch (IOException e) {
      throw new ModuleException("Could not close stream", e);
    }

    // close output
    try {
      out.close();
    } catch (IOException e) {
      throw new ModuleException("Could not close stream", e);
    }
  }

  private SiardArchive jaxbSiardArchive(DatabaseStructure db) throws ModuleException {
    SiardArchive elem = new SiardArchive();
    elem.setArchivalDate(JodaUtils.xsDateFormat(db.getArchivalDate()));

    elem.setVersion("1.0");

    if (StringUtils.isNotBlank(db.getName())) {
      elem.setDbname(db.getName());
    } else {
      throw new ModuleException("Error while exporting structure: dbname cannot be blank");
    }

    if (StringUtils.isNotBlank(db.getDescription())) {
      elem.setDescription(db.getDescription());
    }

    if (StringUtils.isNotBlank(db.getArchiver())) {
      elem.setArchiver(db.getArchiver());
    }

    if (StringUtils.isNotBlank(db.getArchiverContact())) {
      elem.setArchiverContact(db.getArchiverContact());
    }

    if (StringUtils.isNotBlank(db.getDataOwner())) {
      elem.setDataOwner(db.getDataOwner());
    } else {
      throw new ModuleException("Error while exporting structure: data owner cannot be blank");
    }

    if (StringUtils.isNotBlank(db.getDataOriginTimespan())) {
      elem.setDataOriginTimespan(db.getDataOriginTimespan());
    } else {
      throw new ModuleException("Error while exporting structure: data origin timestamp cannot be blank");
    }

    if (StringUtils.isNotBlank(db.getProducerApplication())) {
      elem.setProducerApplication(db.getProducerApplication());
    }

    if (db.getArchivalDate() != null) {
      elem.setArchivalDate(JodaUtils.xsDateFormat(db.getArchivalDate()));
    }

    // TODO: use some kind of message digest
    elem.setMessageDigest("");

    if (StringUtils.isNotBlank(db.getProductName())) {
      if (StringUtils.isBlank(db.getProductVersion())) {
        elem.setDatabaseProduct(db.getProductName());
      } else {
        elem.setDatabaseProduct(db.getProductName() + " " + db.getProductVersion());
      }
    }

    if (StringUtils.isNotBlank(db.getUrl())) {
      elem.setConnection(db.getUrl());
    }

    if (StringUtils.isNotBlank(db.getDatabaseUser())) {
      elem.setDatabaseUser(db.getDatabaseUser());
    }

    if (StringUtils.isNotBlank(db.getClientMachine())) {
      elem.setClientMachine(db.getClientMachine());
    }

    elem.setSchemas(jaxbSchemasType(db.getSchemas()));
    elem.setUsers(jaxbUsersType(db.getUsers()));
    elem.setRoles(jaxbRolesType(db.getRoles()));
    elem.setPrivileges(jaxbPrivilegesType(db.getPrivileges()));

    return elem;
  }

  private PrivilegesType jaxbPrivilegesType(List<PrivilegeStructure> privileges) throws ModuleException {
    if (privileges != null && !privileges.isEmpty()) {
      PrivilegesType privilegesType = new PrivilegesType();
      for (PrivilegeStructure privilege : privileges) {
        privilegesType.getPrivilege().add(jaxbPrivilegeType(privilege));
      }
      return privilegesType;
    } else {
      return null;
    }
  }

  private PrivilegeType jaxbPrivilegeType(PrivilegeStructure privilege) throws ModuleException {
    PrivilegeType privilegeType = new PrivilegeType();

    if (StringUtils.isNotEmpty(privilege.getType())) {
      privilegeType.setType(privilege.getType());
    } else {
      throw new ModuleException("Error while exporting users structure: privilege type cannot be blank");
    }

    if (StringUtils.isNotEmpty(privilege.getObject())) {
      privilegeType.setObject(privilege.getObject());
    } else {
      privilegeType.setObject("unknown object");
      // LOGGER.warn("Could not export privilege object");
      // TODO: check in which circumstances this happens
      throw new ModuleException("Error while exporting users structure: privilege object cannot be blank");
    }

    if (StringUtils.isNotBlank(privilege.getGrantor())) {
      privilegeType.setGrantor(privilege.getGrantor());
    } else {
      throw new ModuleException("Error while exporting users structure: privilege grantor cannot be blank");
    }

    if (StringUtils.isNotBlank(privilege.getGrantee())) {
      privilegeType.setGrantee(privilege.getGrantee());
    } else {
      throw new ModuleException("Error while exporting users structure: privilege grantee cannot be blank");
    }

    if (StringUtils.isNotBlank(privilege.getOption()) && SIARDHelper.isValidOption(privilege.getOption())) {
      privilegeType.setOption(PrivOptionType.fromValue(privilege.getOption()));
    }

    if (StringUtils.isNotBlank(privilege.getDescription())) {
      privilegeType.setDescription(privilege.getDescription());
    }

    return privilegeType;
  }

  private RolesType jaxbRolesType(List<RoleStructure> roles) throws ModuleException {
    if (roles != null && !roles.isEmpty()) {
      RolesType rolesType = new RolesType();
      for (RoleStructure role : roles) {
        rolesType.getRole().add(jaxbRoleType(role));
      }
      return rolesType;
    } else {
      return null;
    }
  }

  private RoleType jaxbRoleType(RoleStructure role) throws ModuleException {
    RoleType roleType = new RoleType();

    if (StringUtils.isNotBlank(role.getName())) {
      roleType.setName(role.getName());
    } else {
      throw new ModuleException("Error while exporting users structure: user name cannot be blank");
    }

    if (role.getAdmin() != null) {
      roleType.setAdmin(role.getAdmin());
    } else {
      // TODO: check in which circumstances this happens
      throw new ModuleException("Error while exporting users structure: role admin cannot be null");
    }

    if (StringUtils.isNotBlank(role.getDescription())) {
      roleType.setDescription(role.getDescription());
    }

    return roleType;
  }

  private UsersType jaxbUsersType(List<UserStructure> users) throws ModuleException {
    if (users != null && !users.isEmpty()) {
      UsersType usersType = new UsersType();
      for (UserStructure user : users) {
        usersType.getUser().add(jaxbUserType(user));
      }
      return usersType;
    } else {
      return null;
    }
  }

  private UserType jaxbUserType(UserStructure user) throws ModuleException {
    UserType userType = new UserType();

    if (StringUtils.isNotBlank(user.getName())) {
      userType.setName(user.getName());
    } else {
      throw new ModuleException("Error while exporting users structure: user name cannot be blank");
    }

    if (StringUtils.isNotBlank(user.getDescription())) {
      userType.setDescription(user.getDescription());
    }

    return userType;
  }

  private SchemasType jaxbSchemasType(List<SchemaStructure> schemas) throws ModuleException {
    if (schemas != null && !schemas.isEmpty()) {
      SchemasType schemasType = new SchemasType();
      for (SchemaStructure schema : schemas) {
        if (schema.getTables().isEmpty()) {
          LOGGER.warn("Schema " + schema.getName() + " was not exported because it does not contain tables.");
        } else {
          schemasType.getSchema().add(jaxbSchemaType(schema));
        }
      }
      return schemasType;
    } else {
      return null;
    }
  }

  private SchemaType jaxbSchemaType(SchemaStructure schema) throws ModuleException {
    SchemaType schemaType = new SchemaType();

    if (StringUtils.isNotBlank(schema.getName())) {
      schemaType.setName(schema.getName());
      schemaType.setFolder(contentPathStrategy.getSchemaFolderName(schema.getIndex()));
    } else {
      throw new ModuleException("Error while exporting schema structure: schema name cannot be blank");
    }

    if (StringUtils.isNotBlank(schema.getDescription())) {
      schemaType.setDescription(schema.getDescription());
    }

    schemaType.setTables(jaxbTablesType(schema, schema.getTables()));
    schemaType.setViews(jaxbViewsType(schema.getViews()));
    schemaType.setRoutines(jaxbRoutinesType(schema.getRoutines()));

    return schemaType;
  }

  private RoutinesType jaxbRoutinesType(List<RoutineStructure> routines) throws ModuleException {
    if (routines != null && !routines.isEmpty()) {
      RoutinesType routinesType = new RoutinesType();
      for (RoutineStructure routineStructure : routines) {
        routinesType.getRoutine().add(jaxbRoutineType(routineStructure));
      }
      return routinesType;
    } else {
      return null;
    }
  }

  private RoutineType jaxbRoutineType(RoutineStructure routine) throws ModuleException {
    RoutineType routineType = new RoutineType();

    if (StringUtils.isNotBlank(routine.getName())) {
      routineType.setName(routine.getName());
    } else {
      throw new ModuleException("Error while exporting routine: routine name cannot be blank");
    }

    if (StringUtils.isNotBlank(routine.getDescription())) {
      routineType.setDescription(routine.getDescription());
    }

    if (StringUtils.isNotBlank(routine.getSource())) {
      routineType.setSource(routine.getSource());
    }

    if (StringUtils.isNotBlank(routine.getBody())) {
      routineType.setBody(routine.getBody());
    }

    if (StringUtils.isNotBlank(routine.getCharacteristic())) {
      routineType.setCharacteristic(routine.getCharacteristic());
    }

    if (StringUtils.isNotBlank(routine.getReturnType())) {
      routineType.setReturnType(routine.getReturnType());
    }

    routineType.setParameters(jaxbParametersType(routine.getParameters()));

    return routineType;
  }

  private ParametersType jaxbParametersType(List<Parameter> parameters) throws ModuleException {
    if (parameters != null && !parameters.isEmpty()) {
      ParametersType parametersType = new ParametersType();
      for (Parameter parameter : parameters) {
        parametersType.getParameter().add(jaxbParameterType(parameter));
      }
      return parametersType;
    } else {
      return null;
    }
  }

  private ParameterType jaxbParameterType(Parameter parameter) throws ModuleException {
    ParameterType parameterType = new ParameterType();

    if (StringUtils.isNotBlank(parameter.getName())) {
      parameterType.setName(parameter.getName());
    } else {
      throw new ModuleException("Error while exporting routine parameters: parameter name cannot be blank");
    }

    if (StringUtils.isNotBlank(parameter.getMode())) {
      parameterType.setMode(parameter.getMode());
    } else {
      throw new ModuleException("Error while exporting routine parameters: parameter mode cannot be blank");
    }

    if (parameter.getType() != null) {
      parameterType.setType(parameter.getType().getSql99TypeName());
      parameterType.setTypeOriginal(parameter.getType().getOriginalTypeName());
    } else {
      throw new ModuleException("Error while exporting routine parameters: parameter type cannot be null");
    }

    if (StringUtils.isNotBlank(parameter.getDescription())) {
      parameterType.setDescription(parameter.getDescription());
    }

    return parameterType;
  }

  private ViewsType jaxbViewsType(List<ViewStructure> views) throws ModuleException {
    if (views != null && !views.isEmpty()) {
      ViewsType viewsType = new ViewsType();
      for (ViewStructure viewStructure : views) {
        viewsType.getView().add(jaxbViewType(viewStructure));
      }
      return viewsType;
    } else {
      return null;
    }
  }

  private ViewType jaxbViewType(ViewStructure view) throws ModuleException {
    ViewType viewType = new ViewType();

    if (StringUtils.isNotBlank(view.getName())) {
      viewType.setName(view.getName());
    } else {
      throw new ModuleException("Error while exporting view: view name cannot be null");
    }

    if (StringUtils.isNotBlank(view.getQuery())) {
      viewType.setQuery(view.getQuery());
    }

    if (StringUtils.isNotBlank(view.getQueryOriginal())) {
      viewType.setQueryOriginal(view.getQueryOriginal());
    }

    if (StringUtils.isNotBlank(view.getDescription())) {
      viewType.setDescription(view.getDescription());
    }

    viewType.setColumns(jaxbColumnsType(view.getColumns()));

    return viewType;
  }

  private ColumnsType jaxbColumnsType(List<ColumnStructure> columns) throws ModuleException {
    if (columns != null && !columns.isEmpty()) {
      ColumnsType columnsType = new ColumnsType();
      for (int index = 0; index < columns.size(); index++) {
        ColumnStructure columnStructure = columns.get(index); // 0-based index
        columnsType.getColumn().add(jaxbColumnType(columnStructure, index + 1)); // 1-based
        // index
      }
      return columnsType;
    } else {
      return null;
    }
  }

  private ColumnType jaxbColumnType(ColumnStructure column, int columnIndex) throws ModuleException {
    ColumnType columnType = new ColumnType();

    if (StringUtils.isNotBlank(column.getName())) {
      columnType.setName(column.getName());
    } else {
      throw new ModuleException("Error while exporting table structure: column name cannot be null");
    }

    if (column.getType() != null) {
      if (column.getType() instanceof ComposedTypeStructure) {
        LOGGER.debug("ignoring composed type '" + column.getType().getOriginalTypeName() + "'");
        columnType.setType("SMALLINT");
        columnType.setNullable(true);

        columnType.setTypeOriginal(column.getType().getOriginalTypeName());
      } else {
        LOGGER.debug("Saving type '" + column.getType().getOriginalTypeName() + "'(internal_id:"
          + column.getType().hashCode() + ") as " + column.getType().getSql99TypeName());
        columnType.setType(column.getType().getSql99TypeName());
        columnType.setTypeOriginal(column.getType().getOriginalTypeName());

        if (column.isNillable() != null) {
          columnType.setNullable(column.getNillable());
        } else {
          LOGGER.debug("column nullable property was null. changed it to false");
        }
      }
    } else {
      throw new ModuleException("Error while exporting table structure: column type cannot be null");
    }

    if (StringUtils.isNotBlank(column.getDefaultValue())) {
      columnType.setDefaultValue(column.getDefaultValue());
    }

    if (StringUtils.isNotBlank(column.getDescription())) {
      columnType.setDescription(column.getDescription());
    }

    // specific fields for lobs
    String xsdTypeFromColumnSql99Type = null;
    try {
      xsdTypeFromColumnSql99Type = Sql99toXSDType.convert(column.getType());
    } catch (UnknownTypeException e) {
      throw new ModuleException("Could not get SQL2008 type", e);
    }

    if (xsdTypeFromColumnSql99Type != null
      && ("clobType".equals(xsdTypeFromColumnSql99Type) || "blobType".equals(xsdTypeFromColumnSql99Type))) {
      columnType.setFolder(contentPathStrategy.getColumnFolderName(columnIndex));
    }

    return columnType;
  }

  private TablesType jaxbTablesType(SchemaStructure schema, List<TableStructure> tables) throws ModuleException {
    TablesType tablesType = new TablesType();
    if (tables != null && !tables.isEmpty()) {
      for (TableStructure tableStructure : tables) {
        tablesType.getTable().add(jaxbTableType(schema, tableStructure));
      }
    } else {
      LOGGER.info(String.format("Schema %s does not have any tables.", schema.getName()));
    }
    return tablesType;
  }

  private TableType jaxbTableType(SchemaStructure schema, TableStructure table) throws ModuleException {
    TableType tableType = new TableType();

    if (StringUtils.isNotBlank(table.getName())) {
      tableType.setName(table.getName());
      tableType.setFolder(contentPathStrategy.getTableFolderName(table.getIndex()));
    } else {
      throw new ModuleException("Error while exporting table structure: table name cannot be blank");
    }

    if (StringUtils.isNotBlank(table.getDescription())) {
      tableType.setDescription(table.getDescription());
    }

    tableType.setColumns(jaxbColumnsType(table.getColumns()));

    tableType.setPrimaryKey(jaxbPrimaryKeyType(table.getPrimaryKey()));

    tableType.setForeignKeys(jaxbForeignKeysType(table.getForeignKeys()));

    tableType.setCandidateKeys(jaxbCandidateKeysType(table.getCandidateKeys()));

    tableType.setCheckConstraints(jaxbCheckConstraintsType(table.getCheckConstraints()));

    tableType.setTriggers(jaxbTriggersType(table.getTriggers()));

    if (table.getRows() >= 0) {
      tableType.setRows(BigInteger.valueOf(table.getRows()));
    } else {
      throw new ModuleException(
        "Error while exporting table structure: number of table rows was not set (or was set to negative value)");
    }

    return tableType;
  }

  private PrimaryKeyType jaxbPrimaryKeyType(PrimaryKey primaryKey) throws ModuleException {
    if (primaryKey != null) {
      PrimaryKeyType primaryKeyType = new PrimaryKeyType();
      if (StringUtils.isNotBlank(primaryKey.getName())) {
        primaryKeyType.setName(primaryKey.getName());
      } else {
        throw new ModuleException("Error while exporting primary key: name cannot be blank");
      }

      if (StringUtils.isNotBlank(primaryKey.getDescription())) {
        primaryKeyType.setDescription(primaryKey.getDescription());
      }

      if (primaryKey.getColumnNames() != null && primaryKey.getColumnNames().size() > 0) {
        primaryKeyType.getColumn().addAll(primaryKey.getColumnNames());
      } else {
        // throw new
        // ModuleException("Error while exporting primary key: column list cannot be empty");
        LOGGER.warn("Error while exporting primary key: column list cannot be empty");
      }
      return primaryKeyType;
    } else {
      return null;
    }
  }

  private TriggersType jaxbTriggersType(List<Trigger> triggers) throws ModuleException {
    if (triggers != null && !triggers.isEmpty()) {
      TriggersType triggersType = new TriggersType();
      for (Trigger trigger : triggers) {
        triggersType.getTrigger().add(jaxbTriggerType(trigger));
      }
      return triggersType;
    } else {
      return null;
    }
  }

  private TriggerType jaxbTriggerType(Trigger trigger) throws ModuleException {
    TriggerType triggerType = new TriggerType();

    if (StringUtils.isNotBlank(trigger.getName())) {
      triggerType.setName(XMLUtils.encode(trigger.getName()));
    } else {
      throw new ModuleException("Error while exporting trigger: trigger name key name cannot be blank");
    }

    try {
      triggerType.setActionTime(ActionTimeType.fromValue(trigger.getActionTime()));
    } catch (IllegalArgumentException e) {
      throw new ModuleException("Error while exporting trigger: trigger actionTime is invalid", e);
    } catch (NullPointerException e) {
      throw new ModuleException("Error while exporting trigger: trigger actionTime cannot be null", e);
    }

    if (StringUtils.isNotBlank(trigger.getTriggerEvent())) {
      triggerType.setTriggerEvent(XMLUtils.encode(trigger.getTriggerEvent()));
    } else {
      throw new ModuleException("Error while exporting trigger: trigger triggerEvent cannot be blank");
    }

    if (StringUtils.isNotBlank(trigger.getAliasList())) {
      triggerType.setAliasList(trigger.getAliasList());
    }

    if (StringUtils.isNotBlank(trigger.getTriggeredAction())) {
      triggerType.setTriggeredAction(XMLUtils.encode(trigger.getTriggeredAction()));
    } else {
      throw new ModuleException("Error while exporting trigger: trigger triggeredAction cannot be blank");
    }

    if (StringUtils.isNotBlank(trigger.getDescription())) {
      triggerType.setDescription(trigger.getDescription());
    }

    return triggerType;
  }

  private CheckConstraintsType jaxbCheckConstraintsType(List<CheckConstraint> checkConstraints) throws ModuleException {
    if (checkConstraints != null && !checkConstraints.isEmpty()) {
      CheckConstraintsType checkConstraintsType = new CheckConstraintsType();
      for (CheckConstraint checkConstraint : checkConstraints) {
        checkConstraintsType.getCheckConstraint().add(jaxbCheckConstraintType(checkConstraint));
      }
      return checkConstraintsType;
    } else {
      return null;
    }
  }

  private CheckConstraintType jaxbCheckConstraintType(CheckConstraint checkConstraint) throws ModuleException {
    CheckConstraintType checkConstraintType = new CheckConstraintType();

    if (StringUtils.isNotBlank(checkConstraint.getName())) {
      checkConstraintType.setName(checkConstraint.getName());
    } else {
      throw new ModuleException("Error while exporting check constraint: check constraint key name cannot be null");
    }

    if (StringUtils.isNotBlank(checkConstraint.getCondition())) {
      checkConstraintType.setCondition(checkConstraint.getCondition());
    } else {
      throw new ModuleException("Error while exporting candidate key: check constraint condition cannot be null");
    }

    if (StringUtils.isNotBlank(checkConstraint.getDescription())) {
      checkConstraintType.setDescription(checkConstraint.getDescription());
    }

    return checkConstraintType;
  }

  private CandidateKeysType jaxbCandidateKeysType(List<CandidateKey> candidateKeys) throws ModuleException {
    if (candidateKeys != null && !candidateKeys.isEmpty()) {
      CandidateKeysType candidateKeysType = new CandidateKeysType();
      for (CandidateKey candidateKey : candidateKeys) {
        candidateKeysType.getCandidateKey().add(jaxbCandidateKeyType(candidateKey));
      }
      return candidateKeysType;
    } else {
      return null;
    }
  }

  private CandidateKeyType jaxbCandidateKeyType(CandidateKey candidateKey) throws ModuleException {
    CandidateKeyType candidateKeyType = new CandidateKeyType();

    if (StringUtils.isNotBlank(candidateKey.getName())) {
      candidateKeyType.setName(candidateKey.getName());
    } else {
      throw new ModuleException("Error while exporting candidate key: candidate key name cannot be null");
    }

    if (StringUtils.isNotBlank(candidateKey.getDescription())) {
      candidateKeyType.setDescription(candidateKey.getDescription());
    }

    if (candidateKey.getColumns() != null && candidateKey.getColumns().size() > 0) {
      candidateKeyType.getColumn().addAll(candidateKey.getColumns());
    } else {
      throw new ModuleException("Error while exporting candidate key: columns cannot be be null or empty");
    }

    return candidateKeyType;
  }

  private ForeignKeysType jaxbForeignKeysType(List<ForeignKey> foreignKeys) throws ModuleException {
    if (foreignKeys != null && !foreignKeys.isEmpty()) {
      ForeignKeysType foreignKeysType = new ForeignKeysType();
      for (ForeignKey foreignKey : foreignKeys) {
        foreignKeysType.getForeignKey().add(jaxbForeignKeyType(foreignKey));
      }
      return foreignKeysType;
    } else {
      return null;
    }
  }

  private ForeignKeyType jaxbForeignKeyType(ForeignKey foreignKey) throws ModuleException {
    ForeignKeyType foreignKeyType = new ForeignKeyType();

    if (StringUtils.isNotBlank(foreignKey.getName())) {
      foreignKeyType.setName(foreignKey.getName());
    } else {
      throw new ModuleException("Error while exporting foreign key: name cannot be blank");
    }

    if (StringUtils.isNotBlank(foreignKey.getReferencedSchema())) {
      foreignKeyType.setReferencedSchema(foreignKey.getReferencedSchema());
    } else {
      throw new ModuleException("Error while exporting foreign key: referencedSchema cannot be blank");
    }

    if (StringUtils.isNotBlank(foreignKey.getReferencedTable())) {
      foreignKeyType.setReferencedTable(foreignKey.getReferencedTable());
    } else {
      throw new ModuleException("Error while exporting foreign key: referencedTable cannot be blank");
    }

    if (foreignKey.getReferences() != null && foreignKey.getReferences().size() > 0) {
      for (Reference reference : foreignKey.getReferences()) {
        foreignKeyType.getReference().add(jaxbReferenceType(reference));
      }
    } else {
      throw new ModuleException("Error while exporting foreign key: reference cannot be null or empty");
    }

    if (StringUtils.isNotBlank(foreignKey.getMatchType())) {
      foreignKeyType.setMatchType(MatchTypeType.fromValue(foreignKey.getMatchType()));
    }

    if (StringUtils.isNotBlank(foreignKey.getDeleteAction())) {
      foreignKeyType.setDeleteAction(foreignKey.getDeleteAction());
    }

    if (StringUtils.isNotBlank(foreignKey.getUpdateAction())) {
      foreignKeyType.setUpdateAction(foreignKey.getUpdateAction());
    }

    if (StringUtils.isNotBlank(foreignKey.getDescription())) {
      foreignKeyType.setDescription(foreignKey.getDescription());
    }

    return foreignKeyType;
  }

  private ReferenceType jaxbReferenceType(Reference reference) {
    ReferenceType referenceType = new ReferenceType();

    if (StringUtils.isNotBlank(reference.getColumn())) {
      referenceType.setColumn(reference.getColumn());
    }

    if (StringUtils.isNotBlank(reference.getReferenced())) {
      referenceType.setReferenced(reference.getReferenced());
    }

    return referenceType;
  }
}
