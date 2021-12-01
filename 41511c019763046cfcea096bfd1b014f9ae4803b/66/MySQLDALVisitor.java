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

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DALVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CacheIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChecksumTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DescContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FlushContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KillContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadIndexInfoContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptimizeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RepairTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ResetStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinaryLogsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinlogEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowErrorsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowOtherContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowWarningsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UninstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.CacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.InstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.KillStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.LoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.RepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ResetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.StringLiteralValue;

/**
 * DAL visitor for MySQL.
 */
public final class MySQLDALVisitor extends MySQLVisitor implements DALVisitor {
    
    @Override
    public ASTNode visitUninstallPlugin(final UninstallPluginContext ctx) {
        return new UninstallPluginStatement();
    }
    
    @Override
    public ASTNode visitShowBinaryLogs(final ShowBinaryLogsContext ctx) {
        return new ShowBinaryLogsStatement();
    }
    
    @Override
    public ASTNode visitShowStatus(final ShowStatusContext ctx) {
        return new ShowStatusStatement();
    }
    
    @Override
    public ASTNode visitShowCreateView(final ShowCreateViewContext ctx) {
        return new ShowCreateViewStatement();
    }
    
    @Override
    public ASTNode visitShowCreateEvent(final ShowCreateEventContext ctx) {
        return new ShowCreateEventStatement();
    }
    
    @Override
    public ASTNode visitShowCreateFunction(final ShowCreateFunctionContext ctx) {
        return new ShowCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitShowCreateProcedure(final ShowCreateProcedureContext ctx) {
        return new ShowCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitShowBinlogEvents(final ShowBinlogEventsContext ctx) {
        return new ShowBinlogStatement();
    }
    
    @Override
    public ASTNode visitShowErrors(final ShowErrorsContext ctx) {
        return new ShowErrorsStatement();
    }
    
    @Override
    public ASTNode visitShowWarnings(final ShowWarningsContext ctx) {
        return new ShowWarningsStatement();
    }
    
    @Override
    public ASTNode visitResetStatement(final ResetStatementContext ctx) {
        return new ResetStatement();
    }
    
    @Override
    public ASTNode visitRepairTable(final RepairTableContext ctx) {
        return new RepairTableStatement();
    }
    
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        return new AnalyzeTableStatement();
    }
    
    @Override
    public ASTNode visitCacheIndex(final CacheIndexContext ctx) {
        return new CacheIndexStatement();
    }
    
    @Override
    public ASTNode visitChecksumTable(final ChecksumTableContext ctx) {
        return new ChecksumTableStatement();
    }
    
    @Override
    public ASTNode visitFlush(final FlushContext ctx) {
        return new FlushStatement();
    }
    
    @Override
    public ASTNode visitKill(final KillContext ctx) {
        return new KillStatement();
    }
    
    @Override
    public ASTNode visitLoadIndexInfo(final LoadIndexInfoContext ctx) {
        return new LoadIndexInfoStatement();
    }
    
    @Override
    public ASTNode visitInstallPlugin(final InstallPluginContext ctx) {
        return new InstallPluginStatement();
    }
    
    @Override
    public ASTNode visitOptimizeTable(final OptimizeTableContext ctx) {
        return new OptimizeTableStatement();
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        UseStatement result = new UseStatement();
        result.setSchema(((IdentifierValue) visit(ctx.schemaName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDesc(final DescContext ctx) {
        DescribeStatement result = new DescribeStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        return new ShowDatabasesStatement();
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        ShowTablesStatement result = new ShowTablesStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        ShowTableStatusStatement result = new ShowTableStatusStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        ShowColumnsStatement result = new ShowColumnsStatement();
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visit(ctx.fromTable())).getTable());
        }
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        ShowIndexStatement result = new ShowIndexStatement();
        if (null != ctx.fromSchema()) {
            SchemaNameContext schemaNameContext = ctx.fromSchema().schemaName();
            // TODO visitSchemaName
            result.setSchema(new SchemaSegment(schemaNameContext.getStart().getStartIndex(), schemaNameContext.getStop().getStopIndex(), (IdentifierValue) visit(schemaNameContext)));
        }
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visitFromTable(ctx.fromTable())).getTable());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        ShowCreateTableStatement result = new ShowCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowOther(final ShowOtherContext ctx) {
        return new ShowOtherStatement();
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        SetStatement result = new SetStatement();
        if (null != ctx.variable()) {
            result.setVariable((VariableSegment) visit(ctx.variable()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitVariable(final VariableContext ctx) {
        return new VariableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitFromSchema(final FromSchemaContext ctx) {
        return new FromSchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
}
