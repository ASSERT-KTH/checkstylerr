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

package org.apache.shardingsphere.sql.parser.core.visitor.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.core.SQLParserConfigurationRegistry;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorRule;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatementType;

/**
 * Statement SQL visitor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatementSQLVisitorFactory {
    
    /** 
     * New instance of statement SQL visitor.
     * 
     * @param databaseTypeName name of database type
     * @param SQLVisitorRule visitor rule
     * @return parse tree visitor
     */
    public static ParseTreeVisitor newInstance(final String databaseTypeName, final SQLVisitorRule SQLVisitorRule) {
        return createParseTreeVisitor(SQLParserConfigurationRegistry.getInstance().getSQLParserConfiguration(databaseTypeName), SQLVisitorRule.getType());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static ParseTreeVisitor createParseTreeVisitor(final SQLParserConfiguration config, final SQLStatementType type) {
        StatementSQLVisitorFacade visitorFacade =
                config.getVisitorFacadeClass().getConstructor().newInstance().getStatementSQLVisitorFacadeClass().getConstructor().newInstance();
        switch (type) {
            case DML:
                return (ParseTreeVisitor) visitorFacade.getDMLVisitorClass().getConstructor().newInstance();
            case DDL:
                return (ParseTreeVisitor) visitorFacade.getDDLVisitorClass().getConstructor().newInstance();
            case TCL:
                return (ParseTreeVisitor) visitorFacade.getTCLVisitorClass().getConstructor().newInstance();
            case DCL:
                return (ParseTreeVisitor) visitorFacade.getDCLVisitorClass().getConstructor().newInstance();
            case DAL:
                return (ParseTreeVisitor) visitorFacade.getDALVisitorClass().getConstructor().newInstance();
            case RL:
                return (ParseTreeVisitor) visitorFacade.getRLVisitorClass().getConstructor().newInstance();
            default:
                throw new SQLParsingException("Can not support SQL statement type: `%s`", type);
        }
    }
}
