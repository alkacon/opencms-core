/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/scheduler/TestCmsSchedulerInSystem.java,v $
 * Date   : $Date: 2004/11/24 15:57:25 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.scheduler;

import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestPropertiesSingleton;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the OpenCms scheduler in a running system.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class TestCmsSchedulerInSystem extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsSchedulerInSystem(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        OpenCmsTestPropertiesSingleton.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSchedulerInSystem.class.getName());
                
        suite.addTest(new TestCmsSchedulerInSystem("testDefaultConfiguration"));
               
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms("simpletest", "/sites/default/");
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    }         
    
    /**
     * Test case for the initCmsObject methods.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testDefaultConfiguration() throws Exception {
        
        System.out.println("Trying to run a persistent OpenCms job 5x with the OpenCms system scheduler.");
        TestScheduledJob.m_runCount = 0;
                
        // generate job description 
        CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
        CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());
        jobInfo.setContextInfo(contextInfo);
        jobInfo.setJobName("Test job");
        jobInfo.setClassName(TestScheduledJob.class.getName());
        jobInfo.setReuseInstance(true);
        jobInfo.setCronExpression("0/2 * * * * ?");
        
        // add the job to the manager
        OpenCms.getScheduleManager().scheduleJob(getCmsObject(), jobInfo);
        
        int seconds = 0;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("Something caused the waiting test thread to interrupt!");
            }
            seconds++;
        } while ((seconds < TestCmsScheduler.SECONDS_TO_WAIT) && (TestScheduledJob.m_runCount < 5));


        if (TestScheduledJob.m_runCount == 5) {
            System.out.println("Test job was correctly run 5 times in OpenCms scheduler.");
        } else {
            fail("Test class not run after " + TestCmsScheduler.SECONDS_TO_WAIT + " seconds.");
        }
        
        if (TestScheduledJob.m_instanceCountCopy == 5) {
            System.out.println("Instance counter was correctly incremented 5 times.");
        } else {
            fail("Instance counter was not incremented!");
        }         
    }
}