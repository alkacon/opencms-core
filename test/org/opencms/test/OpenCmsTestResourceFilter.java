/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceFilter.java,v $
 * Date   : $Date: 2004/06/21 10:01:50 $
 * Version: $Revision: 1.10 $
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
 
package org.opencms.test;

import org.opencms.file.CmsProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Filter class for OpenCms CmsObject method tests. This object defines the attributes
 * of a CmsResource object which must not be changed after a method call in the CmsObject.
 * The defualt setting for all flags in the filter is "true", i.e. if an attribute should
 * be tested to a new, specified value, the equal test must be disabled in the filter.<p>
 * 
 *  @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.10 $
 */
public class OpenCmsTestResourceFilter {


    /** Defintition of a filter used for the chacc method. */
    public static OpenCmsTestResourceFilter FILTER_CHACC = getFilterChacc();
    
    /** Defintition of a equal filter. */
    public static OpenCmsTestResourceFilter FILTER_EQUAL = new OpenCmsTestResourceFilter();
    
    /** Defintition of a filter used to validate the existing and the new sibling after a copy opreation. */
    public static OpenCmsTestResourceFilter FILTER_EXISTING_AND_NEW_SIBLING = getFilterExistingAndNewSibling();     
    
    /** Defintition of a filter used to validate an existing sibling after a copy operation. */
    public static OpenCmsTestResourceFilter FILTER_EXISTING_SIBLING = getFilterExistingSibling();
    
    /** Defintition of a filter used for the touch method. */
    public static OpenCmsTestResourceFilter FILTER_TOUCH = getFilterTouch();
    
    /** Defintition of a filter used for the undoChanges method. */
    public static OpenCmsTestResourceFilter FILTER_UNDOCHANGES = getFilterUndoChanges();
    
    /** Defintition of a filter used for the writeProperty method. */   
    public static OpenCmsTestResourceFilter FILTER_WRITEPROPERTY = getFilterWriteProperty();
   
    /** Flags for validating the attributes of two CmsResources. */
    private boolean m_access;
    private boolean m_contentId;
    private boolean m_contents;
    private boolean m_dateCreated;
    private boolean m_dateExpired;    
    private boolean m_dateLastModified;
    private boolean m_dateReleased;
    private boolean m_flags;
    private boolean m_isTouched;
    private boolean m_length;
    private boolean m_loaderId;
    private boolean m_lockstate; 
    private boolean m_name;
    private boolean m_parentId;
    private boolean m_projectLastModified;
    private boolean m_properties;
    private boolean m_resourceId;
    private boolean m_siblingCount;
    private boolean m_state;
    private boolean m_structureId;
    private boolean m_type;
    private boolean m_userCreated;
    private boolean m_userLastModified;
    
    /**
     * Creates a new OpenCmsTestResourceFilter.<p>
     */
    public OpenCmsTestResourceFilter() {
        m_contentId = true;
        m_dateCreated = true;
        m_dateLastModified = true;
        m_dateReleased = true;
        m_dateExpired = true;   
        m_flags = true;
        m_isTouched = true;
        m_length = true;
        m_siblingCount = true;
        m_loaderId = true;
        m_name = true;
        m_parentId = true;
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
        m_access = true;
    }
    
    /**
     * Compares two lists of properties and returns those 
     * that are included only in the source but not in the targer list and not
     * part of a seperade exclude list.<p>
     * 
     * @param source the source properties
     * @param target the target properties
     * @param exclude the exclude list
     * @return list of not matching properties
     */    
    public static List compareProperties(List source, List target, List exclude) {
        
        List result = new ArrayList();
        List targetClone = new ArrayList(target);
        Iterator i = source.iterator();
        while (i.hasNext()) {
            boolean found = false;
            CmsProperty sourceProperty = (CmsProperty) i.next();
            Iterator j = targetClone.iterator();
            CmsProperty targetProperty = null;
            while (j.hasNext()) {
                targetProperty = (CmsProperty) j.next();
                if (sourceProperty.isIdentical(targetProperty)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(sourceProperty);
            } else {
                targetClone.remove(targetProperty);
            }
        }
        
        // finally match the result list with the exclude list
        if (exclude != null) {
            Iterator l = exclude.iterator();
            while (l.hasNext()) {
                CmsProperty excludeProperty = (CmsProperty) l.next();   
                if (result.contains(excludeProperty)) {
                    result.remove(excludeProperty);
                }
            }
        }        
        
        return result;
    }            
    
    
    /**
     * Creates a new filter used for the "chacc" method.
     * different from the existing sibling(s) from which it was created.<p>
     * 
     * @return OpenCmsTestResourceFilter filter
     */
    private static OpenCmsTestResourceFilter getFilterChacc() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();

        filter.disableStateTest();
        filter.disableProjectLastModifiedTest();
        filter.disableAccessTest();

        return filter;        
    }
    
