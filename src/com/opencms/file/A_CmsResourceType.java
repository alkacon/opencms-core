/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsResourceType.java,v $
 * Date   : $Date: 2003/07/14 20:12:40 $
 * Version: $Revision: 1.7 $
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
import com.opencms.core.I_CmsConstants;

import java.util.Map;

/**
 * Base implementation for resource type classes.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
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
     * @see com.opencms.file.I_CmsResourceType#getLauncherClass()
     */
    public abstract String getLauncherClass();
    
    /**
     * @see com.opencms.file.I_CmsResourceType#getLauncherType()
     */
    public abstract int getLauncherType();

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
     * @see com.opencms.file.I_CmsResourceType#copyResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    public void copyResource(CmsObject cms, String resourcename, String destination, boolean keeppermissions) throws CmsException {
        cms.doCopyFile(resourcename, destination);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#moveResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void moveResource(CmsObject cms, String resourcename, String destination) throws CmsException {
        cms.doMoveResource(resourcename, destination);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#renameResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void renameResource(CmsObject cms, String resourcename, String destination) throws CmsException {
        cms.doRenameResource(resourcename, destination);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#deleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void deleteResource(CmsObject cms, String resourcename) throws CmsException {
        cms.doDeleteFile(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undeleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undeleteResource(CmsObject cms, String resourcename) throws CmsException {
        cms.doUndeleteFile(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#touch(com.opencms.file.CmsObject, java.lang.String, long, boolean)
     */
    public void touch(CmsObject cms, String resourcename, long timestamp, boolean recursive) throws CmsException {
        cms.doTouch(resourcename, timestamp);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#chtype(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void chtype(CmsObject cms, String resourcename, String newtype) throws CmsException {
        cms.doChtype(resourcename, newtype);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#replaceResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.String)
     */
    public void replaceResource(CmsObject cms, String resourcename, Map properties, byte[] content, String type) throws CmsException {
        // TODO: Move locking of resource to CmsObject or CmsDriverManager
        CmsResource res = cms.readFileHeader(resourcename, true);
        cms.doLockResource(cms.readAbsolutePath(res), true);
        cms.doReplaceResource(resourcename, content, type, properties);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undoChanges(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undoChanges(CmsObject cms, String resourcename) throws CmsException {
        // TODO: Move permission check to CmsObject or CmsDriverManager        
        if (!cms.hasPermissions(resourcename, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(resourcename, CmsException.C_NO_ACCESS);
        }
        cms.doUndoChanges(resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#restoreResource(com.opencms.file.CmsObject, int, java.lang.String)
     */
    public void restoreResource(CmsObject cms, int version, String resourcename) throws CmsException {
        // TODO: Move permission check in CmsObject or CmsDriverManager
        if (!cms.hasPermissions(resourcename, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(resourcename, CmsException.C_NO_ACCESS);
        }
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
     * @see com.opencms.file.I_CmsResourceType#importResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.Map, java.lang.String, byte[], java.lang.String)
     */
    public CmsResource importResource(CmsObject cms, String resourcename, String destination, String type, String user, String group, String access, long lastmodified, Map properties, String launcherStartClass, byte[] content, String importPath) throws CmsException {
        // TODO: Remove owner / group / access / launcherStartClass parameter
        CmsResource importedResource = null;
        destination = importPath + destination;
        boolean changed = true;

        int resourceType = cms.getResourceType(type).getResourceType();
        int launcherType = cms.getResourceType(type).getLauncherType();

        if ((launcherStartClass == null) || ("".equals(launcherStartClass))) {
            launcherStartClass = cms.getResourceType(type).getLauncherClass();
        }
        try {
            importedResource = cms.doImportResource(destination, resourceType, properties, launcherType, launcherStartClass, user, group, 0, lastmodified, content);
            if (importedResource != null) {
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the resource already exists
        }

        if (changed) {
            // if the resource already exists it must be updated
            lockResource(cms, destination, true);
            cms.doWriteResource(destination, properties, null, null, -1, resourceType, content);
            importedResource = cms.readFileHeader(destination);
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
        output.append(" , Id=");
        output.append(getResourceType());
        output.append(" , launcherType=");
        output.append(getLauncherType());
        output.append(" , launcherClass=");
        output.append(getLauncherClass());
        return output.toString();
    }
}
