/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeFolder.java,v $
 * Date   : $Date: 2004/06/25 16:33:42 $
 * Version: $Revision: 1.2 $
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

package org.opencms.file.types;

import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsNotImplementedException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringSubstitution;

import java.util.List;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 */
public class CmsResourceTypeFolder extends A_CmsResourceType {

    /** The type id of this resource. */
    public static final int C_RESOURCE_TYPE_ID = 0;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "folder";

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource)
     */
    public void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException {

        // collect all resources in the folder (include deleted ones)
        List resources = driverManager.readChildResources(
            cms.getRequestContext(), 
            resource, 
            CmsResourceFilter.ALL, 
            true, 
            true); 

        // handle the folder itself
        super.changeLastModifiedProjectId(cms, driverManager, resource);

        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);    
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                changeLastModifiedProjectId(cms, driverManager, childResource);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).changeLastModifiedProjectId(cms, driverManager, childResource);
            }            
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, byte[], List)
     */
    public CmsResource createResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        byte[] content, 
        List properties
    ) throws CmsException {
        
        resourcename = validateFoldername(resourcename);
        return super.createResource(cms, driverManager, resourcename, content, properties);
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void chtype(CmsObject cms, CmsDriverManager driverManager, CmsResource filename, int newType) throws CmsException {
        
        // it is not possible to change the type of a folder
        throw new CmsNotImplementedException("Folder resource type can not be changed!");
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, java.lang.String, int)
     */
    public void copyResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource source, 
        String destination, 
        int siblingMode
    ) throws CmsException {

        // first validate the destination name
        destination = validateFoldername(destination);

        // collect all resources in the folder (but exclude deleted ones)
        List resources = driverManager.readChildResources(
            cms.getRequestContext(),            
            source,
            CmsResourceFilter.IGNORE_EXPIRATION, 
            true, 
            true); 
            
        // handle the folder itself
        super.copyResource(
            cms, 
            driverManager, 
            source, 
            destination, 
            siblingMode);
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);    
            String childDestination = destination.concat(childResource.getName());
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                copyResource(cms, driverManager, childResource, childDestination, siblingMode);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).copyResource(
                    cms, 
                    driverManager, 
                    childResource, 
                    childDestination, 
                    siblingMode);
            }            
        }        
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void deleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int siblingMode
    ) throws CmsException {

        // collect all resources in the folder (but exclude deleted ones)
        List resources = driverManager.readChildResources(
            cms.getRequestContext(),            
            resource,
            CmsResourceFilter.IGNORE_EXPIRATION, 
            true, 
            true); 
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                deleteResource(cms, driverManager, childResource, siblingMode);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).deleteResource(cms, driverManager, childResource, siblingMode);
            }            
        }  

        // handle the folder itself
        super.deleteResource(cms, driverManager, resource, siblingMode);
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        // folders have no loader
        return -1;
    }     

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {
        
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeName()
     */
    public String getTypeName() {
        
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int type, 
        byte[] content, 
        List properties
    ) throws CmsException {

        if (type != getTypeId()) {
            // it is not possible to change the type of a folder
            throw new CmsNotImplementedException("Folder resource type can not be replaced!");
        }
        // properties of a folder can be replaced, content is ignored
        super.replaceResource(cms, driverManager, resource, getTypeId(), null, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResourceBackup(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void restoreResourceBackup(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resourename, 
        int tag
    ) throws CmsException {

        throw new CmsNotImplementedException("[" + this.getClass().getName() + "] Cannot restore folders.");
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[ResourceType]:");
        output.append(getTypeName());
        output.append(", Id=");
        output.append(getTypeId());
        output.append(", LoaderId=");
        output.append(getLoaderId());
        return output.toString();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#touch(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, long, long, long, boolean)
     */
    public void touch(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource,  
        long dateLastModified, 
        long dateReleased, 
        long dateExpired, 
        boolean recursive
    ) throws CmsException {

        // handle the folder itself
        super.touch(
            cms, 
            driverManager, 
            resource, 
            dateLastModified, 
            dateReleased, 
            dateExpired, 
            recursive);
        
        if (recursive) {            
            // collect all resources in the folder (but exclude deleted ones)
            List resources = driverManager.readChildResources(
                cms.getRequestContext(),            
                resource,
                CmsResourceFilter.ALL, 
                true, 
                true); 
            
            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = (CmsResource)resources.get(i);
                if (childResource.isFolder()) {
                    // recurse into this method for subfolders
                    touch(
                        cms, 
                        driverManager, 
                        childResource, 
                        dateLastModified, 
                        dateReleased, 
                        dateExpired, 
                        recursive);
                } else {
                    // handle child resources
                    getResourceType(
                        childResource.getTypeId()
                    ).touch(
                        cms, 
                        driverManager, 
                        childResource, 
                        dateLastModified, 
                        dateReleased, 
                        dateExpired, 
                        recursive);
                }            
            }
        }       
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, boolean)
     */
    public void undoChanges(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource,
        boolean recursive
    ) throws CmsException {

        // handle the folder itself
        super.undoChanges(cms, driverManager, resource, recursive);
        
        if (recursive) {            
            // collect all resources in the folder (but exclude deleted ones)
            List resources = driverManager.readChildResources(
                cms.getRequestContext(),            
                resource,
                CmsResourceFilter.ALL, 
                true, 
                true); 
            
            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = (CmsResource)resources.get(i);
                if (childResource.isFolder()) {
                    // recurse into this method for subfolders
                    undoChanges(cms, driverManager, childResource, recursive);
                } else {
                    // handle child resources
                    getResourceType(
                        childResource.getTypeId()
                    ).undoChanges(cms, driverManager, childResource, recursive);
                }            
            }
        }     
    }
    
    /**
     * Checks if there are at least one character in the folder name,
     * also ensures that it starts and ends with a '/'.<p>
     *
     * @param resourcename folder name to check (complete path)
     * @return the validated folder name
     * @throws CmsException if the folder name is empty or null
     */
    private String validateFoldername(String resourcename) throws CmsException {

        if (CmsStringSubstitution.isEmpty(resourcename)) {
            throw new CmsException("[" + this.getClass().getName() + "] " + resourcename, CmsException.C_BAD_NAME);
        }
        if (! CmsResource.isFolder(resourcename)) {
            resourcename = resourcename.concat("/");
        }
        if (resourcename.charAt(0) != '/') {
            resourcename = "/".concat(resourcename);
        }
        return resourcename;        
    }
}