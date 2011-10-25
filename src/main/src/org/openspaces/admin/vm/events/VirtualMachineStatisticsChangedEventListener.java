/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.vm.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener called when statistics have changed.
 *
 * @author kimchy
 */
public interface VirtualMachineStatisticsChangedEventListener extends AdminEventListener {

    /**
     * A callback indicating the statistics have changed.
     */
    void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event);
}