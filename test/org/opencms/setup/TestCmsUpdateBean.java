/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/setup/Attic/TestCmsUpdateBean.java,v $
 * Date   : $Date: 2006/11/28 15:37:20 $
 * Version: $Revision: 1.1.4.2 $
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

package org.opencms.setup;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.0.0
 */
public class TestCmsUpdateBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsUpdateBean(String arg0) {

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
        suite.setName(TestCmsUpdateBean.class.getName());

        suite.addTest(new TestCmsUpdateBean("testUnlockSystem"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("systemtest", "/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Wrapper class to extend accessibility of tested method.<p>
     */
    class TestUpdateBean extends CmsUpdateBean {
    
        /**
         * Wraps the unlockSystem method.
         * 
         * @param cms the cms object
         * @param report the report
         * @throws CmsException if something goes wrong
         */
        public void testUnlockSystem(CmsObject cms, I_CmsReport report) throws CmsException {
            unlockSystem(cms, report);
        }
    }
    
    /**
     * Tests the unlockSystem method of the update bean.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testUnlockSystem() throws Throwable {
        
        TestUpdateBean updateBean = new TestUpdateBean();
        List locked;
        String test;
        
        CmsObject cms = getCmsObject();
        I_CmsReport report = new CmsShellReport(CmsLocaleManager.getDefaultLocale());

        cms.getRequestContext().setSiteRoot("/");
        
        echo("Unlocking everything already unlocked");
        updateBean.testUnlockSystem(cms, report);
        locked = cms.getLockedResources("/", CmsLockFilter.FILTER_ALL);
        assertTrue(locked.isEmpty());
        
        echo("Unlocking test resource locked as admin");
        test = "/system/test.txt";
        cms.lockResource(test);
        assertLock(cms, test, CmsLockType.EXCLUSIVE);
        updateBean.testUnlockSystem(cms, report);
        locked = cms.getLockedResources("/", CmsLockFilter.FILTER_ALL);
        assertTrue(locked.isEmpty());
        
        echo("Unlocking system folder locked as admin");
        test = "/system";
        cms.lockResource(test);
        assertLock(cms, test, CmsLockType.EXCLUSIVE);
        updateBean.testUnlockSystem(cms, report);
        locked = cms.getLockedResources("/", CmsLockFilter.FILTER_ALL);
        assertTrue(locked.isEmpty()); 
        
        echo("Unlocking test resource locked as other user");
        cms.loginUser("test1" , "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        test = "/system/test.txt";
        cms.lockResource(test);
        assertLock(cms, test, CmsLockType.EXCLUSIVE);
        cms.loginUser("Admin" , "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        updateBean.testUnlockSystem(cms, report);
        locked = cms.getLockedResources("/", CmsLockFilter.FILTER_ALL);
        assertTrue(locked.isEmpty());  
        
        echo("Unlocking system folder locked as other user");
        cms.loginUser("test1" , "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        test = "/system";
        cms.lockResource(test);
        assertLock(cms, test, CmsLockType.EXCLUSIVE);
        cms.loginUser("Admin" , "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        updateBean.testUnlockSystem(cms, report);
        locked = cms.getLockedResources("/", CmsLockFilter.FILTER_ALL);
        assertTrue(locked.isEmpty());  

    }
}
