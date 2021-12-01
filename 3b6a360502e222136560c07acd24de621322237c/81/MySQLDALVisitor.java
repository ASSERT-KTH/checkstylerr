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

package org.apache.shardingsphere.sql.parser.visitor.impl;

import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DescContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowOtherContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.visitor.MySQLVisitor;

/**
 * MySQL DAL visitor.
 */
public final class MySQLDALVisitor extends MySQLVisitor {
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        UseStatement result = new UseStatement();
        result.setSchema(((IdentifierValue) visit(ctx.schemaName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDesc(final DescContext ctx) {
        DescribeStatement result = new DescribeStatement();
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        ShowDatabasesStatement result = new ShowDatabasesStatement();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        ShowTablesStatement result = new ShowTablesStatement();
        if (null != ctx.fromSchema()) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != ctx.showLike()) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        ShowTableStatusStatement result = new ShowTableStatusStatement();
        if (null != ctx.fromSchema()) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != ctx.showLike()) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        ShowColumnsStatement result = new ShowColumnsStatement();
        if (null != ctx.fromTable()) {
            FromTableSegment fromTableSegment = (FromTableSegment) visit(ctx.fromTable());
            result.setTable(fromTableSegment.getTable());
            result.getAllSQLSegments().add(fromTableSegment.getTable());
            result.getAllSQLSegments().add(fromTableSegment);
        }
        if (null != ctx.fromSchema()) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        ShowIndexStatement result = new ShowIndexStatement();
        if (null != ctx.fromSchema()) {
            SchemaNameContext schemaNameContext = ctx.fromSchema().schemaName();
            SchemaSegment schemaSegment = new SchemaSegment(schemaNameContext.getStart().getStartIndex(), schemaNameContext.getStop().getStopIndex(), (IdentifierValue) visit(schemaNameContext));
            result.getAllSQLSegments().add(schemaSegment);
        }
        if (null != ctx.fromTable()) {
            FromTableSegment fromTableSegment = (FromTableSegment) visitFromTable(ctx.fromTable());
            TableSegment tableSegment = fromTableSegment.getTable();
            result.setTable(tableSegment);
            result.getAllSQLSegments().add(tableSegment);
            result.getAllSQLSegments().add(fromTableSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        ShowCreateTableStatement result = new ShowCreateTableStatement();
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowOther(final ShowOtherContext ctx) {
        return new ShowOtherStatement();
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        SetStatement result = new SetStatement();
        VariableContext variableContext = ctx.variable();
        if (null != variableContext) {
            VariableExpressionSegment variable = new VariableExpressionSegment(variableContext.getStart().getStartIndex(), variableContext.getStop().getStopIndex(), variableContext.getText());
            result.getAllSQLSegments().add(variable);
        }
        return result;
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
