/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/notification/TestContentNotification.java,v $
 * Date   : $Date: 2005/10/19 09:24:33 $
 * Version: $Revision: 1.1.2.2 $
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
 
package org.opencms.notification;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the OpenCms content notification.<p>
 * 
 * @author Jan Baudisch 
 * @version $Revision: 1.1.2.2 $
 */
public class TestContentNotification extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestContentNotification(String arg0) {
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
        suite.setName(TestContentNotification.class.getName());
                
        suite.addTest(new TestContentNotification("testContentNotification"));
               
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
     * Sets responsibles to a file and then tests the readResponsibleUsers method of CmsObject .<p>
     *  
     * @throws Throwable if something goes wrong
     */
    public void testContentNotification() throws Throwable {
        
        echo("Testing OpenCms content notification");
        CmsObject cms = getCmsObject();
        // initialize calendars
        
        GregorianCalendar today = new GregorianCalendar(TimeZone.getDefault(), CmsLocaleManager.getDefaultLocale()); 
        today.setTimeInMillis(cms.getRequestContext().getRequestTime());
        GregorianCalendar inFiveDays = (GregorianCalendar)today.clone();
        inFiveDays.add(Calendar.DAY_OF_YEAR, 5);
        GregorianCalendar inEightDays = (GregorianCalendar)today.clone();
        inEightDays.add(Calendar.DAY_OF_YEAR, 8);
        GregorianCalendar oneDayBefore = (GregorianCalendar)today.clone();
        oneDayBefore.add(Calendar.DAY_OF_YEAR, -1);
        

        echo("yesterday: " + oneDayBefore.getTimeInMillis());
        echo("today: " + today.getTimeInMillis());
        echo("in five days: " + inFiveDays.getTimeInMillis());
        echo("inEightDays: " + inEightDays.getTimeInMillis());
        
        // create three users, two of them belonging to a group
        CmsUser fry = cms.createUser("fry", "password", "First test user", new HashMap());
         
        // create a number of resources
        String folder = "folder1/";
        String expired01 = "folder1/expired01.html";
        CmsResource expired = cms.createResource(expired01, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(expired01, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateExpired(expired01, inFiveDays.getTimeInMillis(), false);
        
        String expired02 = "folder1/expired02.html";
        cms.createResource(expired02, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(expired02, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateExpired(expired02, inEightDays.getTimeInMillis(), false);
        
        String expired03 = "folder1/expired03.html";
        cms.createResource(expired03, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(expired03, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateExpired(expired03, oneDayBefore.getTimeInMillis(), false);
        
        String released01 = "folder1/released01.html";
        CmsResource released = cms.createResource(released01, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(released01, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateReleased(released01, inFiveDays.getTimeInMillis(), false);
        
        String released02Name = "folder1/released02.html";
        cms.createResource(released02Name, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(released02Name, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateReleased(released02Name, inEightDays.getTimeInMillis(), false);
        
        String released03 = "folder1/released03.html";
        cms.createResource(released03, CmsResourceTypeXmlPage.getStaticTypeId());
        cms.chacc(released03, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), "+s");
        cms.setDateReleased(released03, oneDayBefore.getTimeInMillis(), false);
        
        cms.lockResource(folder);
        cms.writePropertyObject(folder, new CmsProperty(
            CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION, CmsStringUtil.TRUE, CmsStringUtil.TRUE));
        cms.unlockResource(folder);
        cms.publishProject();
        Iterator notifications = new CmsNotificationCandidates(cms).getContentNotifications().iterator();
        // there should be exactly one notification
        while (notifications.hasNext()) {
            CmsContentNotification notification = (CmsContentNotification)notifications.next();
            assertTrue(notification.getResponsible().equals(fry)); // fry should be notified;
            Collection notificationCauses = notification.getNotificationCauses();
            assertTrue(notificationCauses.contains(new CmsExtendedNotificationCause(expired, 
                CmsExtendedNotificationCause.RESOURCE_EXPIRES, new Date(expired.getDateExpired()))));
            assertTrue(notificationCauses.contains(new CmsExtendedNotificationCause(released, 
                CmsExtendedNotificationCause.RESOURCE_RELEASE, new Date(released.getDateReleased()))));
            // there should be no other resources contained in the notification
            assertEquals(notificationCauses.size(), 2);
        }
    }
}