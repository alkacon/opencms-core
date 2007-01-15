/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestCmsPrincipal.java,v $
 * Date   : $Date: 2007/01/15 18:48:35 $
 * Version: $Revision: 1.2.4.3 $
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
 
package org.opencms.security;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for <code>{@link org.opencms.security.CmsPrincipal}</code> (and it's subclasses).<p>
 */
public class TestCmsPrincipal extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsPrincipal(String arg0) {
        
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
        suite.setName(TestCmsPrincipal.class.getName());

        suite.addTest(new TestCmsPrincipal("testBasicReadOperation"));
        
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
     * Tests basic principal read operation.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testBasicReadOperation() throws Exception {

        echo("Testing basic principal read operation");
        CmsObject cms = getCmsObject();

        I_CmsPrincipal principal;
        String prefixedName;

        prefixedName = CmsPrincipal.getPrefixedUser(CmsOrganizationalUnit.appendFqn(null, OpenCms.getDefaultUsers().getUserAdmin()));
        principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        assertTrue(principal.isUser());
        assertFalse(principal.isGroup());
        assertEquals(prefixedName, principal.getPrefixedName());

        prefixedName = CmsPrincipal.getPrefixedGroup(CmsRole.ROOT_ADMIN.getGroupName());
        principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        assertFalse(principal.isUser());
        assertTrue(principal.isGroup());
        assertEquals(prefixedName, principal.getPrefixedName());

        // negative test
        prefixedName = "kaputt";
        CmsException caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsSecurityException);
        if (caught != null) {
            assertSame(Messages.ERR_INVALID_PRINCIPAL_1, caught.getMessageContainer().getKey());
        }

        // negative test 2
        prefixedName = CmsPrincipal.getPrefixedUser("kaputt");
        caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsDbEntryNotFoundException);
        if (caught != null) {
            assertSame(org.opencms.db.Messages.ERR_READ_USER_FOR_NAME_1, caught.getMessageContainer().getKey());
        }

        // negative test 3
        prefixedName = CmsPrincipal.getPrefixedGroup("kaputt");
        caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsDbEntryNotFoundException);
        if (caught != null) {
            assertSame(org.opencms.db.Messages.ERR_READ_GROUP_FOR_NAME_1, caught.getMessageContainer().getKey());
        }
    }    
}