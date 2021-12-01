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

package org.apache.shardingsphere.sql.parser.core.extractor.impl.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.core.constant.Bracket;
import org.apache.shardingsphere.sql.parser.core.constant.LogicalOperator;
import org.apache.shardingsphere.sql.parser.core.constant.Paren;
import org.apache.shardingsphere.sql.parser.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.common.expression.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.sql.parser.core.extractor.util.RuleName;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateLeftBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightBracketValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Predicate extractor.
 */
public final class PredicateExtractor implements OptionalSQLSegmentExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    
    @Override
    public Optional<OrPredicateSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) whereNode.get().getChild(1), RuleName.EXPR);
        Preconditions.checkState(exprNode.isPresent());
        return extractRecursiveWithLogicalOperation(exprNode.get(), parameterMarkerIndexes);
    }
    
    private Optional<OrPredicateSegment> extractRecursiveWithLogicalOperation(final ParserRuleContext exprNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> logicalOperatorNode = ExtractorUtils.findFirstChildNodeNoneRecursive(exprNode, RuleName.LOGICAL_OPERATOR);
        if (!logicalOperatorNode.isPresent()) {
            return extractRecursiveWithParen(exprNode, parameterMarkerIndexes);
        }
        Optional<OrPredicateSegment> leftPredicate = extractPredicateSegment(exprNode, extractOperatorNode(exprNode, 0), 0, parameterMarkerIndexes);
        Optional<OrPredicateSegment> rightPredicate = extractPredicateSegment(exprNode, extractOperatorNode(exprNode, 2), 2, parameterMarkerIndexes);
        if (leftPredicate.isPresent() && rightPredicate.isPresent()) {
            return Optional.of(mergePredicate(leftPredicate.get(), rightPredicate.get(), logicalOperatorNode.get().getText()));
        }
        return leftPredicate.isPresent() ? leftPredicate : rightPredicate;
    }
    
    private ParserRuleContext extractOperatorNode(final ParserRuleContext exprNode, final int childIndex) {
        ParserRuleContext result = (ParserRuleContext) exprNode.getChild(childIndex);
        while (ExtractorUtils.findFirstChildNodeNoneRecursive(result, RuleName.LOGICAL_OPERATOR).isPresent()) {
            result = (ParserRuleContext) result.getChild(childIndex);
        }
        return result;
    }
    
    private Optional<OrPredicateSegment> extractPredicateSegment(final ParserRuleContext exprNode, final ParserRuleContext childNode,
                                                                 final int childIndex, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        ParserRuleContext operatorNode = childNode;
        Optional<OrPredicateSegment> result = extractRecursiveWithParen(childNode, parameterMarkerIndexes);
        Optional<OrPredicateSegment> predicateSegment;
        while (!operatorNode.getParent().equals(exprNode)) {
            Optional<ParserRuleContext> childLogicalNode = ExtractorUtils.findFirstChildNodeNoneRecursive(operatorNode.getParent(), RuleName.LOGICAL_OPERATOR);
            if (childLogicalNode.isPresent() && childIndex == 0) {
                predicateSegment = extractRecursiveWithParen((ParserRuleContext) operatorNode.getParent().getChild(2), parameterMarkerIndexes);
                if (result.isPresent() && predicateSegment.isPresent()) {
                    result = Optional.of(mergePredicate(result.get(), predicateSegment.get(), childLogicalNode.get().getText()));
                }
                result = result.isPresent() ? result : predicateSegment;
            } else if (childLogicalNode.isPresent() && childIndex == 2) {
                predicateSegment = extractRecursiveWithParen((ParserRuleContext) operatorNode.getParent().getChild(0), parameterMarkerIndexes);
                if (result.isPresent() && predicateSegment.isPresent()) {
                    result = Optional.of(mergePredicate(predicateSegment.get(), result.get(), childLogicalNode.get().getText()));
                }
                result = predicateSegment.isPresent() ? predicateSegment : result;
            }
            operatorNode = operatorNode.getParent();
        }
        return result;
    }
    
    private Optional<OrPredicateSegment> extractRecursiveWithParen(final ParserRuleContext exprNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        if (1 == exprNode.getChild(0).getText().length() && Paren.isLeftParen(exprNode.getChild(0).getText().charAt(0))) {
            return extractRecursiveWithLogicalOperation((ParserRuleContext) exprNode.getChild(1), parameterMarkerIndexes);
        }
        Optional<PredicateSegment> predicate = extractPredicate(exprNode, parameterMarkerIndexes);
        return predicate.isPresent() ? Optional.of(getOrPredicateSegment(predicate.get())) : Optional.<OrPredicateSegment>absent();
    }
    
    private Optional<PredicateSegment> extractPredicate(final ParserRuleContext exprNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        if (ExtractorUtils.findFirstChildNode(exprNode, RuleName.SUBQUERY).isPresent()) {
            return Optional.absent();
        }
        Optional<PredicateSegment> result = extractComparisonPredicate(exprNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        Optional<ParserRuleContext> predicateNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.PREDICATE);
        if (!predicateNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnSegment> column = columnExtractor.extract((ParserRuleContext) predicateNode.get().getChild(0), parameterMarkerIndexes);
        if (!column.isPresent()) {
            return Optional.absent();
        }
        if (5 == predicateNode.get().getChildCount() && "BETWEEN".equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            result = extractBetweenPredicate(predicateNode.get(), parameterMarkerIndexes, column.get());
            if (result.isPresent()) {
                return result;
            }
        }
        if (predicateNode.get().getChildCount() >= 5 && "IN".equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            result = extractInPredicate(predicateNode.get(), parameterMarkerIndexes, column.get());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private Optional<PredicateSegment> extractComparisonPredicate(final ParserRuleContext exprNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> comparisonOperatorNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.COMPARISON_OPERATOR);
        if (!comparisonOperatorNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext booleanPrimaryNode = comparisonOperatorNode.get().getParent();
        Optional<ParserRuleContext> leftColumnNode = ExtractorUtils.findSingleNodeFromFirstDescendant((ParserRuleContext) booleanPrimaryNode.getChild(0), RuleName.COLUMN_NAME);
        Optional<ParserRuleContext> rightColumnNode = ExtractorUtils.findSingleNodeFromFirstDescendant((ParserRuleContext) booleanPrimaryNode.getChild(2), RuleName.COLUMN_NAME);
        if (!leftColumnNode.isPresent() && !rightColumnNode.isPresent()) {
            return Optional.absent();
        }
        if (leftColumnNode.isPresent() && rightColumnNode.isPresent()) {
            Optional<ColumnSegment> leftColumn = columnExtractor.extract(leftColumnNode.get(), parameterMarkerIndexes);
            Optional<ColumnSegment> rightColumn = columnExtractor.extract(rightColumnNode.get(), parameterMarkerIndexes);
            Preconditions.checkState(leftColumn.isPresent() && rightColumn.isPresent());
            return Optional.of(new PredicateSegment(booleanPrimaryNode.getStart().getStartIndex(), booleanPrimaryNode.getStop().getStopIndex(), leftColumn.get(), rightColumn.get()));
        }
        Optional<ColumnSegment> column = columnExtractor.extract(exprNode, parameterMarkerIndexes);
        Preconditions.checkState(column.isPresent());
        ParserRuleContext valueNode = leftColumnNode.isPresent()
                ? (ParserRuleContext) comparisonOperatorNode.get().getParent().getChild(2) : (ParserRuleContext) comparisonOperatorNode.get().getParent().getChild(0);
        Optional<? extends ExpressionSegment> sqlExpression = expressionExtractor.extract(valueNode, parameterMarkerIndexes);
        return sqlExpression.isPresent() ? Optional.of(new PredicateSegment(booleanPrimaryNode.getStart().getStartIndex(), booleanPrimaryNode.getStop().getStopIndex(), column.get(), 
                new PredicateCompareRightValue(comparisonOperatorNode.get().getText(), sqlExpression.get()))) : Optional.<PredicateSegment>absent();
    }
    
    private Optional<PredicateSegment> extractBetweenPredicate(final ParserRuleContext predicateNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ColumnSegment column) {
        Optional<? extends ExpressionSegment> betweenSQLExpression = expressionExtractor.extract((ParserRuleContext) predicateNode.getChild(2), parameterMarkerIndexes);
        Optional<? extends ExpressionSegment> andSQLExpression = expressionExtractor.extract((ParserRuleContext) predicateNode.getChild(4), parameterMarkerIndexes);
        return betweenSQLExpression.isPresent() && andSQLExpression.isPresent()
                ? Optional.of(new PredicateSegment(
                        predicateNode.getStart().getStartIndex(), predicateNode.getStop().getStopIndex(), column, new PredicateBetweenRightValue(betweenSQLExpression.get(), andSQLExpression.get())))
                : Optional.<PredicateSegment>absent();
    }
    
    private Optional<PredicateSegment> extractInPredicate(final ParserRuleContext predicateNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ColumnSegment column) {
        Optional<PredicateBracketValue> predicateBracketValue = extractBracketValue(predicateNode);
        Collection<ExpressionSegment> sqlExpressions = extractInExpressionSegments(predicateNode, parameterMarkerIndexes);
        PredicateSegment predicateSegment = new PredicateSegment(predicateNode.getStart().getStartIndex(), predicateNode.getStop().getStopIndex(), column,
                new PredicateInRightValue(predicateBracketValue.get(), sqlExpressions));
        return sqlExpressions.isEmpty() ? Optional.<PredicateSegment>absent() : Optional.of(predicateSegment);
    }
    
    private Optional<PredicateBracketValue> extractBracketValue(final ParserRuleContext predicateNode) {
        PredicateLeftBracketValue predicateLeftBracketValue = null;
        PredicateRightBracketValue predicateRightBracketValue = null;
        for (ParseTree each : predicateNode.children) {
            if (each instanceof TerminalNode && Bracket.LEFT.getValue().equals(each.getText())) {
                predicateLeftBracketValue = new PredicateLeftBracketValue(((TerminalNode) each).getSymbol().getStartIndex(), ((TerminalNode) each).getSymbol().getStopIndex());
            }
            if (each instanceof TerminalNode && Bracket.RIGHT.getValue().equals(each.getText())) {
                predicateRightBracketValue = new PredicateRightBracketValue(((TerminalNode) each).getSymbol().getStartIndex(), ((TerminalNode) each).getSymbol().getStopIndex());
            }
        }
        if (null != predicateLeftBracketValue && null != predicateRightBracketValue) {
            return Optional.of(new PredicateBracketValue(predicateLeftBracketValue, predicateRightBracketValue));
        }
        return Optional.absent();
    }
    
    private Collection<ExpressionSegment> extractInExpressionSegments(final ParserRuleContext predicateNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (int i = 3; i < predicateNode.getChildCount(); i++) {
            if (RuleName.EXPR.getName().equals(predicateNode.getChild(i).getClass().getSimpleName())) {
                Optional<? extends ExpressionSegment> expression = expressionExtractor.extract((ParserRuleContext) predicateNode.getChild(i), parameterMarkerIndexes);
                // FIXME if some part of expr is not supported, clear all expr for IN clause
                if (!expression.isPresent()) {
                    return Collections.emptyList();
                }
                result.add(expression.get());
            }
        }
        return result;
    }
    
    private OrPredicateSegment getOrPredicateSegment(final PredicateSegment predicate) {
        OrPredicateSegment result = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(predicate);
        result.getAndPredicates().add(andPredicate);
        return result;
    }
    
    private OrPredicateSegment mergePredicate(final OrPredicateSegment leftPredicate, final OrPredicateSegment rightPredicate, final String operator) {
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(operator);
        Preconditions.checkState(logicalOperator.isPresent());
        if (LogicalOperator.OR == logicalOperator.get()) {
            leftPredicate.getAndPredicates().addAll(rightPredicate.getAndPredicates());
            return leftPredicate;
        }
        OrPredicateSegment result = new OrPredicateSegment();
        for (AndPredicate eachLeftPredicate : leftPredicate.getAndPredicates()) {
            for (AndPredicate eachRightPredicate : rightPredicate.getAndPredicates()) {
                result.getAndPredicates().add(getAndPredicate(eachLeftPredicate, eachRightPredicate));
            }
        }
        return result;
    }
    
    private AndPredicate getAndPredicate(final AndPredicate leftPredicate, final AndPredicate rightPredicate) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().addAll(leftPredicate.getPredicates());
        result.getPredicates().addAll(rightPredicate.getPredicates());
        return result;
    }
}
