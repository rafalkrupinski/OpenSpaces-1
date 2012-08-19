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
package org.openspaces.admin.pu.elastic.config;

import java.util.concurrent.TimeUnit;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ManualCapacityPerZonesScaleConfigurer implements ScaleStrategyConfigurer<ManualCapacityPerZonesScaleConfig> {

    ManualCapacityPerZonesScaleConfig config = new ManualCapacityPerZonesScaleConfig();
    
    public ManualCapacityPerZonesScaleConfigurer addZone(String[] zones, CapacityRequirementsConfig capacity) {
        config.addCapacity(zones, capacity);
        return this;
    }
    
    public ManualCapacityPerZonesScaleConfig create() {
        return config; 
    }

    @Override
    public ManualCapacityPerZonesScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }

    @Override
    public ManualCapacityPerZonesScaleConfigurer atMostOneContainerPerMachine() {
        config.setAtMostOneContainerPerMachine(true);
        return this;
    }

    @Override
    public ManualCapacityPerZonesScaleConfigurer pollingInterval(long pollingInterval, TimeUnit timeUnit) {
        config.setPollingIntervalSeconds((int)timeUnit.toSeconds(pollingInterval));
        return this;
    }

}
