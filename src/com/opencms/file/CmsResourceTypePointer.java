/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypePointer.java,v $
 * Date   : $Date: 2003/10/02 12:10:21 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.file;

import org.opencms.loader.CmsPointerLoader;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Implementation of a resource type for external links
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.9 $
 */
public class CmsResourceTypePointer extends A_CmsResourceType {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 99;

    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "pointer";

    /** DEBUG flag */
    private static final int DEBUG = 0;

    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see com.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsPointerLoader.C_RESOURCE_LOADER_ID;
    } 
    
    /**
     * @see com.opencms.file.I_CmsResourceType#createResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        HashMap targetProperties = null;
        Vector linkPropertyDefs = null;

        // create the new link
        CmsResource res = cms.doCreateFile(resourcename, contents, getResourceTypeName(), properties);

        // lock the new file
        cms.lockResource(resourcename);

        if (parameter != null) {
            targetProperties = (HashMap)parameter;

            // read all existing properties defined for links
            Vector propertyDefs = cms.readAllPropertydefinitions(getResourceTypeName());
            Enumeration allPropertyDefs = propertyDefs.elements();
            linkPropertyDefs = new Vector(propertyDefs.size());

            while (allPropertyDefs.hasMoreElements()) {
                CmsPropertydefinition currentPropertyDefinition = (CmsPropertydefinition)allPropertyDefs.nextElement();
                linkPropertyDefs.add(currentPropertyDefinition.getName());
            }

            // copy all properties of the target to the link
            Iterator i = targetProperties.keySet().iterator();
            while (i.hasNext()) {
                String currentProperty = (String)i.next();

                if (!linkPropertyDefs.contains(currentProperty)) {
                    // add the property definition if the property is not yet defined for links
                    if (DEBUG > 0)
                        System.out.println("adding property definition " + currentProperty + " for resource type " + getResourceTypeName());
                    cms.createPropertydefinition(currentProperty, getResourceType());
                }

                // write the target property on the link
                if (DEBUG > 0)
                    System.out.println("writing property " + currentProperty + " with value " + (String)targetProperties.get(currentProperty));
                cms.writeProperty(resourcename, currentProperty, (String)targetProperties.get(currentProperty));
            }
        }

        return res;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#deleteResource(com.opencms.file.CmsObject, java.lang.String, int)
     */
    public void deleteResource(CmsObject cms, String resourcename, int deleteOption) throws CmsException {
        super.deleteResource(cms, resourcename, I_CmsConstants.C_DELETE_OPTION_IGNORE_VFS_LINKS);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undeleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undeleteResource(CmsObject cms, String resourcename) throws CmsException {
        super.undeleteResource(cms, resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean, boolean, int)
     */
    public void copyResource(CmsObject cms, String theSourceResourceName, String theDestinationResourceName, boolean keepFlags, boolean lockCopy, int copyMode) throws CmsException {
        super.copyResource(cms, theSourceResourceName, theDestinationResourceName, keepFlags, lockCopy, copyMode);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#moveResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void moveResource(CmsObject cms, String theSourceResourceName, String theDestinationResourceName) throws CmsException {
        super.moveResource(cms, theSourceResourceName, theDestinationResourceName);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#renameResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void renameResource(CmsObject cms, String theOldResourceName, String theNewResourceName) throws CmsException {
        super.renameResource(cms, theOldResourceName, theNewResourceName);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undoChanges(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undoChanges(CmsObject cms, String theResourceName) throws CmsException {
        super.undoChanges(cms, theResourceName);
    }
    
}