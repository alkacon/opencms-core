/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceType.java,v $
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Base implementation for resource type classes.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
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
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, org.opencms.db.CmsDriverManager, java.lang.String)
     */
    public void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException {

        driverManager.changeLastModifiedProjectId(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void chtype(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int newType
    ) throws CmsException {

        driverManager.chtype(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), newType);
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

        driverManager.copyResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(source), 
            cms.getRequestContext().addSiteRoot(destination), 
            siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String)
     */
    public void copyResourceToProject(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException {

        driverManager.copyResourceToProject(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename));
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
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void deleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int siblingMode
    ) throws CmsException {

        driverManager.deleteResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
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

        CmsResource importedResource = null;
        CmsResource existingResource = null;

        try {
            importedResource = driverManager.importResource(
                cms.getRequestContext(), 
                cms.getRequestContext().addSiteRoot(resourcename), 
                resource, 
                content, 
                properties);

            cms.lockResource(resourcename);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                // read the existing resource                
                existingResource = cms.readFileHeader(resourcename);

                // check if the resource uuids are the same, if so, update the content
                if (OpenCms.getImportExportManager().overwriteCollidingResources()
                    || existingResource.getResourceId().equals(resource.getResourceId())) {
                    // resource with the same name and same uuid does exist, 
                    // update the existing resource
                    cms.lockResource(resourcename);
                    
                    driverManager.importResourceUpdate(
                        cms.getRequestContext(), 
                        cms.getRequestContext().addSiteRoot(resourcename), 
                        content, 
                        properties);

                    importedResource = cms.readFileHeader(resourcename);
                    cms.touch(
                        resourcename,
                        resource.getDateLastModified(),
                        I_CmsConstants.C_DATE_UNCHANGED,
                        I_CmsConstants.C_DATE_UNCHANGED,
                        resource.getUserLastModified(),
                        false);
                } else {
                    // a resource with the same name but different uuid does exist,
                    // copy the new resource to the lost+found folder 
                    String newResourcename = moveToLostAndFound(cms, driverManager, resourcename, false);
                    CmsResource newResource = new CmsResource(
                        resource.getStructureId(),
                        resource.getResourceId(),
                        resource.getParentStructureId(),
                        resource.getFileId(),
                        CmsResource.getName(newResourcename),
                        resource.getTypeId(),
                        resource.getFlags(),
                        resource.getProjectLastModified(),
                        resource.getState(),
                        resource.getLoaderId(),
                        resource.getDateLastModified(),
                        resource.getUserLastModified(),
                        resource.getDateCreated(),
                        resource.getUserCreated(),
                        resource.getDateReleased(),
                        resource.getDateExpired(),
                        1,
                        resource.getLength());
                    
                    importedResource = driverManager.importResource(
                        cms.getRequestContext(), 
                        cms.getRequestContext().addSiteRoot(newResourcename), 
                        newResource, 
                        content, 
                        properties);
                }
            }
        }

        return importedResource;
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
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void lockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int mode
    ) throws CmsException {

        driverManager.lockResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
            mode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, java.lang.String)
     */
    public void moveResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String source, 
        String destination
    ) throws CmsException {
        
        copyResource(cms, driverManager, source, destination, I_CmsConstants.C_COPY_AS_SIBLING);
        deleteResource(cms, driverManager, source, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveToLostAndFound(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, boolean)
     */
    public String moveToLostAndFound(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean returnNameOnly
    ) throws CmsException {

        return driverManager.moveToLostAndFound(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
            returnNameOnly);
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
        
        driverManager.replaceResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
            type, 
            content, 
            properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, int)
     */
    public void restoreResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        int tag
    ) throws CmsException {

        driverManager.restoreResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
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

        driverManager.touch(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename), 
            dateLastModified, 
            dateReleased, 
            dateExpired, 
            user);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undeleteResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String)
     */
    public void undeleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename
    ) throws CmsException {

        driverManager.undeleteResource(
            cms.getRequestContext(), 
            cms.getRequestContext().addSiteRoot(resourcename));
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

        driverManager.undoChanges(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#unlockResource(org.opencms.file.CmsObject, CmsDriverManager, java.lang.String, boolean)
     */
    public void unlockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        boolean recursive
    ) throws CmsException {

        driverManager.unlockResource(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename));
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
}