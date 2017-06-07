/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test.performance;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for lock operation.<p>
 */
public class TestFill extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestFill(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestFill.class.getName());

        //suite.addTest(new TestFill("testFillResources"));
        suite.addTest(new TestFill("testPermissionsWithOUs"));
        suite.addTest(new TestFill("testResWithProps"));
        suite.addTest(new TestFill("testReadFile"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * fills the db with organizational units.<br>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPermissionsWithOUs() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Test filling the db with tons of ous");

        // prepare the scenario
        String resourcename = "testPermissionsWithOu.jsp";
        CmsResource res = cms.createResource(resourcename, CmsResourceTypeJsp.getJSPTypeId());

        // check the initial performance
        long t = System.currentTimeMillis();
        OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeJsp.getStaticTypeName()).isEditable(
            cms,
            res);
        t = System.currentTimeMillis() - t;
        echo("initial permissions read in " + t + " msecs");

        int x = 50;
        for (int k = 0; k < x; k++) {
            // create some ous
            t = System.currentTimeMillis();
            int n = 10;
            for (int i = 0; i < n; i++) {
                int number = (n * k) + i;
                cms.createResource("testPermissionsWithOu" + number, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
                OpenCms.getPublishManager().publishResource(cms, "testPermissionsWithOu" + number);
                OpenCms.getPublishManager().waitWhileRunning();
                OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    cms,
                    "testPermissionsWithOu" + number,
                    "test" + number,
                    0,
                    "/testPermissionsWithOu" + number);
            }
            t = System.currentTimeMillis() - t;
            echo("" + n + " ous created in " + t + " msecs");

            // check the performance
            String newName = "testPermissionsWithOu" + ((n * k) + 1) + "/" + resourcename;
            cms.copyResource(resourcename, newName);
            res = cms.readResource(newName);
            t = System.currentTimeMillis();
            OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeJsp.getStaticTypeName()).isEditable(
                cms,
                res);
            t = System.currentTimeMillis() - t;
            echo("permissions with " + (n * (k + 1)) + " OUs read in " + t + " msecs");
        }
    }

    /**
     * fills the db with resources in average: <br>
     *    <ul>
     *        <li>10 folders in 5 subfolders
     *        <li>20 files in each folder, 75% binary / 30% text files,<li>
     *        <li>with 10 properties, 60% individual / 30% shared properties.<li>
     *    </ul>
     * that is a total of app. 10000 files, and 100000 property values. <p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFillResources() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Test filling the db with tons of files");
        long t = System.currentTimeMillis();
        int nFiles = generateContent(cms, "/", 10, 5, 10, 0.6, 20, 0.75);
        t = System.currentTimeMillis() - t;
        echo("" + nFiles + " files have been created in " + t + " msecs");
    }

    /**
     * Performance test for readFile.<p>
     * 10,000 files will be read and 20% of them are binary.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadFile() throws Throwable {

        CmsObject cms = getCmsObject();
        int nFiles = generateContent(cms, "/", 10000, 0.2);

        Iterator listIt = cms.readResources("/", CmsResourceFilter.ALL_MODIFIED).iterator();
        OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, null);

        long startT = System.currentTimeMillis();
        while (listIt.hasNext()) {
            CmsResource resource = (CmsResource)listIt.next();
            if (resource.isFile()) {
                cms.readFile(resource.getName());
            }
        }
        long endT = System.currentTimeMillis();
        long workT = endT - startT;
        echo("" + nFiles + " files have been read in " + workT + " msecs");
    }

    /**
     * Tests the <code>{@link CmsObject#readResourcesWithProperty(String)}</code> method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testResWithProps() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the CmsObject#readResourcesWithProperty(String) method");
        String prop = "NavPos";
        long t = System.currentTimeMillis();
        List l = cms.readResourcesWithProperty(prop);
        t = System.currentTimeMillis() - t;
        echo("There are " + l.size() + " files with prop " + prop);
        echo("readResourcesWithProperty(String) performance was: " + t + " msecs");
    }
}
