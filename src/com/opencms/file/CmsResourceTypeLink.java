/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeLink.java,v $
* Date   : $Date: 2002/10/23 15:12:46 $
* Version: $Revision: 1.5 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.core.*;
import java.util.*;

/**
 * This class describes the resource-type link.
 *
 * @author
 * @version 1.0
 */

public class CmsResourceTypeLink extends CmsResourceTypePlain {

    public static final String C_TYPE_RESOURCE_NAME = "link";
    private static final int DEBUG = 0;

    public CmsResource createResource(CmsObject cms, String newResourceName, Hashtable properties, byte[] contents, Object parameter) throws CmsException{
        HashMap targetProperties = null;
        Vector linkPropertyDefs = null;
                
        // create the new link
        CmsResource res = cms.doCreateFile(newResourceName, contents, C_TYPE_RESOURCE_NAME, properties);
        
        // lock the new file
        cms.lockResource(newResourceName);
        
        if (parameter!=null) {
            targetProperties = (HashMap)parameter;
            
            // read all existing properties defined for links
            Vector propertyDefs = cms.readAllPropertydefinitions( CmsResourceTypeLink.C_TYPE_RESOURCE_NAME );
            Enumeration allPropertyDefs = propertyDefs.elements();
            linkPropertyDefs = new Vector( propertyDefs.size() );
                        
            while (allPropertyDefs.hasMoreElements()) {
                CmsPropertydefinition currentPropertyDefinition = (CmsPropertydefinition)allPropertyDefs.nextElement();
                linkPropertyDefs.add( (String)currentPropertyDefinition.getName() );
            }
            
            // copy all properties of the target to the link
            Iterator i = targetProperties.keySet().iterator();
            while (i.hasNext()) {
                String currentProperty = (String)i.next();
                
                if (!linkPropertyDefs.contains((String)currentProperty)) {
                    // add the property definition if the property is not yet defined for links
                    if (DEBUG>0) System.out.println( "adding property definition " + currentProperty + " for resource type " + CmsResourceTypeLink.C_TYPE_RESOURCE_NAME );
                    CmsPropertydefinition newPropertyDef = cms.createPropertydefinition( currentProperty, CmsResourceTypeLink.C_TYPE_RESOURCE_NAME );
                }
                
                // write the target property on the link
                if (DEBUG>0) System.out.println( "writing property " + currentProperty + " with value " + (String)targetProperties.get(currentProperty) );
                cms.writeProperty( newResourceName, currentProperty, (String)targetProperties.get(currentProperty) );
            }
        }        
        
        return res;
    }
}