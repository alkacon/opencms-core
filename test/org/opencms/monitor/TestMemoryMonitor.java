/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/monitor/TestMemoryMonitor.java,v $
 * Date   : $Date: 2005/02/17 12:46:01 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.monitor;

import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the memory monitor.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.5.4
 */
public class TestMemoryMonitor extends OpenCmsTestCase {
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestMemoryMonitor(String arg0) {
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
        suite.setName(TestMemoryMonitor.class.getName());
             
        suite.addTest(new TestMemoryMonitor("testMemoryMonitor"));
         
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
     * Tests the memory monitor.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testMemoryMonitor() throws Exception {
        
        System.out.println("Testing the OpenCms memory monitor.");
                
        // generate job description 
        CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
        CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());
        jobInfo.setContextInfo(contextInfo);
        jobInfo.setJobName("Memory monitor");
        jobInfo.setClassName(CmsMemoryMonitor.class.getName());
        jobInfo.setReuseInstance(true);
        jobInfo.setCronExpression("0/4 * * * * ?");
        
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
        } while (seconds < 19);
        
        assertEquals(5, OpenCms.getMemoryMonitor().getLogCount());
    }
}
