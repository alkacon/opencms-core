/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishList.java,v $
 * Date   : $Date: 2004/11/22 08:50:28 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A container for all new/changed/deteled Cms resources of a project or a direct published 
 * resource (and optionally it's siblings) that actually get published.<p>
 * 
 * Only classes inside the org.opencms.db package can add or remove elements to or from this list. 
 * This allows the Cms app to pass the list around between classes, but with restricted access to 
 * create this list.<p>
 * 
 * {@link org.opencms.db.CmsDriverManager#getPublishList(org.opencms.file.CmsRequestContext, CmsResource, boolean)}
 * creates Cms publish lists.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.15 $ $Date: 2004/11/22 08:50:28 $
 * @since 5.3.0
 * @see org.opencms.db.CmsDriverManager#getPublishList(org.opencms.file.CmsRequestContext, CmsResource, boolean)
 */
public class CmsPublishList extends Object {

    /** The resource to publish in case of direct publishing. */
    private CmsResource m_publishResource;
    
    /** The list of new/changed/deleted Cms file resources to be published.<p> */
    private List m_fileList;

    /** The list of new/changed Cms folder resources to be published.<p> */
    private List m_folderList;
    
    /** The list of deleted Cms folder resources to be published.<p> */
    private List m_deletedFolderList;

    /** The publish history ID.<p> */
    private CmsUUID m_publishHistoryId;

    /**
     * Constructs an empty publish list for the resources of a project to be published.<p>
     */
    public CmsPublishList() {
        this(null);
    }

    /**
     * Constructs an empty publish list with additional information for a direct published resource.<p>
     * @param directPublishResource a Cms resource to be published directly
     */
    public CmsPublishList(CmsResource directPublishResource) {
        m_fileList = new ArrayList();
        m_folderList = new ArrayList();
        m_deletedFolderList = new ArrayList();
        m_publishResource = directPublishResource;

        m_publishHistoryId = new CmsUUID();
    }

    /**
     * Adds a new/changed/deleted Cms file resource to the publish list.<p>
     * 
     * @param resource a new/changed/deleted Cms file resource
     * @throws IllegalArgumentException if the specified resource is not a file or unchanged
     */
    protected void addFile(CmsResource resource) throws IllegalArgumentException {
        // it is essential that this method is only visible within the db package!

        if (resource.isFolder()) {
            throw new IllegalArgumentException("Cms resource '" + resource.getRootPath() + "' is not a Cms file resource!");
        }

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            throw new IllegalArgumentException("Cms resource '" + resource.getRootPath() + "' is a unchanged resource!");
        }

        m_fileList.add(resource);
    }

    /**
     * Appends all of the new/changed/deleted Cms file resources in the specified list to the end 
     * of this publish list.<p>
     * 
     * @param list a list with new/changed/deleted Cms file resources to be added to this publish list
     * @throws IllegalArgumentException if one of the resources is not a file or unchanged
     */
    protected void addFiles(List list) throws IllegalArgumentException {
        // it is essential that this method is only visible within the db package!

        Iterator i = list.iterator();
        while (i.hasNext()) {
            addFile((CmsResource) i.next());
        }
    }

    /**
     * Adds a new/changed Cms folder resource to the publish list.<p>
     * 
     * @param resource a new/changed Cms folder resource
     * @throws IllegalArgumentException if the specified resource is not a folder or unchanged
     */
    protected void addFolder(CmsResource resource) throws IllegalArgumentException {
        // it is essential that this method is only visible within the db package!

        if (resource.isFile()) {
            throw new IllegalArgumentException("Cms resource '" + resource.getRootPath() + "' is not a Cms folder resource!");
        }

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            throw new IllegalArgumentException("Cms resource '" + resource.getRootPath() + "' is a unchanged resource!");
        }
        
        if (resource.getState() == I_CmsConstants.C_STATE_DELETED) {
            m_deletedFolderList.add(resource);
        } else {
            m_folderList.add(resource);
        }
    }

    /**
     * Appends all of the new/changed Cms folder resources in the specified list to the end 
     * of this publish list.<p>
     * 
     * @param list a list with new/changed Cms folder resources to be added to this publish list
     * @throws IllegalArgumentException if one of the resources is not a folder or unchanged
     */
    protected void addFolders(List list) throws IllegalArgumentException {
        // it is essential that this method is only visible within the db package!

        Iterator i = list.iterator();
        while (i.hasNext()) {
            addFolder((CmsResource) i.next());
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_fileList != null) {
                m_fileList.clear();
            }
            m_fileList = null;

            if (m_folderList != null) {
                m_folderList.clear();
            }
            m_folderList = null;
        } catch (Throwable t) {
            // ignore
        }

        super.finalize();
    }

    /**
     * Returns the list of deleted folders resources in reversed order.<p>
     * 
     * @return the list of deleted folders
     */
    public List getDeletedFolderList() {
        
        Collections.sort(m_deletedFolderList, Collections.reverseOrder());
        return m_deletedFolderList;
    }
    
    /**
     * Returns the resource that should be published or null.<p>
     * 
     * @return the resource that should be published or null
     */
    public CmsResource getDirectPublishResource() {
        return m_publishResource;
    }

    /**
     * Returns an unmodifiable list of the Cms file resources in this publish list.<p>
     * 
     * @return the list with the Cms file resources in this publish list
     */
    public List getFileList() {
        return Collections.unmodifiableList(m_fileList);
    }

    /**
     * Returns an unmodifiable list of the new/changed Cms folder resources in this publish list.<p>
     * 
     * @return the list with the new/changed Cms file resources in this publish list
     */
    public List getFolderList() {
        return Collections.unmodifiableList(m_folderList);
    }

    /**
     * Returns the list with the new/changed Cms folder resources.<p>
     * 
     * @return the list with the new/changed Cms folder resources
     */
    protected List getFolderListInstance() {
        // it is essential that this method is only visible within the db package!
        return m_folderList;
    }

    /**
     * Returns the publish history Id for this publish list.<p>
     * 
     * @return the publish history Id
     */
    public CmsUUID getPublishHistoryId() {
        return m_publishHistoryId;
    }

    /**
     * Checks if this is a publish list for a direct published file *OR* folder.<p>
     * 
     * @return true if this is a publish list for a direct published file *OR* folder
     * @see #isDirectPublishFile()
     */
    public boolean isDirectPublish() {
        return m_publishResource != null;
    }

    /**
     * Checks if this is a publish list for a direct published file.<p>
     * 
     * @return true if this is a publish list for a direct published file
     * @see #isDirectPublish()
     */
    public boolean isDirectPublishFile() {
        return m_publishResource.isFile();
    }

    /**
     * Removes a Cms resource from the publish list.<p>
     * 
     * @param resource a Cms resource
     * @return true if this publish list contains the specified resource
     * @see List#remove(java.lang.Object)
     */
    protected boolean remove(CmsResource resource) {
        // it is essential that this method is only visible within the db package!
        return m_fileList.remove(resource);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        
        strBuf.append("\n[\n");
        strBuf.append("direct publish file or folder: ").append((m_publishResource != null) ? m_publishResource.getRootPath() : "-").append("\n");
        strBuf.append("publish history ID: ").append(m_publishHistoryId.toString()).append("\n");
        strBuf.append("resources: ").append(m_fileList.toString()).append("\n");
        strBuf.append("folders: ").append(m_folderList.toString()).append("\n");
        strBuf.append("]\n");
        
        return strBuf.toString();
    }

}
