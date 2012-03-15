/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.core.transaction;

import com.gigaspaces.internal.cluster.node.impl.processlog.multisourcesinglefile.DistributedTransactionProcessingConfiguration;

/**
 * A bean for configuring distributed transaction processing at Mirror/Sink components.
 * 
 * Its possible to configure two parameters:
 * <ul>
 * <li>
 * {@link #setDistributedTransactionWaitTimeout(Float)} - determines the wait timeout for all distributed transaction participants data
 * before committing only the data that arrived.
 * </li>
 * <li>
 * {@link #setDistributedTransactionWaitForOperations(Integer)} - determines the number of operations to wait for before committing
 *  a distributed transaction when data from all participants haven't arrived.  
 * </li>
 * </ul>
 * 
 * @author idan
 * @since 8.0.4
 *
 */
public class DistributedTransactionProcessingConfigurationFactoryBean {

    private Long distributedTransactionWaitTimeout;
    private Long distributedTransactionWaitForOperations;
    
    public DistributedTransactionProcessingConfigurationFactoryBean() {
    }
    
    public Long getDistributedTransactionWaitTimeout() {
        return distributedTransactionWaitTimeout;
    }
    
    public void setDistributedTransactionWaitTimeout(Long distributedTransactionWaitTimeout) {
        this.distributedTransactionWaitTimeout = distributedTransactionWaitTimeout;
    }
    
    public Long getDistributedTransactionWaitForOperations() {
        return distributedTransactionWaitForOperations;
    }
    
    public void setDistributedTransactionWaitForOperations(Long distributedTransactionWaitForOperations) {
        this.distributedTransactionWaitForOperations = distributedTransactionWaitForOperations;
    }

    public void copyParameters(DistributedTransactionProcessingConfiguration transactionProcessingConfiguration) {
        if (distributedTransactionWaitTimeout != null)
            transactionProcessingConfiguration.setTimeoutBeforePartialCommit(distributedTransactionWaitTimeout.longValue());
        if (distributedTransactionWaitForOperations != null)
            transactionProcessingConfiguration.setWaitForOperationsBeforePartialCommit(distributedTransactionWaitForOperations.longValue());
    }
    
}
