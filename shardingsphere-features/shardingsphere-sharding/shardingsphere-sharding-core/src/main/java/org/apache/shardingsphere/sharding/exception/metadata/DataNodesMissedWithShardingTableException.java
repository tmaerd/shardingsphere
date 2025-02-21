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

package org.apache.shardingsphere.sharding.exception.metadata;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.exception.ShardingSQLException;

/**
 * Data nodes missed with sharding table exception.
 */
public final class DataNodesMissedWithShardingTableException extends ShardingSQLException {
    
    private static final long serialVersionUID = 1691113459377955727L;
    
    public DataNodesMissedWithShardingTableException(final String tableName) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 3, "Data nodes must be configured for sharding table `%s`", tableName);
    }
}
