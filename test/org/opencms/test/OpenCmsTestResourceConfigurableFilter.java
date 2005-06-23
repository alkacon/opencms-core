/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceConfigurableFilter.java,v $
 * Date   : $Date: 2005/06/23 10:47:27 $
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
 
package org.opencms.test;

/**
 * Configurable filter class for OpenCms VFS access method tests.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $
 */
public class OpenCmsTestResourceConfigurableFilter extends OpenCmsTestResourceFilter {

    /**
     * Creates a new OpenCmsTestResourceFilter with all tests enabled.<p>
     */
    public OpenCmsTestResourceConfigurableFilter() {
        m_contentId = true;
        m_dateCreated = true;
        m_dateLastModified = true;
        m_dateReleased = true;
        m_dateExpired = true;   
        m_flags = true;
        m_isTouched = true;
        m_length = true;
        m_siblingCount = true;
        m_name = true;
        m_projectLastModified = true;
        m_resourceId = true;
        m_state = true;
        m_structureId = true;
        m_type = true;
        m_userCreated = true;
        m_userLastModified = true;
        m_lockstate = true;
        m_contents = true;
        m_properties = true;
        m_acl = true;
        m_ace = true;
    }    
    
    /**
     * Creates a new OpenCmsTestResourceFilter based on an existing filter.<p>
     * 
     * @param baseFilter the filter to base this filter on
     */
    public OpenCmsTestResourceConfigurableFilter(OpenCmsTestResourceFilter baseFilter) {
        m_contentId = baseFilter.testContentId();
        m_dateCreated = baseFilter.testDateCreated();
        m_dateLastModified = baseFilter.testDateLastModified();
        m_dateReleased = baseFilter.testDateReleased();
        m_dateExpired = baseFilter.testDateExpired();   
        m_flags = baseFilter.testFlags();
        m_isTouched = baseFilter.testTouched();
        m_length = baseFilter.testLength();
        m_siblingCount = baseFilter.testSiblingCount();
        m_name = baseFilter.testName();
        m_projectLastModified = baseFilter.testProjectLastModified();
        m_resourceId = baseFilter.testResourceId();
        m_state = baseFilter.testState();
        m_structureId = baseFilter.testStructureId();
        m_type = baseFilter.testType();
        m_userCreated = baseFilter.testUserCreated();
        m_userLastModified = baseFilter.testUserLastModified();
        m_lockstate = baseFilter.testLock();
        m_contents = baseFilter.testContents();
        m_properties = baseFilter.testProperties(); 
        m_ace = baseFilter.testAce();
        m_acl = baseFilter.testAcl();
    }      
    
    /**
     * Disables the access list test.<p>
     */
    public void disableAclTest() {
        m_acl = false;
    }
    
    /**
     * Disables the access list test.<p>
     */
    public void disableAceTest() {
        m_ace = false;
    }    
    
    /**
     * Disables the Content Id test.<p>
     */
    public void disableContentIdTest() {
        m_contentId = false;
    }
    
    /**
     * Disables the Contenttest.<p>
     */
    public void disableContentsTest() {
        m_contents = false;
    }
    
    /**
     * Disables the date created test.<p>
     */
    public void disableDateCreatedTest() {
        m_dateCreated = false;
    }
    
    /**
     * Disables the date expired test.<p>
     */
    public void disableDateExpiredTest() {
        m_dateExpired = false;
    }
    
    /**
     * Disables the date last modified test.<p>
     */
    public void disableDateLastModifiedTest() {
        m_dateLastModified = false;
    }
    
    /**
     * Disables the date released test.<p>
     */
    public void disableDateReleasedTest() {
        m_dateReleased = false;
    }
   
    /**
     * Disables the flags test.<p>
     */
    public void disableFlagsTest() {
        this.m_flags = false;
    }
    
    /**
     * Disables the length test.<p>
     */
    public void disableLengthTest() {
        this.m_length = false;
    }
    
    /**
     * Disables the lockstate test.<p>
     */
    public void disableLockTest() {
        m_lockstate = false;
    }
    
    /**
     * Disables the name test.<p>
     */
    public void disableNameTest() {
        m_name = false;
    }
    
    /**
     * Disables the project last modified test.<p>
     */
    public void disableProjectLastModifiedTest() {
        m_projectLastModified = false;
    }
    
    /**
     * Disables the properties test test.<p>
     */
    public void disablePropertiesTest() {
        m_properties = false;
    }
    
    /**
     * Disables the resource Id test.<p>
     */
    public void disableResourceIdTest() {
        m_resourceId = false;
    }
    
    /**
     * Disables the sibling count test.<p>
     */
    public void disableSiblingCountTest() {
        m_siblingCount = false;
    }
    
    /**
     * Disables the state test.<p>
     */
    public void disableStateTest() {
        this.m_state = false;
    }
    
    /**
     * Disables the structure Id test.<p>
     */
    public void disableStructureIdTest() {
        m_structureId = false;
    }
    
    /**
     * Disables the touched test.<p>
     */
    public void disableTouchedTest() {
        m_isTouched = false;
    }
    
    /**
     * Disables the type test.<p>
     */
    public void disableTypeTest() {
        m_type = false;
    }
    
    /**
     * Disables the user created test.
     */
    public void disableUserCreatedTest() {
        m_userCreated = false;
    }
    
    /**
     * Disables the user last modified test.<p>
     */
    public void disableUserLastModifiedTest() {
        m_userLastModified = false;
    }
}
