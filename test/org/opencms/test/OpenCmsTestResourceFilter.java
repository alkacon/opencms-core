/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceFilter.java,v $
 * Date   : $Date: 2004/08/20 11:44:49 $
 * Version: $Revision: 1.15 $
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
 * Base filter class for OpenCms VFS access method tests.<p>
 * 
 * A filter defines the attributes of a CmsResource which 
 * must not be changed after a method call in the CmsObject.<p>
 * 
 * To use a filter, either use the static filters provided by this class,
 * or create an instance of {@link org.opencms.test.OpenCmsTestResourceConfigurableFilter}.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.15 $
 */
public abstract class OpenCmsTestResourceFilter {

    /** Definition of a filter used for the chacc method. */
    public static final OpenCmsTestResourceFilter FILTER_CHACC = getFilterChacc();
    
    /** Definition of a filter used for the create resource method. */
    public static final OpenCmsTestResourceFilter FILTER_CREATE_RESOURCE = getFilterCreateResource();    
    
    /** Definition of a equal filter. */
    public static final OpenCmsTestResourceFilter FILTER_EQUAL = new OpenCmsTestResourceConfigurableFilter();
    
    /** Definition of a filter used to validate the existing and the new sibling after a copy opreation. */
    public static final OpenCmsTestResourceFilter FILTER_EXISTING_AND_NEW_SIBLING = getFilterExistingAndNewSibling();     
    
    /** Definition of a filter used to validate an existing sibling after a copy operation. */
    public static final OpenCmsTestResourceFilter FILTER_EXISTING_SIBLING = getFilterExistingSibling();
    
    /** Definition of a filter used for the touch method. */
    public static final OpenCmsTestResourceFilter FILTER_TOUCH = getFilterTouch();
    
    /** Definition of a filter used for the copy method. */
    public static final OpenCmsTestResourceFilter FILTER_COPY_AS_NEW = getFilterCopyAsNew();    
    
    /** Definition of a filter used for the copy method. */
    public static final OpenCmsTestResourceFilter FILTER_COPY_SOURCE_DESTINATION_AS_SIBLING = getFilterCopySourceDestinationAsSibling();  
    
    /** Definition of a filter used for the move/reanme method. */
    public static final OpenCmsTestResourceFilter FILTER_MOVE_DESTINATION = getFilterMoveDestination();  
    
    /** Definition of a filter used for the move/reanme method. */
    public static final OpenCmsTestResourceFilter FILTER_MOVE_SOURCE = getFilterMoveSource();  
        
    /** Definition of a filter used for the undoChanges method. */
    public static final OpenCmsTestResourceFilter FILTER_UNDOCHANGES = getFilterUndoChanges();
    
    /** Definition of a filter used for the writeProperty method. */   
    public static final OpenCmsTestResourceFilter FILTER_WRITEPROPERTY = getFilterWriteProperty();
  
    /** Definition of a filter used for the publsihResource method. */   
    public static final OpenCmsTestResourceFilter FILTER_PUBLISHRESOURCE = getFilterPublishResource();
 
    /** Definition of a filter used for the replaceResource method. */
    public static final OpenCmsTestResourceFilter FILTER_REPLACERESOURCE = getFilterReplaceResource();
    
    /** Flag to enable/disable access (ACL) tests. */
    protected boolean m_acl;

    /** Flag to enable/disable access (ACE) tests. */
    protected boolean m_ace;
    
    /** Flag to enable/disable content id tests. */
    protected boolean m_contentId;

    /** Flag to enable/disable content comparison tests. */
    protected boolean m_contents;

    /** Flag to enable/disable date created tests. */
    protected boolean m_dateCreated;

    /** Flag to enable/disable date expired tests. */
    protected boolean m_dateExpired;    

    /** Flag to enable/disable date last modified tests. */
    protected boolean m_dateLastModified;

    /** Flag to enable/disable date released tests. */
    protected boolean m_dateReleased;

    /** Flag to enable/disable flags tests. */
    protected boolean m_flags;

    /** Flag to enable/disable "is touched" tests. */
    protected boolean m_isTouched;

    /** Flag to enable/disable length tests. */
    protected boolean m_length;

