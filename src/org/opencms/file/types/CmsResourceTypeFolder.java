/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeFolder.java,v $
 * Date   : $Date: 2004/06/21 09:55:50 $
 * Version: $Revision: 1.1 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class CmsResourceTypeFolder extends A_CmsResourceType {

    /** The type id of this resource. */
    public static final int C_RESOURCE_TYPE_ID = 0;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "folder";

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, java.lang.String)
     */
    public void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException {

        // first validate the resource name
        resourcename = validateFoldername(resourcename);

        // collect all resources in the folder (include deleted ones)
        List resources = cms.getResourcesInFolder(resourcename, CmsResourceFilter.ALL);

        // handle the folder itself
        super.changeLastModifiedProjectId(cms, driverManager, resourcename);

        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);    
            String name = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            if (res.isFolder()) {
                // recurse into this method for subfolders
                changeLastModifiedProjectId(cms, driverManager, name);
            } else {
                // handle other resources using the CmsObject method
                cms.changeLastModifiedProjectId(name);
            }            
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void chtype(CmsObject cms, CmsDriverManager driverManager, String filename, int newType) throws CmsException {
        
        // it is not possible to change the type of a folder
        throw new CmsNotImplementedException("Folder resource type can not be changed!");
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, java.lang.String, int)
     */
    public void copyResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String source, 
        String destination, 
        int siblingMode
    ) throws CmsException {

        // first validate the destination and source name
        source = validateFoldername(source);
        destination = validateFoldername(destination);

        // collect all resources in the folder (but exclude deleted ones)
        List resources = cms.getResourcesInFolder(source, CmsResourceFilter.IGNORE_EXPIRATION);
        
        // handle the folder itself
        super.copyResource(cms, driverManager, source, destination, siblingMode);
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);    
            String sourceName = cms.readAbsolutePath(res, CmsResourceFilter.IGNORE_EXPIRATION);
            String targetName = destination + CmsResource.getName(sourceName);
            if (res.isFolder()) {
                // recurse into this method for subfolders
                copyResource(cms, driverManager, sourceName, targetName, siblingMode);
            } else {
                // handle other resources using the CmsObject method
                cms.copyResource(sourceName, targetName, siblingMode);
            }            
        }        
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void deleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int siblingMode
    ) throws CmsException {

        // first validate the resource name
        resourcename = validateFoldername(resourcename);

        // collect all resources in the folder (but exclude deleted ones)
        List resources = cms.getResourcesInFolder(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);    
            String name = cms.readAbsolutePath(res, CmsResourceFilter.IGNORE_EXPIRATION);
            if (res.isFolder()) {
                // recurse into this method for subfolders
                deleteResource(cms, driverManager, name, siblingMode);
            } else {
                // handle other resources using the CmsObject method
                cms.deleteResource(name, siblingMode);
            }            
        }  

        // handle the folder itself
        super.deleteResource(cms, driverManager, resourcename, siblingMode);
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
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, org.opencms.file.CmsResource, byte[], List)
     */
    public CmsResource importResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        CmsResource resource, 
        byte[] content, 
        List properties
    ) throws CmsException {
        
        resourcename = validateFoldername(resourcename);

        CmsResource importedResource = null;
        CmsProperty property = null, oldProperty = null;
        int found = 0;
        
        boolean changed = true;
        //try to import the resource
        try {            
            importedResource = driverManager.importResource(
                cms.getRequestContext(), 
                cms.getRequestContext().addSiteRoot(resourcename), 
                resource, 
                null, 
                properties);            
            
            if (importedResource != null) {
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the folder already exists
        }
        
        if (changed) {
            changed = false;
            // the resource did already exist, check if properties have to be updated
            importedResource = cms.readFolder(resourcename);
            String importedResourcename = cms.readAbsolutePath(importedResource);
            List oldProperties = cms.readPropertyObjects(importedResourcename, false);
            if (oldProperties == null) {
                oldProperties = Collections.EMPTY_LIST;
            }
            if (properties == null) {
                properties = Collections.EMPTY_LIST;
            }
            
            // find the delta between imported and existing properties
            if (properties.size() > 0) {
                if (oldProperties.size() != properties.size()) {
                    changed = true;
                } else {
                    for (int i = 0, n = properties.size(); i < n; i++) {
                        found = -1;
                        property = (CmsProperty)properties.get(i);

                        if ((found = oldProperties.indexOf(property)) == -1
                            || (oldProperty = (CmsProperty)oldProperties.get(found)) == null) {
                            changed = true;
                            break;
                        }
                        
                        if (!oldProperty.isIdentical(property)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            
            // check changes of the resourcetype
            if (importedResource.getTypeId() != getTypeId()) {
                changed = true;
            }
            
            // update the folder if something has changed
            if (changed) {                
                lockResource(cms, driverManager, importedResourcename, CmsLock.C_MODE_COMMON);
                
                driverManager.importResourceUpdate(
                    cms.getRequestContext(), 
                    cms.getRequestContext().addSiteRoot(importedResourcename), 
                    null, 
                    properties);
            } 
        }
        // get the updated folder
        return importedResource;
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveToLostAndFound(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, boolean)
     */
    public String moveToLostAndFound(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean copyResource
    ) {

        // nothing to do here, folders are never moved to the "lost and found" folder
        return null;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int, byte[], List)
     */
    public void replaceResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int type, 
        byte[] content, 
        List properties
    ) throws CmsException {

        if (type != getTypeId()) {
            // it is not possible to change the type of a folder
            throw new CmsNotImplementedException("Folder resource type can not be replaced!");
        }
        // properties of a folder can be replaced, content is ignored
        super.replaceResource(cms, driverManager, resourcename, getTypeId(), null, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void restoreResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourename, 
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
     * @see org.opencms.file.types.I_CmsResourceType#touch(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, long, long, long, org.opencms.util.CmsUUID, boolean)
     */
    public void touch(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename,  
        long dateLastModified, 
        long dateReleased, 
        long dateExpired, 
        CmsUUID user, 
        boolean recursive
    ) throws CmsException {

        // first validate the resource name
        resourcename = validateFoldername(resourcename);

        // collect all resources in the folder (but exclude deleted ones)
        List resources = cms.getResourcesInFolder(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);

        // handle the folder itself
        super.touch(
            cms, 
            driverManager, 
            resourcename, 
            dateLastModified, 
            dateReleased, 
            dateExpired, 
            user, 
            recursive);
        
        if (! recursive) {
            // if no recurse mode operation is finished
            return;
        }
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);    
            String name = cms.readAbsolutePath(res, CmsResourceFilter.IGNORE_EXPIRATION);
            if (res.isFolder()) {
                // recurse into this method for subfolders
                touch(
                    cms, 
                    driverManager, 
                    name, 
                    dateLastModified, 
                    dateReleased, 
                    dateExpired, 
                    user, 
                    recursive);
            } else {
                // handle other resources using the CmsObject method
                cms.touch(
                    name, 
                    dateLastModified, 
                    dateReleased, 
                    dateExpired, 
                    user, 
                    recursive);
            }            
        }  
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undeleteResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String)
     */
    public void undeleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException {
        
        // first validate the resource name
        resourcename = validateFoldername(resourcename);
        
        // handle the folder itself
        super.undeleteResource(cms, driverManager, resourcename);
        
        // collect all resources in the folder (but exclude deleted ones)
        List resources = cms.getResourcesInFolder(resourcename, CmsResourceFilter.ALL);
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                String name = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
                if (res.isFolder()) {
                    // recurse into this method for subfolders
                    undeleteResource(cms, driverManager, name);
                } else {
                    // handle other resources using the CmsObject method
                    cms.undeleteResource(name);
                }            
            }
        }          
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, boolean)
     */
    public void undoChanges(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename,
        boolean recursive
    ) throws CmsException {

        // first validate the resource name
        resourcename = validateFoldername(resourcename);
        
        // handle the folder itself
        super.undoChanges(cms, driverManager, resourcename, recursive);
        
        // collect all resources in the folder (but exclude deleted ones)
        List resources = cms.getResourcesInFolder(resourcename, CmsResourceFilter.ALL);
        
        if (! recursive) {
            // if no recurse mode operation is finished
            return;
        }
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);
            String name = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            if (res.isFolder()) {
                // recurse into this method for subfolders
                undoChanges(cms, driverManager, name, recursive);
            } else {
                // handle other resources using the CmsObject method
                cms.undoChanges(name, recursive);
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