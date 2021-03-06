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

package org.openspaces.itest.events.notify.annotation;

import org.openspaces.core.GigaSpace;
import org.openspaces.itest.utils.EmptySpaceDataObject;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class AnnotationNotifyContainerTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    protected TestListener testListener;

    public AnnotationNotifyContainerTests() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(null);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/notify/annotation/notify-annotation.xml"};
    }


    public void testReceiveMessage() throws Exception {
        assertFalse(testListener.isReceivedMessage());
        gigaSpace.write(new EmptySpaceDataObject());
        Thread.sleep(500);
        assertTrue(testListener.isReceivedMessage());
    }

}