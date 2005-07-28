/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/module/TestModuleDeleteThread.java,v $
 * Date   : $Date: 2005/07/28 15:53:10 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.workplace.threads.CmsModuleDeleteThread;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the deleting of modules using the module delete thread, 
 * comparing this to the deletion using the module manager alone.<p>
 * 
 * @author Olaf Watteroth
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $
 */
public class TestModuleDeleteThread extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestModuleDeleteThread(String arg0) {

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
        suite.setName(TestModuleDeleteThread.class.getName());

        // test to delete a module without resources with a single and with two threads
        suite.addTest(new TestModuleDeleteThread("testModuleDeleteThread"));
        // test to delete a module with non-existing resources using the module delete thread
        suite.addTest(new TestModuleDeleteThread("testModuleResourcesDeleteThread"));
        // test to delete a module with non-existing resources using the CmsModuleManager - to compare with above
        suite.addTest(new TestModuleDeleteThread("testModuleResourcesDelete"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
                // this test causes issues that are written to the error log channel
                OpenCmsTestLogAppender.setBreakOnError(false);
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test to delete a module without resources with a single and with two threads.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testModuleDeleteThread() throws Exception {

        echo("Testing to delete a module without resources using the module delete thread.");
        // get a reference to the CmsObject
        CmsObject cms = getCmsObject();

        // list for the module - used later
        List moduleDeleteList;

        // create a new blank module
        String moduleName = "org.opencms.test.testModuleDeleteThread";
        CmsModule module1 = new CmsModule(
            moduleName,
            "Testing to delete a single module using the module delete thread/1",
            "ModuleGroup",
            null,
            null,
            new CmsModuleVersion("1.0"),
            "Olaf Watteroth",
            "watterot@inf.fu-berlin.de",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            null,
            null);

        OpenCms.getModuleManager().addModule(cms, module1);
        //      basic check if the module was created correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not created!");
        }

        // create two CmsModuleDeleteThread's for testing
        moduleDeleteList = new ArrayList();
        moduleDeleteList.add(moduleName);
        // create a single Thread to delete the module
        CmsModuleDeleteThread thread1 = new CmsModuleDeleteThread(cms, moduleDeleteList, false, false);

        // start the threads
        thread1.start();
        // wait till the thread finish
        thread1.join();

        while (thread1.isAlive()) {
            // check if thread1 is still running and wait to finish
        }
        // try to get the deleted module
        CmsModule temp = OpenCms.getModuleManager().getModule(moduleName);
        // test if the module is null - it should be 'cause it was deleted
        echo("Test if the module still exists");
        assertNull(temp);

        CmsModule module2 = new CmsModule(
            moduleName,
            "Testing to delete a single module using the module delete thread/2",
            "ModuleGroup",
            null,
            null,
            new CmsModuleVersion("1.0"),
            "Olaf Watteroth",
            "watterot@inf.fu-berlin.de",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            null,
            null);

        OpenCms.getModuleManager().addModule(cms, module2);

        // Create two CmsModuleDeleteThread's for testing
        moduleDeleteList = new ArrayList();
        moduleDeleteList.add(moduleName);
        echo("Created a new module again and try to delete it - this time with two threads at once");
        CmsModuleDeleteThread thread_parallel_1 = new CmsModuleDeleteThread(cms, moduleDeleteList, false, false);
        CmsModuleDeleteThread thread_parallel_2 = new CmsModuleDeleteThread(cms, moduleDeleteList, false, false);

        // start the threads
        thread_parallel_1.start();
        thread_parallel_2.start();

        // wait 'till all threads finish
        thread_parallel_1.join();
        thread_parallel_2.join();

        while (thread_parallel_1.isAlive() & thread_parallel_2.isAlive()) {
            // check if all threads finished
            Thread.sleep(1000);
        }

        // try to get the deleted module
        module1 = OpenCms.getModuleManager().getModule(moduleName);
        // test if the module is null - it should be 'cause it was deleted
        echo("Exceptions will be logged - but the module should be deleted correctly");
        assertNull(module1);
    }

    /**
     * Test to delete a module with non-existing resources using the module delete thread.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testModuleResourcesDeleteThread() throws Exception {

        echo("Test to delete a module with non-existing resources using the module delete thread");

        CmsObject cms = getCmsObject();
        String moduleName = "org.opencms.test.testModuleResourcesDeleteThread";

        String res1 = "/system/modules/tests/test1/";
        String res2 = "/system/modules/tests/test2/";
        String res3 = "/system/modules/tests/test3/";
        String res4 = "/system/modules/tests/test4/";

        List resources = new ArrayList();
        resources.add(res1);
        resources.add(res2);
        resources.add(res3);
        resources.add(res4);

        CmsModule module1 = new CmsModule(
            moduleName,
            "Test to delete a module with non-existing resources using the module delete thread",
            "ModuleGroup",
            null,
            null,
            new CmsModuleVersion("1.0"),
            "Olaf Watteroth",
            "watterot@inf.fu-berlin.de",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            resources,
            null);

        OpenCms.getModuleManager().addModule(cms, module1);
        module1 = OpenCms.getModuleManager().getModule(moduleName);

        assertEquals(0, module1.getParameters().size());
        assertEquals(4, module1.getResources().size());

        // Now its new code
        echo("Module created. Now try to delete it");
        // Now try to delete this module after it was added
        // Create a CmsModuleDeleteThread's for testing
        List module = new ArrayList();
        module.add(moduleName);

        // Create a single Thread to delete the module
        CmsModuleDeleteThread thread1 = new CmsModuleDeleteThread(cms, module, false, false);

        // Start the threads
        thread1.start();

        // Wait till the thread finish
        thread1.join();

        while (thread1.isAlive()) {
            // Check if thread1 is still running and wait to finish
            Thread.sleep(1000);
        }

        // try to get the deleted module
        module1 = OpenCms.getModuleManager().getModule(moduleName);
        // test if the module is null - it should be 'cause it was deleted
        echo("Test if the module still exists");
        assertNull(module1);
    }

    /**
     * Test to delete a module with non-existing resources using the CmsModuleManager.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleResourcesDelete() throws Throwable {

        echo("Test to delete a module with non-existing resources using the CmsModuleManager");

        CmsObject cms = getCmsObject();
        String moduleName = "org.opencms.test.testModuleResourcesDelete";

        String res1 = "/system/modules/tests/test1/";
        String res2 = "/system/modules/tests/test2/";
        String res3 = "/system/modules/tests/test3/";
        String res4 = "/system/modules/tests/test4/";

        List resources = new ArrayList();
        resources.add(res1);
        resources.add(res2);
        resources.add(res3);
        resources.add(res4);

        CmsModule module1 = new CmsModule(
            moduleName,
            "Test to delete a module with non-existing resources using the CmsModuleManager",
            "ModuleGroup",
            null,
            null,
            new CmsModuleVersion("1.0"),
            "Olaf Watteroth",
            "watterot@inf.fu-berlin.de",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            resources,
            null);

        OpenCms.getModuleManager().addModule(cms, module1);
        module1 = OpenCms.getModuleManager().getModule(moduleName);

        assertEquals(4, module1.getResources().size());

        echo("Now try to delete it *the normal way*");
        OpenCms.getModuleManager().deleteModule(cms, moduleName, false, new CmsShellReport(cms.getRequestContext().getLocale()));

        module1 = OpenCms.getModuleManager().getModule(moduleName);
        // now it should be null
        echo("Now check if module was deleted");
        assertNull(module1);
        echo("Test finished");
    }
}