    /** Flag to enable/disable lock state tests. */
    protected boolean m_lockstate; 

    /** Flag to enable/disable name tests. */
    protected boolean m_name;

    /** Flag to enable/disable project last modified tests. */
    protected boolean m_projectLastModified;

    /** Flag to enable/disable properties tests. */
    protected boolean m_properties;

    /** Flag to enable/disable resource id tests. */
    protected boolean m_resourceId;

    /** Flag to enable/disable sibling count tests. */
    protected boolean m_siblingCount;

    /** Flag to enable/disable state tests. */
    protected boolean m_state;

    /** Flag to enable/disable structure id tests. */
    protected boolean m_structureId;

    /** Flag to enable/disable resource type tests. */
    protected boolean m_type;

    /** Flag to enable/disable user created tests. */
    protected boolean m_userCreated;

    /** Flag to enable/disable use last modified tests. */
    protected boolean m_userLastModified;
   
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
     * Creates a new filter used for the "chacc" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterChacc() {
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableStateTest();
        filter.disableProjectLastModifiedTest();
        filter.disableAclTest();
        filter.disableAceTest();

        return filter;        
    }
    
    /**
     * Creates a new filter used for the "create resource" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterCreateResource() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        filter.disableContentsTest();
        filter.disableLengthTest();
        filter.disableLockTest();
        return filter; 
    }
    
    /**
     * Creates a new filter used to validate the fields of a new sibling
     * different from the existing sibling(s) from which it was created.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterExistingAndNewSibling() {
        
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

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
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterExistingSibling() {
       
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableSiblingCountTest();
        filter.disableLockTest();

        return filter;        
    }
    
    /**
     * Creates a new filter used for the "touch" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterTouch() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        return filter; 
    }
    
    /**
     * Creates a new filter used for the "copy as new" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterCopyAsNew() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableStructureIdTest();
        filter.disableContentIdTest();
        filter.disableResourceIdTest();
        filter.disableUserCreatedTest();
        filter.disableDateCreatedTest();
        filter.disableLockTest();
        filter.disableNameTest();
        filter.disableAceTest();
        return filter; 
    }    
    
    /**
     * Creates a new filter used for the "copy as sibling" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterCopySourceDestinationAsSibling() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableStructureIdTest();
        filter.disableLockTest();
        filter.disableNameTest();
        return filter; 
    }   
    
    /**
     * Creates a new filter used for the "move/rename" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterMoveDestination() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableStructureIdTest();
        filter.disableLockTest();
        filter.disableNameTest();
        filter.disableSiblingCountTest();
        return filter; 
    }
    
    /**
     * Creates a new filter used for the "move/rename" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceConfigurableFilter getFilterMoveSource() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableLockTest();
        filter.disableStateTest();
        filter.disablePropertiesTest();
        filter.disableSiblingCountTest();
        return filter; 
    }    
    
    /**
     * Creates a new filter used for the "undoChanges" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceFilter getFilterUndoChanges() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableLockTest();
        return filter; 
    }
    
    
    /**
     * Creates a new filter used for the "write property" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceFilter getFilterWriteProperty() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableProjectLastModifiedTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        filter.disablePropertiesTest();
        return filter;
    }

    /**
     * Creates a new filter used for the "publishResource" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceFilter getFilterPublishResource() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableLockTest();
        filter.disableStateTest();
        filter.disableSiblingCountTest();
        return filter;
    }
    
    /**
     * Creates a new filter used for the "replaceResource" method.<p>
     * 
     * @return the created filter
     */
    private static OpenCmsTestResourceFilter getFilterReplaceResource() {

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();

        filter.disableLockTest();
        filter.disableStateTest();
        filter.disableDateLastModifiedTest();
        filter.disableUserLastModifiedTest();
        filter.disableContentsTest();
        filter.disableLengthTest();
        filter.disableProjectLastModifiedTest();
        return filter;
    }
    
    /**
     * Returns true if the acl test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testAcl() {
        return m_acl;
    }
    
    /**
     * Returns true if the ace test is enabled.<p>
     *
     * @return true or false
     */
    public boolean testAce() {
        return m_ace;
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
