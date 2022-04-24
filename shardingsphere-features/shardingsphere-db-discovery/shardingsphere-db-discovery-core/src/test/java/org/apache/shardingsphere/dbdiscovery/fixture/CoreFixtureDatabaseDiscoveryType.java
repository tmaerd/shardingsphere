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

package org.apache.shardingsphere.dbdiscovery.fixture;

import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public final class CoreFixtureDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    @Override
    public HighlyAvailableStatus loadHighlyAvailableStatus(final DataSource dataSource) {
        return mock(HighlyAvailableStatus.class);
    }
    
    @Override
    public Optional<String> findPrimaryDataSourceName(final Map<String, DataSource> dataSourceMap) {
        return Optional.of("primary");
    }
    
    @Override
    public StorageNodeDataSource getStorageNodeDataSource(final DataSource replicaDataSource) {
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.ENABLED);
    }
    
    @Override
    public String getPrimaryDataSource() {
        return "primary";
    }
    
    @Override
    public void setPrimaryDataSource(final String primaryDataSource) {
    }
    
    @Override
    public String getType() {
        return "CORE.FIXTURE";
    }
}
