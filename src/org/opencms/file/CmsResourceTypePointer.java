/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsResourceTypePointer.java,v $
 * Date   : $Date: 2004/02/27 14:26:40 $
 * Version: $Revision: 1.3 $
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

package org.opencms.file;

import org.opencms.loader.CmsPointerLoader;
import org.opencms.main.CmsException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Implementation of a resource type for external links
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsResourceTypePointer extends A_CmsResourceType {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 99;

    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "pointer";

    /** DEBUG flag */
    private static final int DEBUG = 0;

    /**
     * @see org.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsPointerLoader.C_RESOURCE_LOADER_ID;
    } 
    
    /**
     * @see org.opencms.file.I_CmsResourceType#createResource(org.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        HashMap targetProperties = null;
        Vector linkPropertyDefs = null;

        // create the new link
        CmsResource res = cms.doCreateFile(resourcename, contents, getResourceTypeName(), properties);
        contents = null;

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
                    if (DEBUG > 0) {
                        System.out.println("adding property definition " + currentProperty + " for resource type " + getResourceTypeName());
                    }
                    cms.createPropertydefinition(currentProperty, getResourceType());
                }

                // write the target property on the link
                if (DEBUG > 0) {
                    System.out.println("writing property " + currentProperty + " with value " + (String)targetProperties.get(currentProperty));
                }
                cms.writeProperty(resourcename, currentProperty, (String)targetProperties.get(currentProperty));
            }
        }

        return res;
    }    
}