    /**
     * Creates a new filter used to validate the fields of a new sibling
     * different from the existing sibling(s) from which it was created.<p>
     * 
     * @return OpenCmsTestResourceFilter filter
     */
    private static OpenCmsTestResourceFilter getFilterExistingAndNewSibling() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();

        filter.disableStateTest();
        filter.disableStructureIdTest();
        filter.disableNameTest();
        filter.disableLockTest();

        return filter;        
    }
    
    /**
     * Creates a new filter used to validate the modified fields of an
     * existing resource from which a new sibling was created.<p>
     * 
     * @return OpenCmsTestResourceFilter filter
     */
    private static OpenCmsTestResourceFilter getFilterExistingSibling() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableSiblingCountTest();
        filter.disableLockTest();

        return filter;        
    }
    
    /**
     * Creates a new filter used for the "touch" method.<p>
     * @return OpenCmsTestResourceFilter object
     */
    private static OpenCmsTestResourceFilter getFilterTouch() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        return filter; 

    }
    
    /**
     * Creates a new filter used for the "undoChanges" method.<p>
     * @return OpenCmsTestResourceFilter object
     */
    private static OpenCmsTestResourceFilter getFilterUndoChanges() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();
        filter.disableProjectLastModifiedTest();
        return filter; 

    }
    
    
    /**
     * Creates a new filter used for the "touch" method.<p>
     * @return OpenCmsTestResourceFilter object
     */
    private static OpenCmsTestResourceFilter getFilterWriteProperty() {
        OpenCmsTestResourceFilter filter = new OpenCmsTestResourceFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        filter.disablePropertiesTest();
        return filter;
    }
    
    
    /**
     * Disables the access list test.<p>
     */
    public void disableAccessTest() {
        m_access = false;
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
     * Disables the loader Id test.<p>
     */
    public void disableLoaderIdTest() {
        m_loaderId = false;
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
     * Disables the parent Id test.<p>
     */
    public void disableParentIdTest() {
        m_parentId = false;
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
    
    /**
     * Returns true if the access list test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testAccess() {
        return m_access;
    }
        
    /**
     * Returns true if the Content Id test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testContentId() {
        return m_contentId;
    }
    
    
    /**
     * Returns true if the Contents test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testContents() {
        return m_contents;
    }
    
    /**
     * Returns true if the date created test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testDateCreated() {
        return m_dateCreated;
    }
    
    /**
     * Returns true if the date expired test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testDateExpired() {
        return m_dateExpired;
    }
    
    /**
     * Returns true if the date last modified test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testDateLastModified() {
        return m_dateLastModified;
    }
    
    /**
     * Returns true if the date released test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testDateReleased() {
        return m_dateReleased;
    }
    
    /**
     * Returns true if the flags test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testFlags() {
        return m_flags;
    }
    
    /**
     * Returns true if the length test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testLength() {
        return m_length;
    }
    
    /**
     * Returns true if the loader Id test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testLoaderId() {
        return m_loaderId;
    }
    
    /**
     * Returns true if the lockstate test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testLock() {
        return m_lockstate;
    }
    
    /**
     * Returns true if the name test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testName() {
        return m_name;
    }
    
    /**
     * Returns true if the parent Id test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testParentId() {
        return m_parentId;
    }
    
    /**
     * Returns true if the project last modified test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testProjectLastModified() {
        return m_projectLastModified;
    }
    
    /**
     * Returns true if the properties test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testProperties() {
        return m_properties;
    }
    
    /**
     * Returns true if the resource Id test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testResourceId() {
        return m_resourceId;
    }
    
    /**
     * Returns true if the sibling count test is enabled..<p>
     *
     * @return true or false
     */
    public boolean testSiblingCount() {
        return m_siblingCount;
    }
    
    /**
     * Returns true if the state test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testState() {
        return m_state;
    }
    
    /**
     * Returns true if the structure Id test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testStructureId() {
        return m_structureId;
    }
  
    /**
     * Returns true if the touched test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testTouched() {
        return m_isTouched;
    }
    
    /**
     * Returns true if the type test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testType() {
        return m_type;
    }
    
    /**
     * Returns true if the user created test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testUserCreated() {
        return m_userCreated;
    }
    
    /**
     * Returns true if the user last modified test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testUserLastModified() {
        return m_userLastModified;
    }
}
