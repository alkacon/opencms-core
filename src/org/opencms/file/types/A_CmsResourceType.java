/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceType.java,v $
 * Date   : $Date: 2004/06/28 11:18:10 $
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Base implementation for resource type classes.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.1
 */
public abstract class A_CmsResourceType implements I_CmsResourceType {

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // this configuration does not support parameters 
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "addConfigurationParameter(" + paramName + ", " + paramValue + ") called on " + this);
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource)
     */
    public void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException {

        driverManager.changeLastModifiedProjectId(
            cms.getRequestContext(), 
            resource);
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLock(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource)
     */
    public void changeLock(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsResource resource
    ) throws CmsException {

        driverManager.changeLock(
            cms.getRequestContext(), 
            resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chflags(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void chflags(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int flags
    ) throws CmsException {

        driverManager.chflags(
            cms.getRequestContext(), 
            resource, 
            flags);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void chtype(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int type
    ) throws CmsException {

        driverManager.chtype(
            cms.getRequestContext(), 
            resource, 
            type);
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

        driverManager.copyResource(
            cms.getRequestContext(), 
            source, 
            cms.getRequestContext().addSiteRoot(destination), 
            siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(org.opencms.file.CmsObject, CmsDriverManager, CmsResource)
     */
    public void copyResourceToProject(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException {

        driverManager.copyResourceToProject(
            cms.getRequestContext(), 
            resource);
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

        return driverManager.createResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
            getTypeId(),
            content, 
            properties);
    }
 
    /**
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource, java.lang.String, java.util.List)
     */
     public void createSibling(
        CmsObject cms,
        CmsDriverManager driverManager,
        CmsResource source,
        String destination,
        List properties
     ) throws CmsException {

        driverManager.createSibling(
            cms.getRequestContext(), 
            source, 
            cms.getRequestContext().addSiteRoot(destination), 
            properties);
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

        driverManager.deleteResource(
            cms.getRequestContext(), 
            resource, 
            siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {

        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        // this configuration does not support parameters
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getConfiguration() called on " + this);
        }
        return null;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public abstract int getLoaderId();

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public abstract int getTypeId();

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeName()
     */
    public abstract String getTypeName();

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
        
        // this triggers the interal "is touched" state
        // and prevents the driver manager from inserting the current time
        resource.setDateLastModified(resource.getDateLastModified());
        // ensure resource record is updated
        resource.setState(I_CmsConstants.C_STATE_NEW);
        return driverManager.createResource(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename),
            resource,
            content,
            properties,
            true);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {
        
        // simple default configuration does not need to be initialized
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("initConfiguration() called on " + this);
            // supress compiler warning, this is never true
            if (this == null) {
                throw new CmsConfigurationException();
            }
        }            
    }    
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return false;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void lockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int mode
    ) throws CmsException {

        driverManager.lockResource(
            cms.getRequestContext(), 
            resource, 
            mode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, java.lang.String)
     */
    public void moveResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        String destination
    ) throws CmsException {
        
        // check if the user has write access and if resource is locked
        // done here since copy is ok without lock, but delete is not
        driverManager.checkPermissions(cms.getRequestContext(), resource, I_CmsConstants.C_WRITE_ACCESS, true, CmsResourceFilter.IGNORE_EXPIRATION);
        
        copyResource(
            cms, 
            driverManager, 
            resource, 
            destination, 
            I_CmsConstants.C_COPY_AS_SIBLING);
        
        deleteResource(
            cms, 
            driverManager, 
            resource, 
            I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
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
        
        driverManager.replaceResource(
            cms.getRequestContext(), 
            resource, 
            type, 
            content, 
            properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResourceBackup(org.opencms.file.CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void restoreResourceBackup(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int tag
    ) throws CmsException {

        driverManager.restoreResource(
            cms.getRequestContext(), 
            resource, 
            tag);
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

        driverManager.touch(
            cms.getRequestContext(), 
            resource, 
            dateLastModified, 
            dateReleased, 
            dateExpired);
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

        driverManager.undoChanges(
            cms.getRequestContext(),
            resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#unlockResource(org.opencms.file.CmsObject, CmsDriverManager, CmsResource)
     */
    public void unlockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException {

        driverManager.unlockResource(
            cms.getRequestContext(),
            resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsDriverManager, CmsFile)
     */
    public CmsFile writeFile(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsFile resource
    ) throws CmsException {

        if (resource.isFile()) {
            return driverManager.writeFile(cms.getRequestContext(), resource);
        }
        // folders can never be written like a file
        throw new CmsException("Attempt to write a folder as if it where a file!");        
    }
  
    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObject(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(
        CmsObject cms,
        CmsDriverManager driverManager,
        CmsResource resource,
        CmsProperty property
    ) throws CmsException {

        driverManager.writePropertyObject(
            cms.getRequestContext(), 
            resource, 
            property);
    }
    
    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObjects(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, CmsResource, java.util.List)
     */
    public void writePropertyObjects(
        CmsObject cms,
        CmsDriverManager driverManager,
        CmsResource resource, 
        List properties
        ) throws CmsException {
        
        driverManager.writePropertyObjects(
            cms.getRequestContext(),
            resource,
            properties);
    }     

    /**
     * Convenience method to return the initialized resource type 
     * instance for the given id.<p>
     * 
     * @param resourceType the id of the resource type to get
     * @return the initialized resource type instance for the given id
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    protected I_CmsResourceType getResourceType(int resourceType) throws CmsException {
        
        return OpenCms.getResourceManager().getResourceType(resourceType);
    }
    
}