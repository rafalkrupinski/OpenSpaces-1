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
package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 * @see GS-10644
 */
public class UndeployInProgressException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    
    public UndeployInProgressException(ProcessingUnit pu) {
        super(pu, message(pu));
    }

    private static String message(ProcessingUnit pu) {
        return "Last undeployment of " + pu.getName() + " has not completed succesfully.";
    }
    
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticGridServiceAgentProvisioningFailureEvent event = new DefaultElasticGridServiceAgentProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }
}
