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
package org.openspaces.admin.internal.gsa;

import java.util.Collection;
import java.util.Map;

import org.openspaces.admin.gsa.GSAReservationId;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgents extends GridServiceAgents {

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent);

    InternalGridServiceAgent removeGridServiceAgent(String uid);
    
    /**
     * If relevant raises events to relevant subscribers
     * @since 8.0.6
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);
    

    /**
     * @return all agents indexed by their reservationId
     * @since 9.1.1
     */
    Map<GSAReservationId, Collection<GridServiceAgent>> getAgentsGroupByReservationId();
}
