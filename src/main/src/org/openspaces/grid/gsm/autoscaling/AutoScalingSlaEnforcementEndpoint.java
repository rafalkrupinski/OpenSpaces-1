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
package org.openspaces.grid.gsm.autoscaling;

import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;

/**
 * A service that on demand enforces auto scaling rules providing the new capacity requirements as a result. 
 *  
 * @author itaif
 * @since 9.0.0
 * @see AutoScalingSlaPolicy
 */
public interface AutoScalingSlaEnforcementEndpoint extends ServiceLevelAgreementEnforcementEndpoint{
    
    void enforceSla(AutoScalingSlaPolicy sla) throws AutoScalingSlaEnforcementInProgressException;
    
    /**
     * @return the new required capacity based on auto scaling rules
     */
    CapacityRequirements getNewCapacityRequirements() ;

}
