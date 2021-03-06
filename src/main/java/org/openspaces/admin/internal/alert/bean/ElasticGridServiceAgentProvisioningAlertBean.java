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
package org.openspaces.admin.internal.alert.bean;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.alerts.ElasticGridServiceAgentProvisioningAlert;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEventListener;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;

/**
 * Raises an alert if an elastic processing unit deployment or scale has been affected by a failure to start a new grid service agent.
 * @since 8.0.6
 * @author itaif
 */
public class ElasticGridServiceAgentProvisioningAlertBean 
    extends AbstractElasticProcessingUnitAlertBean 
    implements ElasticGridServiceAgentProvisioningFailureEventListener , ElasticGridServiceAgentProvisioningProgressChangedEventListener  {
    
    private static final AlertSeverity GSA_ALERT_SEVERITY = AlertSeverity.SEVERE;
    private static final String GSA_ALERT_RESOLVED_DESCRIPTION_POSTFIX= "Grid Service Agent provisioning for %s completed succesfully";
    private static final String GSA_ALERT_BEAN_UID = "5d75d7c8-e895-4490-a1d3-2df753c3893e";
    private static final String GSA_ALERT_NAME = "Grid Service Agent Provisioning Alert";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.setBeanUid(GSA_ALERT_BEAN_UID);
        super.setAlertName(GSA_ALERT_NAME);
        super.setAlertSeverity(GSA_ALERT_SEVERITY);
        super.setResolvedAlertDescriptionFormat(GSA_ALERT_RESOLVED_DESCRIPTION_POSTFIX);
        super.afterPropertiesSet();
        
        admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningProgressChanged().add(this, true);
        admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningFailure().add(this);   
    }

    @Override
    public void destroy() throws Exception {
        admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningFailure().remove(this);
        admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningProgressChanged().remove(this);
        super.destroy();
    }

    /**
     * Raises an alert when PU gsa provisioning is still in progress and 
     * this or other pu sharing the same GSA has encountered a gsa provisioning failure.
     */
    @Override
    public void elasticGridServiceAgentProvisioningFailure(ElasticGridServiceAgentProvisioningFailureEvent event) {
        ElasticGridServiceAgentProvisioningAlert alert = new ElasticGridServiceAgentProvisioningAlert(createRaisedAlert(event));
        super.raiseAlert(alert);
    }

    /**
     * Resolves an alert when PU has the GSAs it needs.
     * This includes when a processing unit has been undeployed and all GSAs have been shutdown or not used by the PU.
     */
    @Override
    public void elasticGridServiceAgentProvisioningProgressChanged(ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
        for (Alert baseAlert : createResolvedAlerts(event)) {
            ElasticGridServiceAgentProvisioningAlert alert = new ElasticGridServiceAgentProvisioningAlert(baseAlert);
            super.raiseAlert(alert);
        }
    }
}
