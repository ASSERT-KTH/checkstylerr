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

package org.apache.shardingsphere.sql.parser.sql.segment.dml.item;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;

import java.util.Optional;

/**
 * Aggregation projection segment.
 */
@Getter
public class AggregationProjectionSegment implements ProjectionSegment, AliasAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String text;
    
    private final AggregationType type;
    
    private final int innerExpressionStartIndex;
    
    @Setter
    private AliasSegment alias;
    
    public AggregationProjectionSegment(final int startIndex, final int stopIndex, final String text, final AggregationType type, final int innerExpressionStartIndex) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = SQLUtil.getExpressionWithoutOutsideParentheses(text);
        this.type = type;
        this.innerExpressionStartIndex = innerExpressionStartIndex;
    }
    
    @Override
    public Optional<String> getAlias() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
}
