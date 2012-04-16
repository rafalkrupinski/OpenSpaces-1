/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceTransactionDetails;
import org.openspaces.admin.space.SpaceTransactionDetails;

import com.gigaspaces.cluster.activeelection.SpaceMode;

/**
 * @author moran
 */
public class DefaultSpaceTransactionDetails implements SpaceTransactionDetails {

    private final DefaultSpace defaultSpace;

    public DefaultSpaceTransactionDetails(DefaultSpace defaultSpace) {
        this.defaultSpace = defaultSpace;
    }
    
    @Override
    public int getActiveTransactionCount() {
        int count = 0;
        for (SpaceInstance spaceInstance : defaultSpace.getSpaceInstances()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                SpaceInstanceTransactionDetails transactionDetails = spaceInstance.getRuntimeDetails().getTransactionDetails();
                count += transactionDetails.getActiveTransactionCount();
            }
        }
        return count;
    }
}
