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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.metadata.EngagedViewException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedCreateViewException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding create view statement validator.
 */
public final class ShardingCreateViewStatementValidator extends ShardingDDLStatementValidator<CreateViewStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<CreateViewStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereDatabase database) {
        Optional<SelectStatement> selectStatement = sqlStatementContext.getSqlStatement().getSelect();
        if (!selectStatement.isPresent()) {
            return;
        }
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(selectStatement.get());
        Collection<SimpleTableSegment> tableSegments = extractor.getRewriteTables();
        if (isShardingTablesWithoutBinding(shardingRule, sqlStatementContext, tableSegments)) {
            throw new EngagedViewException("sharding");
        }
        if (isAllBroadcastTablesWithoutConfigView(shardingRule, sqlStatementContext, tableSegments)) {
            throw new EngagedViewException("broadcast");
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<CreateViewStatement> sqlStatementContext, final List<Object> parameters,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        Optional<SelectStatement> selectStatement = sqlStatementContext.getSqlStatement().getSelect();
        if (!selectStatement.isPresent()) {
            return;
        }
        if (isContainsNotSupportedViewStatement(selectStatement.get(), routeContext)) {
            throw new UnsupportedCreateViewException();
        }
    }
    
    private boolean isShardingTablesWithoutBinding(final ShardingRule shardingRule, final SQLStatementContext<CreateViewStatement> sqlStatementContext,
                                                   final Collection<SimpleTableSegment> tableSegments) {
        for (SimpleTableSegment each : tableSegments) {
            String logicTable = each.getTableName().getIdentifier().getValue();
            if (shardingRule.isShardingTable(logicTable) && !isBindingTables(shardingRule, sqlStatementContext.getSqlStatement().getView().getTableName().getIdentifier().getValue(), logicTable)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isBindingTables(final ShardingRule shardingRule, final String logicViewName, final String logicTable) {
        Collection<String> bindTables = Arrays.asList(logicTable, logicViewName);
        return shardingRule.isAllBindingTables(bindTables);
    }
    
    private boolean isAllBroadcastTablesWithoutConfigView(final ShardingRule shardingRule, final SQLStatementContext<CreateViewStatement> sqlStatementContext,
                                                          final Collection<SimpleTableSegment> tableSegments) {
        Collection<String> tables = new LinkedList<>();
        for (SimpleTableSegment each : tableSegments) {
            tables.add(each.getTableName().getIdentifier().getValue());
        }
        return shardingRule.isAllBroadcastTables(tables) && !shardingRule.isBroadcastTable(sqlStatementContext.getSqlStatement().getView().getTableName().getIdentifier().getValue());
    }
    
    private boolean isContainsNotSupportedViewStatement(final SelectStatement selectStatement, final RouteContext routeContext) {
        if (routeContext.getRouteUnits().size() <= 1) {
            return false;
        }
        return hasGroupBy(selectStatement) || hasAggregation(selectStatement) || hasDistinct(selectStatement) || hasLimit(selectStatement);
    }
    
    private boolean hasGroupBy(final SelectStatement selectStatement) {
        return selectStatement.getGroupBy().map(groupBySegment -> !groupBySegment.getGroupByItems().isEmpty()).orElse(false);
    }
    
    private boolean hasAggregation(final SelectStatement selectStatement) {
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof AggregationProjectionSegment) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasDistinct(final SelectStatement selectStatement) {
        return selectStatement.getProjections().isDistinctRow();
    }
    
    private boolean hasLimit(final SelectStatement selectStatement) {
        return SelectStatementHandler.getLimitSegment(selectStatement).isPresent();
    }
}
