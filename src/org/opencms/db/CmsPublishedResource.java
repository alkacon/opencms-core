/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishedResource.java,v $
 * Date   : $Date: 2004/08/27 08:57:22 $
 * Version: $Revision: 1.17 $
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

package org.opencms.db;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * Represents the state of a published resource *before* it got published.<p>
 * 
 * This allows various subsequent tasks in the Cms app. (e.g. exporting files and folders)
 * to identify published resources after a resource or project was published.<p>
 * 
 * The values to fill this container are read from the Cms publish history database table 
 * that is written during each publishing process.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.17 $ $Date: 2004/08/27 08:57:22 $
 * @since 5.1.11
 * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(int, CmsUUID)
 */
public class CmsPublishedResource extends Object implements Serializable, Cloneable {
    
    /** The backup tag ID of the published resource. */
    private int m_backupTagId;

    /** The resource ID of the published resource.<p> */
    private CmsUUID m_resourceId;

    /** The state of the resource *before* it was published.<p> */
    private int m_resourceState;

    /** The type of the published resource.<p> */
    private int m_resourceType;

    /** The root path of the published resource.<p> */
    private String m_rootPath;

    /** The count of siblings of the published resource. */
    private int m_siblingCount;

    /** The structure ID of the published resource.<p> */
    private CmsUUID m_structureId;

    /**
     * Creates an object for published VFS resources.<p>
     * 
     * @param structureId the structure ID of the published resource
     * @param resourceId the resource ID of the published resource
     * @param backupTagId the resource's tag ID in the backup tables
     * @param rootPath the root path of the published resource
     * @param resourceType the type of the published resource
     * @param resourceState the state of the resource *before* it was published
     * @param siblingCount count of siblings of the published resource
     */
    public CmsPublishedResource(CmsUUID structureId, CmsUUID resourceId, int backupTagId, String rootPath, int resourceType, int resourceState, int siblingCount) {
        m_structureId = structureId;
        m_resourceId = resourceId;
        m_backupTagId = backupTagId;
        m_rootPath = rootPath;
        m_resourceType = resourceType;
        m_resourceState = resourceState;
        m_siblingCount = siblingCount;
    }
    
    /**
     * Creates an object for published VFS resources.<p>
     * 
     * @param resource an CmsResource object to create a CmsPublishedResource from
     */
    public CmsPublishedResource(CmsResource resource) {
        m_structureId = resource.getStructureId();
        m_resourceId = resource.getResourceId();
        m_backupTagId = -1;
        m_rootPath = resource.getRootPath();
        m_resourceType = resource.getTypeId();
        m_resourceState = resource.getState();
        m_siblingCount = resource.getSiblingCount();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CmsPublishedResource)) {
            return false;
        }

        return getStructureId().equals(((CmsPublishedResource) obj).getStructureId());
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            m_structureId = null;
            m_resourceId = null;
            m_rootPath = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }
    
    /**
     * Returns the backup tag ID of the published resource.<p>
     * 
     * @return the backup tag ID of the published resource
     */
    public int getBackupTagId() {
        return m_backupTagId;
    }

    /**
     * Returns the count of siblings of the published resource.<p>
     * 
     * @return the count of siblings of the published resource
     */
    public int getSiblingCount() {
        return m_siblingCount;
    } 

    /**
     * Returns the resource ID of the published resource.<p>
     * 
     * @return the resource ID of the published resource
     */
    public CmsUUID getResourceId() {
        return m_resourceId;
    }

    /**
     * Returns the root path of the published resource.<p>
     * 
     * @return the root path of the published resource
     */
    public String getRootPath() {
        return m_rootPath;
    }

    /**
     * Returns the resource state of the published resource.<p>
     * 
     * @return the resource state of the published resource
     */
    public int getState() {
        return m_resourceState;
    }

    /**
     * Returns the structure ID of the published resource.<p>
     * 
     * @return the structure ID of the published resource
     */
    public CmsUUID getStructureId() {
        return m_structureId;
    }

    /**
     * Returns the resource type of the published resource.<p>
     * 
     * @return the resource type of the published resource
     */
    public int getType() {
        return m_resourceType;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_structureId.hashCode();
    }

    /**
     * Checks if the resource is changed.<p>
     * 
     * @return true if the resource is changed
     */
    public boolean isChanged() {
        return getState() == I_CmsConstants.C_STATE_CHANGED;
    }

    /**
     * Checks if the resource is deleted.<p>
     * 
     * @return true if the resource is deleted
     */
    public boolean isDeleted() {
        return getState() == I_CmsConstants.C_STATE_DELETED;
    }

    /**
     * Determines if this resource is a file.<p>
     * 
     * @return true if this resource is a file, false otherwise
     */
    public boolean isFile() {
        return getType() != CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }

    /**
     * Checks if this resource is a folder.<p>
     * 
     * @return true if this is is a folder
     */
    public boolean isFolder() {
        return getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;
    }

    /**
     * Checks if the resource is new.<p>
     * 
     * @return true if the resource is new
     */
    public boolean isNew() {
        return getState() == I_CmsConstants.C_STATE_NEW;
    }

    /**
     * Checks if the resource is unchanged.<p>
     * 
     * @return true if the resource is unchanged
     */
    public boolean isUnChanged() {
        return getState() == I_CmsConstants.C_STATE_UNCHANGED;
    }
    
    /**
     * Checks if this published resource represents a VFS resource.<p>
     * 
     * If the published resource has no structure id, it is considered to be 
     * no VFS resource.<p>
     * 
     * @return true if this published resource is a VFS resource
     */
    public boolean isVfsResource() {
        return !getStructureId().equals(CmsUUID.getNullUUID());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String objInfo = "[" + this.getClass().getName() + ": ";

        objInfo += "root path: " + m_rootPath + ", ";
        objInfo += "structure ID: " + m_structureId + ", ";
        objInfo += "resource ID: " + m_resourceId + ", ";
        objInfo += "backup tag ID: " + m_backupTagId + ", ";
        objInfo += "siblings: " + m_siblingCount + ", ";
        objInfo += "state: " + m_resourceState + ", ";
        objInfo += "type: " + m_resourceType + "]";

        return objInfo;
    }
}
