/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceTypeFolderBase.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsDataNotImplementedException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.10 $
 */
public abstract class A_CmsResourceTypeFolderBase extends A_CmsResourceType {

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public A_CmsResourceTypeFolderBase() {
        
        super();
    } 
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resource
    ) throws CmsException {

        // collect all resources in the folder (include deleted ones)
        List resources = securityManager.readChildResources(
            cms.getRequestContext(), 
            resource, 
            CmsResourceFilter.ALL, 
            true, 
            true); 

        // handle the folder itself
        super.changeLastModifiedProjectId(cms, securityManager, resource);

        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);    
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                changeLastModifiedProjectId(cms, securityManager, childResource);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).changeLastModifiedProjectId(cms, securityManager, childResource);
            }            
        }
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource filename, int newType) throws CmsException, CmsDataNotImplementedException {
        
        if (! OpenCms.getResourceManager().getResourceType(newType).isFolder()) {
            // it is not possible to change the type of a folder to a file type
            throw new CmsDataNotImplementedException(Messages.get().container(Messages.ERR_CHTYPE_FOLDER_1, cms.getSitePath(filename)));
        }
        super.chtype(cms, securityManager, filename, newType);         
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, int)
     */
    public void copyResource(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource source, 
        String destination, 
        int siblingMode
    ) throws CmsIllegalArgumentException, CmsException {

        // first validate the destination name
        destination = validateFoldername(destination);

        // collect all resources in the folder (but exclude deleted ones)
        List resources = securityManager.readChildResources(
            cms.getRequestContext(),            
            source,
            CmsResourceFilter.IGNORE_EXPIRATION, 
            true, 
            true); 
            
        // handle the folder itself
        super.copyResource(
            cms, 
            securityManager, 
            source, 
            destination, 
            siblingMode);
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);    
            String childDestination = destination.concat(childResource.getName());
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                copyResource(cms, securityManager, childResource, childDestination, siblingMode);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).copyResource(
                    cms, 
                    securityManager, 
                    childResource, 
                    childDestination, 
                    siblingMode);
            }            
        }        
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, byte[], List)
     */
    public CmsResource createResource(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        String resourcename,
        byte[] content, 
        List properties
    ) throws CmsException {
        
        resourcename = validateFoldername(resourcename);
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resource, 
        int siblingMode
    ) throws CmsException {

        // collect all resources in the folder (but exclude deleted ones)
        List resources = securityManager.readChildResources(
            cms.getRequestContext(),
            resource,
            CmsResourceFilter.IGNORE_EXPIRATION,
            true,
            true); 
        
        Set deletedResources = new HashSet();
        
        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = (CmsResource)resources.get(i);
            
            if (siblingMode == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS && deletedResources.contains(childResource.getResourceId())) {
                // sibling mode is "delete all siblings" and another sibling of the current child resource has already
                // been deleted- do nothing and continue with the next child resource.
                continue;
            }
            
            if (childResource.isFolder()) {
                // recurse into this method for subfolders
                deleteResource(cms, securityManager, childResource, siblingMode);
            } else {
                // handle child resources
                getResourceType(
                    childResource.getTypeId()
                ).deleteResource(cms, securityManager, childResource, siblingMode);
            }  
            
            deletedResources.add(childResource.getResourceId());
        }  
        
        deletedResources.clear();

        // handle the folder itself
        super.deleteResource(cms, securityManager, resource, siblingMode);
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        // folders have no loader
        return -1;
    }
    
    /**
     * @see org.opencms.file.types.A_CmsResourceType#isFolder()
     */
    public boolean isFolder() {

        return true;
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String)
     */
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException {
        
        String dest = cms.getRequestContext().addSiteRoot(destination);
        if (!CmsResource.isFolder(dest)) {
            // ensure folder name end's with a / (required for the following comparison)
            dest = dest.concat("/");
        }      
        if (resource.getRootPath().equals(dest)) {
            // move to target with same name is not allowed
            throw new CmsVfsException(org.opencms.file.Messages.get().container(org.opencms.file.Messages.ERR_MOVE_SAME_NAME_1, destination));
        }   
        if (dest.startsWith(resource.getRootPath())) {
            // move of folder inside itself is not allowed
            throw new CmsVfsException(org.opencms.file.Messages.get().container(org.opencms.file.Messages.ERR_MOVE_SAME_FOLDER_2,
                cms.getSitePath(resource),
                destination));
        }

        // check if the user has write access and if resource is locked
        // done here since copy is ok without lock, but delete is not
        securityManager.checkPermissions(
            cms.getRequestContext(),
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            true,
            CmsResourceFilter.IGNORE_EXPIRATION);

        copyResource(cms, securityManager, resource, destination, I_CmsConstants.C_COPY_AS_SIBLING);

        deleteResource(cms, securityManager, resource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
    }    

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resource, 
        int type, 
        byte[] content, 
        List properties
    ) throws CmsException, CmsDataNotImplementedException {

        if (type != getTypeId()) {
            // it is not possible to replace a folder with a different type
            throw new CmsDataNotImplementedException(Messages.get().container(Messages.ERR_REPLACE_RESOURCE_FOLDER_1, cms.getSitePath(resource)));
        }
        // properties of a folder can be replaced, content is ignored
        super.replaceResource(cms, securityManager, resource, getTypeId(), null, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResourceBackup(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResourceBackup(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resourename, 
        int tag
    ) throws CmsException {

        // it is not possible to restore a folder from the backup
        throw new CmsDataNotImplementedException(Messages.get().container(Messages.ERR_RESTORE_FOLDERS_0));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#touch(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, long, long, boolean)
     */
    public void touch(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resource,  
        long dateLastModified, 
        long dateReleased, 
        long dateExpired, 
        boolean recursive
    ) throws CmsException {

        // handle the folder itself
        super.touch(
            cms, 
            securityManager, 
            resource, 
            dateLastModified, 
            dateReleased, 
            dateExpired, 
            recursive);
        
        if (recursive) {            
            // collect all resources in the folder (but exclude deleted ones)
            List resources = securityManager.readChildResources(
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
                    touch(
                        cms, 
                        securityManager, 
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
                        securityManager, 
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
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undoChanges(
        CmsObject cms, 
        CmsSecurityManager securityManager, 
        CmsResource resource,
        boolean recursive
    ) throws CmsException {

        // handle the folder itself
        super.undoChanges(cms, securityManager, resource, recursive);
        
        if (recursive) {            
            // collect all resources in the folder (but exclude deleted ones)
            List resources = securityManager.readChildResources(
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
                    undoChanges(cms, securityManager, childResource, recursive);
                } else {
                    // handle child resources
                    getResourceType(
                        childResource.getTypeId()
                    ).undoChanges(cms, securityManager, childResource, recursive);
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
     * @throws CmsIllegalArgumentException if the folder name is empty or null
     */
    private String validateFoldername(String resourcename) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmpty(resourcename)) {
            throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_BAD_RESOURCENAME_1, resourcename)); 
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