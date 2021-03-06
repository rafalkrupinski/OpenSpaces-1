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
package org.openspaces.itest.archive;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.archive.ArchivePollingContainer;
import org.openspaces.archive.ArchivePollingContainerConfigurer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.AbstractJiniTransactionManager;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.openspaces.events.DynamicEventTemplate;
import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.events.support.AnnotationProcessorUtils;
import org.openspaces.events.support.EventContainersBus;
import org.openspaces.itest.events.pojos.MockPojoFifoGrouping;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import com.j_spaces.core.IJSpace;

/**
 * GS-10785 Test Archive Container
 * 
 * In order to run this test in eclise, edit the JUnit Test Configuration: Click the ClassPath tab,
 * click User Entries. Add Folders: Gigaspaces/ and Gigaspaces/src/java/resources and openspaces/src/main/java/
 * 
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class ArchiveContainerTest {

    private final Log logger = LogFactory.getLog(this.getClass());
    
    private final String TEST_RAW_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-raw.xml";
    private final String TEST_NAMESPACE_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-namespace.xml";
    private final String TEST_ANNOTATION_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-annotation.xml";
    private final String TEST_WRONG_TEMPLATE_ANNOTATION_XML = "/org/openspaces/itest/archive/wrongtemplate/test-wrong-template-archive-annotation.xml";
    private final String TEST_WRONG_HANDLER_ANNOTATION_XML = "/org/openspaces/itest/archive/wronghandler/test-wrong-handler-archive-annotation.xml";
    
    private final String TEST_DYNAMIC_RAW_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-raw.xml";
    private final String TEST_DYNAMIC_NAMESPACE_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-namespace.xml";
    private final String TEST_DYNAMIC_ANNOTATION_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-annotation.xml";

    /**
     * Tests archiver with raw spring bean xml
     */
    @Test
    public void testXmlRaw() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_RAW_XML, expectedBatchSize);
    }

    /**
     * Tests archiver with namespace spring bean xml
     */
    @Test 
    public void testXmlNamespace() throws InterruptedException {
        //see batch-size="2" in xml file
        final int expectedBatchSize = 2;
        xmlTest(TEST_NAMESPACE_XML , expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test() 
    public void testXmlAnnotation() throws InterruptedException {
    	// see @Archive(batchSize=2) annotation and atomic=true in MockArchiveContainer
        final int expectedBatchSize = 2; 
        xmlTest(TEST_ANNOTATION_XML , expectedBatchSize);
    }
    
    /**
     * Tests archiver with wrong template (processed = true)
     */
    @Test() //GS-11380
    public void testWrongTemplateXmlAnnotation() throws InterruptedException {
    	// see @Archive(batchSize=2) annotation and atomic=true in MockArchiveContainer
        final int expectedBatchSize = 2; 
        try {
        	xmlTest(TEST_WRONG_TEMPLATE_ANNOTATION_XML , expectedBatchSize);
        	Assert.fail("Expected exception");
	    }
	    catch (AssertionError e) {
	    	Assert.assertEquals("expected:<5> but was:<0>", e.getMessage());
	    }
    }
  
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test(expected=BeanCreationException.class)
    public void testXmlWrongAnnotationAttribute() throws InterruptedException {
        final int expectedBatchSize = 2; 
        xmlTest(TEST_WRONG_HANDLER_ANNOTATION_XML , expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateAnnotation() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_ANNOTATION_XML, expectedBatchSize); 
    }

    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateNamespace() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_NAMESPACE_XML, expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateRaw() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_RAW_XML , expectedBatchSize); 
    }
    
    /**
     * Tests archiver with configurer (no Spring beans)
     */
    @Test
    public void testConfigurer() throws Exception {
        configurerTest(TestTemplate.TEMPLATE);
    }

    /**
     * Tests archiver with configurer (wrong template)
     */
    @Test
    public void testWrongTemplateConfigurer() throws Exception {
    	try {
        	configurerTest(TestTemplate.WRONG_TEMPLATE);
        	Assert.fail("Expected exception");
	    }
	    catch (AssertionError e) {
	    	Assert.assertEquals("expected:<5> but was:<0>", e.getMessage());
	    }
    }
    
    /**
     * Tests archiver with atomic archive handler with default batchSize
     */
    @Test
    public void testConfigurerAtomic() throws Exception {
        final boolean atomic = true;
        configurerTest(TestTemplate.TEMPLATE, atomic);
    }

    /**
     * Tests archiver with atomic archive handler with batchSize
     */
    @Test
    public void testConfigurerAtomicBatchSize() throws Exception {
        final boolean atomic = true;
        final boolean remote = false;
        final int batchSize = 2;
        configurerTest(TestTemplate.TEMPLATE, atomic, remote, batchSize);
    }
    
    /**
     * Tests archiver with dynamic template (interface)
     */
    @Test
    public void testConfigurerDynamicTemplateInterface() throws Exception {
        configurerTest(TestTemplate.DYNAMIC_TEMPLATE_INTERFACE);
    }
    
    /**
     * Tests archiver with dynamic template (annotation)
     */
    @Test
    public void testConfigurerDynamicTemplateAnnotation() throws Exception {
        configurerTest(TestTemplate.DYNAMIC_TEMPLATE_ANNOTATION);
    }
    
    /**
     * Tests archiver with dynamic template (method)
     */
    @Test
    public void testConfigurerDynamicTemplateMethod() throws Exception {
        configurerTest(TestTemplate.DYNAMIC_TEMPLATE_METHOD);
    }
    
    @Test
    public void testConfigurerClustered() throws Exception {
        final boolean atomic = false;
        final boolean clustered = true;
        configurerTest(TestTemplate.TEMPLATE, atomic, clustered);
    }
    
    @Test
    public void testConfigurerAtomicClustered() throws Exception {
        final boolean atomic = true;
        final boolean clustered = true;
        configurerTest(TestTemplate.TEMPLATE, atomic, clustered);
    }
    
    enum TestTemplate {
        TEMPLATE,
        WRONG_TEMPLATE,
        DYNAMIC_TEMPLATE_INTERFACE,
        DYNAMIC_TEMPLATE_ANNOTATION,
        DYNAMIC_TEMPLATE_METHOD
    }
    
    private void configurerTest(TestTemplate templateToTest) throws Exception {
        final boolean atomic = false;
        configurerTest(templateToTest, atomic);
    }
    
    private void configurerTest(TestTemplate templateToTest, boolean atomic) throws Exception {
        final boolean remote = false;
        configurerTest(templateToTest, atomic, remote);
    }
    
    private void configurerTest(TestTemplate templateToTest, boolean atomic, boolean remote) throws Exception {
        final Integer batchSize = null;
        configurerTest(templateToTest, atomic, remote, batchSize);
    }
    
    private void configurerTest(TestTemplate templateToTest, boolean atomic, boolean clustered, Integer batchSize) throws Exception {
       
        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").clusterInfo(new ClusterInfo("partitioned-sync2backup",1,null,1,0));
        UrlSpaceConfigurer remoteUrlSpaceConfigurer = new UrlSpaceConfigurer("jini://*/*/space");
        try {
            PlatformTransactionManager transactionManager = null;
            if (atomic) {
                transactionManager = new DistributedJiniTxManagerConfigurer().transactionManager();
                if (transactionManager instanceof AbstractJiniTransactionManager) {
                    //used later to verify that container has a tx manager.
                    ((AbstractJiniTransactionManager)transactionManager).setBeanName("tx-manager");
                }
            }
            
            GigaSpace gigaSpace;
            
            final IJSpace space = urlSpaceConfigurer.create();
            if (clustered) {
                final IJSpace remoteSpace = remoteUrlSpaceConfigurer.create();
                gigaSpace = new GigaSpaceConfigurer(remoteSpace).transactionManager(transactionManager).create();
            }
            else {
                gigaSpace = new GigaSpaceConfigurer(space).transactionManager(transactionManager).create();
            }
                        
            final MockArchiveOperationsHandler archiveHandler = new MockArchiveOperationsHandler();
            archiveHandler.setAtomicArchiveOfMultipleObjects(atomic);

            ArchivePollingContainerConfigurer containerConfigurer = 
                    new ArchivePollingContainerConfigurer(gigaSpace)           
                    .archiveHandler(archiveHandler);
                        
            configureTemplate(containerConfigurer, templateToTest);

            if (atomic) {
                containerConfigurer.transactionManager(transactionManager);
            }
            
            if (atomic && batchSize != null) {
                containerConfigurer.batchSize(batchSize);
            }
            
            // autostart is enabled by default
            ArchivePollingContainer container = containerConfigurer.create();
            
            int expectedBatchSize;
            if (atomic && batchSize != null) {
                expectedBatchSize = batchSize;
            }
            else if (atomic && batchSize == null) {
                expectedBatchSize = 50; //the default both in the annotation and in the archive container.
            }
            else {
                expectedBatchSize = 1;
            }
            test(archiveHandler, gigaSpace, container, expectedBatchSize);
        } finally {
            
            if (remoteUrlSpaceConfigurer != null) {
                remoteUrlSpaceConfigurer.destroy();
            }
            
            if (urlSpaceConfigurer != null) {
                urlSpaceConfigurer.destroy();
            }
        }
    }
    
    private void configureTemplate(ArchivePollingContainerConfigurer containerConfigurer, TestTemplate templateToTest) {
        switch (templateToTest) {
        case TEMPLATE:
            // template is called once
            final MockPojoFifoGrouping template = new MockPojoFifoGrouping();
            template.setProcessed(false);
            containerConfigurer.template(template);
            break;
        
        case WRONG_TEMPLATE:
            // template is called once
            final MockPojoFifoGrouping wrongTemplate = new MockPojoFifoGrouping();
            wrongTemplate.setProcessed(true);
            containerConfigurer.template(wrongTemplate);
            break;
            
        case DYNAMIC_TEMPLATE_INTERFACE:
            // dynamic template returns a different template each call
            final DynamicEventTemplateProvider dynamicTemplate = new DynamicEventTemplateProvider() {
                final AtomicInteger routing = new AtomicInteger();
                @Override
                public Object getDynamicTemplate() {
                    final MockPojoFifoGrouping template = new MockPojoFifoGrouping();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplate(dynamicTemplate);
            break;
            
        case DYNAMIC_TEMPLATE_ANNOTATION:
            // dynamic template returns a different template each call
            final Object dynamicTemplateAnnotation = new Object() {
                final AtomicInteger routing = new AtomicInteger();
                
                @SuppressWarnings("unused")
                @DynamicEventTemplate
                public Object getDynamicTemplate() {
                    final MockPojoFifoGrouping template = new MockPojoFifoGrouping();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplateAnnotation(dynamicTemplateAnnotation);
            break;
            
        case DYNAMIC_TEMPLATE_METHOD:
            // dynamic template returns a different template each call
            final Object dynamicTemplateMethod = new Object() {
                final AtomicInteger routing = new AtomicInteger();
                
                @SuppressWarnings("unused")
                public Object getDynamicTemplateMethod() {
                    final MockPojoFifoGrouping template = new MockPojoFifoGrouping();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplateMethod(dynamicTemplateMethod, "getDynamicTemplateMethod");
            break;
            
        default:
            Assert.fail("Unknown template test " + templateToTest);
        }
    }

    private void xmlTest(String relativeXmlName, int expectedBatchSize) throws InterruptedException {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(relativeXmlName);
        try {
            final MockArchiveOperationsHandler archiveHandler = context.getBean(MockArchiveOperationsHandler.class);
            final GigaSpace gigaSpace = context.getBean(org.openspaces.core.GigaSpace.class);
            ArchivePollingContainer container = getArchivePollingContainer(context);
            Assert.assertTrue("Expected archive container to be configured with use-fifo-grouping=true", container.isUseFifoGrouping());
            test(archiveHandler, gigaSpace, container, expectedBatchSize);
        } finally {
            context.close();
        }
    }

    private ArchivePollingContainer getArchivePollingContainer(final ClassPathXmlApplicationContext context) {
        ArchivePollingContainer container;
        try {
            container = context.getBean(ArchivePollingContainer.class);
        }
        catch (final Exception e) {
            final EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(context);
            container = (ArchivePollingContainer) eventContainersBus.getEventContainers().iterator().next();
        }
        return container;
    }
    
    private void test(MockArchiveOperationsHandler archiveHandler, GigaSpace gigaSpace, ArchivePollingContainer container, int expectedBatchSize) throws InterruptedException {
          boolean atomic = archiveHandler.supportsBatchArchiving();
          int batchSize = container.getBatchSize();
          int actualBatchSize;
            if (atomic) {
                actualBatchSize = batchSize;
            }
            else {
                Assert.assertEquals("Test configuration error. Cannot expect batchSize!=1 if not atomic, since the implementation uses take and not takeMultiple", 1, expectedBatchSize);
                actualBatchSize = 1;
            }
            
            Assert.assertEquals(expectedBatchSize, actualBatchSize);
            Assert.assertTrue("Atomic test must be performed with a space that uses a transaction manager", !atomic || gigaSpace.getTxProvider() != null);
            Assert.assertTrue("Atomic test must be performed with a container that uses a transaction manager", !atomic || container.getTransactionManagerName() != null);

            test(archiveHandler, gigaSpace, actualBatchSize);
    }

    private void test(final MockArchiveOperationsHandler archiveHandler, GigaSpace gigaSpace, final int batchSize)
            throws InterruptedException {

        // TODO: Make it transactional by default?
        final MockPojoFifoGrouping[] entries = new MockPojoFifoGrouping[] { new MockPojoFifoGrouping(false, 1), new MockPojoFifoGrouping(false, 2), new MockPojoFifoGrouping(false, 3), new MockPojoFifoGrouping(false, 4), new MockPojoFifoGrouping(false, 5) };
        final int numberOfEntries = entries.length;
        gigaSpace.writeMultiple(entries);
        TestUtils.repetitive(new Runnable() {

            @Override
            public void run() {
                Assert.assertEquals(numberOfEntries, countEntries(archiveHandler));
            }

        }, 10000);

        if (batchSize >= 5) {
            // MultiTake uses take() and then takeMultiple() or
            // takeMultiple()
            int count = countOperations(archiveHandler);
            Assert.assertTrue("expected at most 2 count operations (" + count +" > 2)", count <= 2);
        }
        else if (batchSize == 2) {
            // MultiTake uses take() and then takeMultiple (twice) or
            // MultiTake uses takeMultiple (three times) 
            Assert.assertEquals(3, countOperations(archiveHandler));
        }
        else if (batchSize == 1) {
            Assert.assertEquals(numberOfEntries, countOperations(archiveHandler));
        }
        else {
            Assert.fail("unexpected batchSize " + batchSize);
        }
    }

    private int countOperations(final MockArchiveOperationsHandler archiveHandler) {
        return archiveHandler.getArchivedObjectsOperations().size();
    }

    private int countEntries(final MockArchiveOperationsHandler archiveHandler) {
        int numberOfArchivedEntries = 0;
        for (final Object[] objects : archiveHandler.getArchivedObjectsOperations()) {
            numberOfArchivedEntries += objects.length;
        }
        return numberOfArchivedEntries;
    }
}
