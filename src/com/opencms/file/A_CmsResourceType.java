/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsResourceType.java,v $
 * Date   : $Date: 2003/09/12 10:01:54 $
 * Version: $Revision: 1.43 $
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

import com.opencms.core.CmsException;
import com.opencms.flex.util.CmsUUID;

import java.util.Map;

/**
 * Base implementation for resource type classes.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.43 $
 * @since 5.1
 */
public abstract class A_CmsResourceType implements I_CmsResourceType {

    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceTypeName()
     */
    public abstract String getResourceTypeName();

    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceType()
     */
    public abstract int getResourceType();
    
    /**
     * @see com.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public abstract int getLoaderId();

    /**
     * @see com.opencms.file.I_CmsResourceType#createResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public abstract CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException;

    /**
     * @see com.opencms.file.I_CmsResourceType#lockResource(com.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void lockResource(CmsObject cms, String resourcename, boolean force) throws CmsException {
        cms.doLockResource(resourcename, force);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#unlockResource(com.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void unlockResource(CmsObject cms, String resourcename, boolean recursive) throws CmsException {
        cms.doUnlockResource(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean, boolean, int)
     */
    public void copyResource(CmsObject cms, String resourcename, String destination, boolean keeppermissions, boolean lockCopy, int copyMode) throws CmsException {
        cms.doCopyFile(resourcename, destination, lockCopy, copyMode);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#moveResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void moveResource(CmsObject cms, String resourcename, String destination) throws CmsException {
        cms.doMoveResource(resourcename, destination);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyToLostAndFound(com.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public String copyToLostAndFound(CmsObject cms, String resourcename, boolean copyResource) throws CmsException {
        return cms.doCopyToLostAndFound(resourcename, copyResource);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#renameResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void renameResource(CmsObject cms, String resourcename, String destination) throws CmsException {
        cms.doRenameResource(resourcename, destination);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#deleteResource(com.opencms.file.CmsObject, java.lang.String, int)
     */
    public void deleteResource(CmsObject cms, String resourcename, int deleteOption) throws CmsException {
        cms.doDeleteFile(resourcename, deleteOption);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undeleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undeleteResource(CmsObject cms, String resourcename) throws CmsException {
        cms.doUndeleteFile(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#touch(com.opencms.file.CmsObject, java.lang.String, long, boolean, com.opencms.flex.util.CmsUUID)
     */
    public void touch(CmsObject cms, String resourcename, long timestamp, boolean recursive, CmsUUID user) throws CmsException {
        cms.doTouch(resourcename, timestamp, user);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#chtype(com.opencms.file.CmsObject, java.lang.String, int)
     */
    public void chtype(CmsObject cms, String resourcename, int newtype) throws CmsException {
        cms.doChtype(resourcename, newtype);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#replaceResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], int)
     */
    public void replaceResource(CmsObject cms, String resourcename, Map properties, byte[] content, int type) throws CmsException {
        cms.doReplaceResource(resourcename, content, type, properties);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undoChanges(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undoChanges(CmsObject cms, String resourcename) throws CmsException {
        cms.doUndoChanges(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#restoreResource(com.opencms.file.CmsObject, int, java.lang.String)
     */
    public void restoreResource(CmsObject cms, int version, String resourcename) throws CmsException {
        cms.doRestoreResource(version, resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyResourceToProject(com.opencms.file.CmsObject, java.lang.String)
     */
    public void copyResourceToProject(CmsObject cms, String resourcename) throws CmsException {
        cms.doCopyResourceToProject(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#changeLockedInProject(com.opencms.file.CmsObject, int, java.lang.String)
     */
    public void changeLockedInProject(CmsObject cms, int project, String resourcename) throws CmsException {
        cms.doChangeLockedInProject(project, resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#exportResource(com.opencms.file.CmsObject, com.opencms.file.CmsFile)
     */
    public CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException {
        // there are no link tags inside a simple resource
        return file;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#importResource(com.opencms.file.CmsObject, com.opencms.file.CmsResource, byte[], java.util.Map, java.lang.String)
     */
    public CmsResource importResource(CmsObject cms, CmsResource resource, byte[] content, Map properties, String destination) throws CmsException {
        CmsResource importedResource = null;

        boolean changed = true;

        try {
            importedResource = cms.doImportResource(resource, content, properties, destination);
            cms.lockResource(destination);
            changed = (importedResource == null);
        } catch (CmsException e) {
            // an exception is thrown if the resource already exists
        }

        if (changed) { 
            // the resource already exists
            // check if the resource uuids are the same, if so, update the content
            CmsResource existingResource = cms.readFileHeader(destination);
            if (existingResource.getResourceId().equals(resource.getResourceId())) {
                // resource with the same name and same uuid does exist,
                // update the existing resource
                cms.lockResource(destination);
                cms.doWriteResource(destination, properties, null, getResourceType(), content);
                importedResource = cms.readFileHeader(destination);
                cms.touch(destination, resource.getDateLastModified(), false, resource.getUserLastModified());
            } else {
                // a resource with the same name but different uuid does exist,
                // copy the new resource to the lost&found folder 
                String target = copyToLostAndFound(cms, destination, false);                             
                CmsResource newRes = new CmsResource(resource.getId(), resource.getResourceId(), resource.getParentId(), resource.getFileId(),  CmsResource.getName(target), resource.getType(), resource.getFlags(), resource.getProjectId(), resource.getState(), resource.getLoaderId(), resource.getDateLastModified(), resource.getUserLastModified(), resource.getDateCreated(), resource.getUserCreated(), resource.getLength(), 1);                        
                importedResource = cms.doImportResource(newRes, content, properties, target);
            }       
        }

        return importedResource;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[ResourceType]:");
        output.append(getResourceTypeName());
        output.append(", Id=");
        output.append(getResourceType());
        output.append(", LoaderId=");
        output.append(getLoaderId());
        return output.toString();
    }
}
