/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the priority resource collectors.<p>
 */
public class TestPriorityResourceCollectors extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestPriorityResourceCollectors(String arg0) {

        super(arg0);
    }

    /**
     * Initializes the resources needed for the tests.<p>
     * 
     * @param cms the cms object
     * @throws CmsException if something goes wrong
     */
    public static void initResources(CmsObject cms) throws CmsException {

        List<CmsProperty> properties = new ArrayList<CmsProperty>(2);
        CmsProperty propPrio = new CmsProperty();
        propPrio.setName(CmsPriorityResourceCollector.PROPERTY_PRIORITY);
        CmsProperty propDate = new CmsProperty();
        propDate.setName(CmsPriorityDateResourceComparator.PROPERTY_DATE);

        long time = System.currentTimeMillis();

        // absolute priority order of the created files:
        // /folder1/sub1/file5, /file1, /folder1/file3, /folder1/file2, /folder1/file1, /folder1/file4 

        // create a file in the root directory              
        propPrio.setStructureValue("15");
        properties.add(propPrio);
        propDate.setStructureValue("" + time);
        properties.add(propDate);
        CmsProperty.setAutoCreatePropertyDefinitions(properties, true);
        cms.createResource("/file1", CmsResourceTypePlain.getStaticTypeId(), null, properties);

        // create a folder in the root directory
        cms.createResource("/folder1", CmsResourceTypeFolder.getStaticTypeId());

        // create a file in the folder directory
        properties.clear();
        propPrio.setStructureValue("5");
        properties.add(propPrio);
        propDate.setStructureValue("" + (time + 20));
        properties.add(propDate);
        cms.createResource("/folder1/file1", CmsResourceTypePlain.getStaticTypeId(), null, properties);

        // create a file in the folder directory
        properties.clear();
        propPrio.setStructureValue("10");
        properties.add(propPrio);
        propDate.setStructureValue("" + time);
        properties.add(propDate);
        cms.createResource("/folder1/file2", CmsResourceTypePlain.getStaticTypeId(), null, properties);

        // create a file in the folder directory
        properties.clear();
        propPrio.setStructureValue("10");
        properties.add(propPrio);
        propDate.setStructureValue("" + (time + 10));
        properties.add(propDate);
        cms.createResource("/folder1/file3", CmsResourceTypePlain.getStaticTypeId(), null, properties);

        // create a file in the folder directory
        properties.clear();
        propPrio.setStructureValue("1");
        properties.add(propPrio);
        propDate.setStructureValue("" + (time + 30));
        properties.add(propDate);
        cms.createResource("/folder1/file4", CmsResourceTypePlain.getStaticTypeId(), null, properties);

        // create a file in the folder directory
        properties.clear();
        propPrio.setStructureValue("20");
        properties.add(propPrio);
        propDate.setStructureValue("" + (time + 40));
        properties.add(propDate);
        CmsResource res = cms.createResource("/folder1/file5", CmsResourceTypePlain.getStaticTypeId(), null, properties);
        res.setDateExpired(time);
        cms.writeResource(res);

        // create a file of other type in the folder directory
        properties.clear();
        propPrio.setStructureValue("10");
        properties.add(propPrio);
        propDate.setStructureValue("" + (time + 50));
        properties.add(propDate);
        cms.createResource("/folder1/fileJsp", CmsResourceTypeJsp.getJSPTypeId(), null, properties);

        // create a subfolder in the folder1 directory
        cms.createResource("/folder1/sub1", CmsResourceTypeFolder.getStaticTypeId());

        //create a file in the subfolder directory
        properties.clear();
        propPrio.setStructureValue("15");
        properties.add(propPrio);
        propDate.setStructureValue("" + time + 40);
        properties.add(propDate);
        cms.createResource("/folder1/sub1/file5", CmsResourceTypePlain.getStaticTypeId(), null, properties);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestPriorityResourceCollectors.class.getName());

        suite.addTest(new TestPriorityResourceCollectors("testCollectAllInFolderPriority"));
        suite.addTest(new TestPriorityResourceCollectors("testCollectAllInFolderPriorityExcludeTimerange"));
        suite.addTest(new TestPriorityResourceCollectors("testCollectAllInSubTreePriority"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                CmsObject cms = setupOpenCms(null, null, false);
                try {
                    initResources(cms);
                } catch (CmsException exc) {
                    exc.printStackTrace();
                    fail(exc.getMessage());
                }
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectAllInFolderPriority() throws Throwable {

        CmsObject cms = getCmsObject();
        int resTypeIdPlain = CmsResourceTypePlain.getStaticTypeId();
        echo("Testing allInFolderPriorityDateDesc resource collector");

        I_CmsResourceCollector collector = new CmsPriorityResourceCollector();
        List<CmsResource> resources = collector.getResults(cms, "allInFolderPriorityDateDesc", "/folder1/|"
            + resTypeIdPlain
            + "|3");

        // assert that 3 files are returned
        assertEquals(3, resources.size());

        CmsResource res;

        // order descending determined by root path
        res = resources.get(0);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        res = resources.get(1);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());

        res = resources.get(2);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectAllInFolderPriorityExcludeTimerange() throws Throwable {

        CmsObject cms = getCmsObject();
        int resTypeIdPlain = CmsResourceTypePlain.getStaticTypeId();
        echo("Testing allInFolderPriorityDateDesc resource collector including time range");

        I_CmsResourceCollector collector = new CmsPriorityResourceCollector();
        List<CmsResource> resources = collector.getResults(cms, "allInFolderPriorityDateDesc", "/folder1/|"
            + resTypeIdPlain
            + "|0");

        // assert that 4 files are returned
        assertEquals(4, resources.size());

        resources = collector.getResults(cms, "allInFolderPriorityDateDesc", "/folder1/|"
            + resTypeIdPlain
            + "|"
            + CmsCollectorData.PARAM_EXCLUDETIMERANGE);

        // assert that 5 files are returned
        assertEquals(5, resources.size());

        CmsResource res;

        // order descending determined by root path
        res = resources.get(0);
        assertEquals("/sites/default/folder1/file5", res.getRootPath());

        res = resources.get(1);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        res = resources.get(2);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());

        res = resources.get(3);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());

        res = resources.get(4);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());
    }

    /**
     * Tests the "allInSubTreePriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectAllInSubTreePriority() throws Throwable {

        CmsObject cms = getCmsObject();
        int resTypeIdPlain = CmsResourceTypePlain.getStaticTypeId();
        echo("Testing allInSubTreePriorityDesc resource collector");

        I_CmsResourceCollector collector = new CmsPriorityResourceCollector();
        List<CmsResource> resources = collector.getResults(cms, "allInSubTreePriorityDateDesc", "/|"
            + resTypeIdPlain
            + "|4");

        // assert that 4 files are returned
        assertEquals(4, resources.size());

        CmsResource res;

        // order descending determined by root path
        res = resources.get(0);
        assertEquals("/sites/default/folder1/sub1/file5", res.getRootPath());

        res = resources.get(1);
        assertEquals("/sites/default/file1", res.getRootPath());

        res = resources.get(2);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        res = resources.get(3);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());

        resources = collector.getResults(cms, "allInSubTreePriorityDateDesc", "/|1");

        // assert that all 6 plain files are returned
        assertEquals(6, resources.size());

        res = resources.get(0);
        assertEquals("/sites/default/folder1/sub1/file5", res.getRootPath());

        res = resources.get(5);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());
    }
}
