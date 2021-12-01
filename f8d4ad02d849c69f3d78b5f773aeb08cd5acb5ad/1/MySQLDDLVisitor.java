/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.mysql.visitor.impl;

import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DDLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CheckConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLikeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropPrimaryKeySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FirstOrAfterColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ForeignKeyOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GeneratedOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReferenceDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameTableSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StorageOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.DropPrimaryKeySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * DDL visitor for MySQL.
 */
public final class MySQLDDLVisitor extends MySQLVisitor implements DDLVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        return new CreateViewStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        return new DropViewStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement();
    }
    
    @Override
    public ASTNode visitRenameTableSpecification(final RenameTableSpecificationContext ctx) {
        return new RenameTableStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.createDefinitionClause()) {
            CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) visit(ctx.createDefinitionClause());
            for (CreateDefinitionSegment each : createDefinitions.getValue()) {
                if (each instanceof ColumnDefinitionSegment) {
                    result.getColumnDefinitions().add((ColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (CreateDefinitionContext each : ctx.createDefinition()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.constraintDefinition()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.constraintDefinition()));
            }
            if (null != each.checkConstraintDefinition()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.checkConstraintDefinition()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateLikeClause(final CreateLikeClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDefinitionClause())).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.getAddColumnDefinitions().add((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.getDropColumnDefinitions().add((DropColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getAddConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        for (AlterSpecificationContext each : ctx.alterSpecification()) {
            if (null != each.addColumnSpecification()) {
                result.getValue().add((AddColumnDefinitionSegment) visit(each.addColumnSpecification()));
            }
            if (null != each.addConstraintSpecification()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.addConstraintSpecification().constraintDefinition()));
            }
            if (null != each.changeColumnSpecification()) {
                ModifyColumnDefinitionSegment modifyColumnDefinition = new ModifyColumnDefinitionSegment(
                        each.changeColumnSpecification().getStart().getStartIndex(), each.changeColumnSpecification().getStop().getStopIndex(), 
                        (ColumnDefinitionSegment) visit(each.changeColumnSpecification().columnDefinition()));
                if (null != each.changeColumnSpecification().firstOrAfterColumn()) {
                    modifyColumnDefinition.setColumnPosition((ColumnPositionSegment) visit(each.changeColumnSpecification().firstOrAfterColumn()));
                }
                result.getValue().add(modifyColumnDefinition);
            }
            if (null != each.modifyColumnSpecification()) {
                ModifyColumnDefinitionSegment modifyColumnDefinition = new ModifyColumnDefinitionSegment(
                        each.modifyColumnSpecification().getStart().getStartIndex(), each.modifyColumnSpecification().getStop().getStopIndex(),
                        (ColumnDefinitionSegment) visit(each.modifyColumnSpecification().columnDefinition()));
                if (null != each.modifyColumnSpecification().firstOrAfterColumn()) {
                    modifyColumnDefinition.setColumnPosition((ColumnPositionSegment) visit(each.modifyColumnSpecification().firstOrAfterColumn()));
                }
                result.getValue().add(modifyColumnDefinition);
            }
            if (null != each.dropColumnSpecification()) {
                result.getValue().add((DropColumnDefinitionSegment) visit(each.dropColumnSpecification()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
        for (ColumnDefinitionContext each : ctx.columnDefinition()) {
            columnDefinitions.add((ColumnDefinitionSegment) visit(each));
        }
        AddColumnDefinitionSegment result = new AddColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinitions);
        if (null != ctx.firstOrAfterColumn()) {
            Preconditions.checkState(1 == columnDefinitions.size());
            result.setColumnPosition(getColumnPositionSegment(columnDefinitions.iterator().next(), (ColumnPositionSegment) visit(ctx.firstOrAfterColumn())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey);
        result.getReferencedTables().addAll(getReferencedTables(ctx));
        return result;
    }
    
    private Collection<SimpleTableSegment> getReferencedTables(final ColumnDefinitionContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (StorageOptionContext each : ctx.storageOption()) {
            if (null != each.dataTypeGenericOption() && null != each.dataTypeGenericOption().referenceDefinition()) {
                result.add((SimpleTableSegment) visit(each.dataTypeGenericOption().referenceDefinition()));
            }
        }
        for (GeneratedOptionContext each : ctx.generatedOption()) {
            if (null != each.dataTypeGenericOption() && null != each.dataTypeGenericOption().referenceDefinition()) {
                result.add((SimpleTableSegment) visit(each.dataTypeGenericOption().referenceDefinition()));
            }
        }
        return result;
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (StorageOptionContext each : ctx.storageOption()) {
            if (null != each.dataTypeGenericOption() && null != each.dataTypeGenericOption().primaryKey()) {
                return true;
            }
        }
        for (GeneratedOptionContext each : ctx.generatedOption()) {
            if (null != each.dataTypeGenericOption() && null != each.dataTypeGenericOption().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitConstraintDefinition(final ConstraintDefinitionContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.primaryKeyOption()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.primaryKeyOption().columnNames())).getValue());
        }
        if (null != ctx.foreignKeyOption()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.foreignKeyOption().referenceDefinition()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCheckConstraintDefinition(final CheckConstraintDefinitionContext ctx) {
        return new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitChangeColumnSpecification(final ChangeColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList((ColumnSegment) visit(ctx.columnName())));
    }
    
    @Override
    public ASTNode visitDropPrimaryKeySpecification(final DropPrimaryKeySpecificationContext ctx) {
        return new DropPrimaryKeySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                (ColumnSegment) visit(ctx.columnName(0)), (ColumnSegment) visit(ctx.columnName(1)));
    }
    
    @Override
    public ASTNode visitReferenceDefinition(final ReferenceDefinitionContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitForeignKeyOption(final ForeignKeyOptionContext ctx) {
        return visit(ctx.referenceDefinition());
    }
    
    private ModifyColumnDefinitionSegment extractModifyColumnDefinition(final Token start, final Token stop, 
                                                                        final ColumnDefinitionContext columnDefinition, final FirstOrAfterColumnContext firstOrAfterColumn) {
        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(start.getStartIndex(), stop.getStopIndex(),
                (ColumnDefinitionSegment) visit(columnDefinition));
        if (null != firstOrAfterColumn) {
            result.setColumnPosition(getColumnPositionSegment(result.getColumnDefinition(), (ColumnPositionSegment) visit(firstOrAfterColumn)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitFirstOrAfterColumn(final FirstOrAfterColumnContext ctx) {
        ColumnSegment columnName = null;
        if (null != ctx.columnName()) {
            columnName = (ColumnSegment) visit(ctx.columnName());
        }
        return null == ctx.columnName() ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName)
                : new ColumnAfterPositionSegment(
                        ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName);
    }
    
    private ColumnPositionSegment getColumnPositionSegment(final ColumnDefinitionSegment columnDefinition, final ColumnPositionSegment columnPosition) {
        return columnPosition instanceof ColumnFirstPositionSegment
                ? new ColumnFirstPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnPosition.getColumnName())
                : new ColumnAfterPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnPosition.getColumnName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
}
