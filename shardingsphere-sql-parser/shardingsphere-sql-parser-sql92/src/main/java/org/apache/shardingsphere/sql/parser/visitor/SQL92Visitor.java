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

package org.apache.shardingsphere.sql.parser.visitor;

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.sql.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateBuilder;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateLeftBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * SQL92 visitor.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class SQL92Visitor extends SQL92StatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(currentParameterIndex++);
    }
    
    @Override
    public final ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.hexadecimalLiterals()) {
            return visit(ctx.hexadecimalLiterals());
        }
        if (null != ctx.bitValueLiterals()) {
            return visit(ctx.bitValueLiterals());
        }
        if (null != ctx.booleanLiterals()) {
            return visit(ctx.booleanLiterals());
        }
        if (null != ctx.nullValueLiterals()) {
            return visit(ctx.nullValueLiterals());
        }
        throw new IllegalStateException("Literals must have string, number, dateTime, hex, bit, boolean or null.");
    }
    
    @Override
    public final ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitHexadecimalLiterals(final HexadecimalLiteralsContext ctx) {
        // TODO deal with hexadecimalLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBitValueLiterals(final BitValueLiteralsContext ctx) {
        // TODO deal with bitValueLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBooleanLiterals(final BooleanLiteralsContext ctx) {
        return new BooleanLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNullValueLiterals(final NullValueLiteralsContext ctx) {
        // TODO deal with nullValueLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        UnreservedWordContext unreservedWord = ctx.unreservedWord();
        return null != unreservedWord ? visit(unreservedWord) : new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitUnreservedWord(final UnreservedWordContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        ColumnSegment result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitTableNames(final TableNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnNames(final ColumnNamesContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (ColumnNameContext each : ctx.columnName()) {
            result.getValue().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitExpr(final ExprContext ctx) {
        if (null != ctx.booleanPrimary()) {
            return visit(ctx.booleanPrimary());
        }
        if (null != ctx.logicalOperator()) {
            return new PredicateBuilder(visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.logicalOperator().getText()).mergePredicate();
        }
        // TODO deal with XOR
        return visit(ctx.expr().get(0));
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()));
        }
        if (null != ctx.comparisonOperator() || null != ctx.SAFE_EQ_()) {
            return createCompareSegment(ctx);
        }
        if (null != ctx.predicate()) {
            return visit(ctx.predicate());
        }
        //TODO deal with IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ASTNode leftValue = visit(ctx.booleanPrimary());
        if (!(leftValue instanceof ColumnSegment)) {
            return leftValue;
        }
        PredicateRightValue rightValue = (PredicateRightValue) createPredicateRightValue(ctx);
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) leftValue, rightValue);
    }
    
    private ASTNode createPredicateRightValue(final BooleanPrimaryContext ctx) {
        if (null != ctx.subquery()) {
            new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()));
        }
        ASTNode result = visit(ctx.predicate());
        return result instanceof ColumnSegment
                ? (ColumnSegment) result : new PredicateCompareRightValue(ctx.comparisonOperator().getText(), (ExpressionSegment) result);
    }
    
    @Override
    public final ASTNode visitPredicate(final PredicateContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()));
        }
        if (null != ctx.IN() && null == ctx.NOT()) {
            return createInSegment(ctx);
        }
        if (null != ctx.BETWEEN() && null == ctx.NOT()) {
            return createBetweenSegment(ctx);
        }
        if (1 == ctx.children.size()) {
            return visit(ctx.bitExpr(0));
        }
        return visitRemainPredicate(ctx);
    }
    
    private PredicateSegment createInSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        PredicateBracketValue predicateBracketValue = createBracketValue(ctx);
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateInRightValue(predicateBracketValue, getExpressionSegments(ctx)));
    }
    
    private Collection<ExpressionSegment> getExpressionSegments(final PredicateContext ctx) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        if (null != ctx.subquery()) {
            SubqueryContext subquery = ctx.subquery();
            result.add(new SubqueryExpressionSegment(new SubquerySegment(subquery.getStart().getStartIndex(), subquery.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()))));
            return result;
        }
        for (ExprContext each : ctx.expr()) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    private PredicateBracketValue createBracketValue(final PredicateContext ctx) {
        PredicateLeftBracketValue predicateLeftBracketValue = new PredicateLeftBracketValue(ctx.LP_().getSymbol().getStartIndex(), ctx.LP_().getSymbol().getStopIndex());
        PredicateRightBracketValue predicateRightBracketValue = new PredicateRightBracketValue(ctx.RP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
        return new PredicateBracketValue(predicateLeftBracketValue, predicateRightBracketValue);
    }
    
    private PredicateSegment createBetweenSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bitExpr(1));
        ExpressionSegment and = (ExpressionSegment) visit(ctx.predicate());
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateBetweenRightValue(between, and));
    }
    
    private ASTNode visitRemainPredicate(final PredicateContext ctx) {
        for (BitExprContext each : ctx.bitExpr()) {
            visit(each);
        }
        for (ExprContext each : ctx.expr()) {
            visit(each);
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        if (null != ctx.predicate()) {
            visit(ctx.predicate());
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitBitExpr(final BitExprContext ctx) {
        if (null != ctx.simpleExpr()) {
            return createExpressionSegment(visit(ctx.simpleExpr()), ctx);
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createExpressionSegment(final ASTNode astNode, final ParserRuleContext context) {
        if (astNode instanceof StringLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((StringLiteralValue) astNode).getValue());
        }
        if (astNode instanceof NumberLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((NumberLiteralValue) astNode).getValue());
        }
        if (astNode instanceof BooleanLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((BooleanLiteralValue) astNode).getValue());
        }
        if (astNode instanceof ParameterMarkerValue) {
            return new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((ParameterMarkerValue) astNode).getValue());
        }
        if (astNode instanceof OtherLiteralValue) {
            return new CommonExpressionSegment(context.getStart().getStartIndex(), context.getStop().getStopIndex(), context.getText());
        }
        return astNode;
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()));
        }
        if (null != ctx.parameterMarker()) {
            return visit(ctx.parameterMarker());
        }
        if (null != ctx.literals()) {
            return visit(ctx.literals());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction or specialFunction.");
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType) : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        int innerExpressionStartIndex = ((TerminalNode) ctx.getChild(1)).getSymbol().getStartIndex();
        if (null == ctx.distinct()) {
            return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpressionStartIndex);
        }
        return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpressionStartIndex, getDistinctExpression(ctx));
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        return new KeywordValue(ctx.getText());
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public final ASTNode visitOrderByClause(final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public final ASTNode visitOrderByItem(final OrderByItemContext ctx) {
        OrderDirection orderDirection = null != ctx.DESC() ? OrderDirection.DESC : OrderDirection.ASC;
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection);
        }
        return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                SQLUtil.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection);
    }
}
