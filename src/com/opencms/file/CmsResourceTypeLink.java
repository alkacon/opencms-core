/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeLink.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.10 $
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

import com.opencms.core.CmsException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Implementation of a resource type for links between resources in the virtual 
 * file system (VFS). A VFS link is nothing else but a text
 * file of a certain resource type. The content of this file is the name/path
 * of the target resource of the link, including the site root (which is at least
 * "/default/vfs/").
 * <p>
 * VFS links and their target resources are tracked by the RESOURCE_FLAGS table
 * attribute. Each VFS link saves there the ID of it's target resource. Each 
 * resource that has VFS links saves the count of it's VFS links (to fix
 * wheter it has VFS links at all or not).
 * <p>
 * All resource types are created by the factory getResourceType() in CmsObject.
 *
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.10 $
 */
public class CmsResourceTypeLink extends CmsResourceTypePlain {

    /**
     * The name of the resource type node in registry.xml to read the
     * general configurations for VFS links.
     */
    public static final String C_TYPE_RESOURCE_NAME = "link";
    private static final int DEBUG = 0;
    
    /**
     * Create a new VFS link.
     * 
     * @param cms the current user's CmsObject instance
     * @param folder the folder of the new VFS link resource
     * @param name the name of the new VFS link resource
     * @param properties a Hashtable with the properties of the new VFS link resource
     * @param contents a byte array with the content of the new VFS link resource, which is it's target resource name, including the site root
     * @return the new VFS link resource
     * @throws CmsException
     */
    public CmsResource createResource(CmsObject cms, String newResourceName, Map properties, byte[] contents, Object parameter) throws CmsException{
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
                    cms.createPropertydefinition( currentProperty, CmsResourceTypeLink.C_TYPE_RESOURCE_NAME );
                }
                
                // write the target property on the link
                if (DEBUG>0) System.out.println( "writing property " + currentProperty + " with value " + (String)targetProperties.get(currentProperty) );
                cms.writeProperty( newResourceName, currentProperty, (String)targetProperties.get(currentProperty) );
            }
        }        
        
		// update the link management
		String targetResourceName = new String(contents);
		cms.linkResourceToTarget(newResourceName, targetResourceName);
		cms.doIncrementLinkCountForResource(targetResourceName);

        return res;
    }

    /**
     * Delete a VFS link. The link counter of the target resource is decremented.
     * 
     * @param cms the current user's CmsObject instance
     * @param theResourceName the resource name of the link
     * @throws CmsException
     */
	public void deleteResource(CmsObject cms, String theResourceName) throws CmsException {
        String targetResourceName = new String(cms.readFile(theResourceName).getContents());        
		super.deleteResource(cms, theResourceName);

		// update the link management
		cms.doDecrementLinkCountForResource(targetResourceName);
}    
    /**
     * Undelete a VFS link. The link counter of the target resource is incremented again.
     * 
     * @param cms the current user's CmsObject instance
     * @param theResourceName the resource name of the link
     * @throws CmsException
     */    
    public void undeleteResource(CmsObject cms, String theResourceName) throws CmsException{   
        super.undeleteResource(cms, theResourceName);
        String targetResourceName = new String(cms.readFile(theResourceName).getContents());
        
        // update the link management
        cms.doIncrementLinkCountForResource(targetResourceName);
        cms.linkResourceToTarget(theResourceName, targetResourceName);        
    }    

    /**
     * Copy a VFS link. The link counter of the target resource is incremented. 
     * The ID of the target resource is saved in the new link resource.
     * 
     * @param cms the current user's CmsObject instance
     * @param theSourceResourceName the source resource name of the link
     * @param theDestinationResourceName the destination resource name of the link
     * @param keepFlags boolean whether the copy should use the file flags of the source or the user's default file flags
     * @throws CmsException
     */
	public void copyResource(CmsObject cms, String theSourceResourceName, String theDestinationResourceName, boolean keepFlags) throws CmsException {        
		super.copyResource(cms, theSourceResourceName, theDestinationResourceName, keepFlags);

		// update the link management
		String targetResourceName = new String(cms.readFile(theDestinationResourceName).getContents());
		cms.doIncrementLinkCountForResource(targetResourceName);
        cms.linkResourceToTarget(theDestinationResourceName, targetResourceName);
	}

    /**
     * Move a VFS link. The link counter of the target resource remains unchanged.
     * The ID of the target resource is saved in the new link resource.
     * 
     * @param cms the current user's CmsObject instance
     * @param theSourceResourceName the source resource name of the link
     * @param theDestinationResourceName the destination resource name of the link
     * @throws CmsException
     */
	public void moveResource(CmsObject cms, String theSourceResourceName, String theDestinationResourceName) throws CmsException {        
		super.moveResource(cms, theSourceResourceName, theDestinationResourceName);

		// update the link management
		String targetResourceName = new String(cms.readFile(theDestinationResourceName).getContents());
		cms.linkResourceToTarget(theDestinationResourceName, targetResourceName);
	}

    /**
     * Rename a VFS link. The link counter of the target resource remains unchanged.
     * The ID of the target resource is saved in the new link resource.
     * 
     * @param cms the current user's CmsObject instance
     * @param theOldResourceName the old resource name of the link
     * @param theNewResourceName the new resource name of the link
     * @throws CmsException
     */
	public void renameResource(CmsObject cms, String theOldResourceName, String theNewResourceName) throws CmsException {        
		super.renameResource(cms, theOldResourceName, theNewResourceName);

		// update the link management
        String folder = theOldResourceName.substring(0, theOldResourceName.lastIndexOf("/") + 1);
        theNewResourceName = folder + theNewResourceName;
		String targetResourceName = new String(cms.readFile(theNewResourceName).getContents());
		cms.linkResourceToTarget(theNewResourceName, targetResourceName);
	}

    /**
     * Undo the changes on a VFS link. In case the target of the link has to be changed
     * back to another target resource, the link counter of the new target resource
     * is decremented, and so the link counter of the previous target resource is
     * incremented again. The ID of the target resource is saved in the new link resource.
     * 
     * @param cms the current user's CmsObject instance
     * @param theResourceName the resource name of the link
     * @throws CmsException
     */
	public void undoChanges(CmsObject cms, String theResourceName) throws CmsException {       
		String oldTargetResourceName = new String(cms.readFile(theResourceName).getContents());
		super.undoChanges(cms, theResourceName);
		String newTargetResourceName = new String(cms.readFile(theResourceName).getContents());

		// update the link management
		if (!oldTargetResourceName.equals(newTargetResourceName)) {
			cms.doDecrementLinkCountForResource(oldTargetResourceName);
			cms.doIncrementLinkCountForResource(newTargetResourceName);
			cms.linkResourceToTarget(theResourceName, newTargetResourceName);
		}
	}
        
}