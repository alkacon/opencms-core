/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishList.java,v $
 * Date   : $Date: 2005/10/25 18:38:50 $
 * Version: $Revision: 1.24.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A container for all new/changed/deteled Cms resources that are published together.<p>
 * 
 * Only classes inside the org.opencms.db package can add or remove elements to or from this list. 
 * This allows the OpenCms API to pass the list around between classes, but with restricted access to 
 * create this list.<p>
 * 
 * To create a publish list, one of the public constructors must be used in order to set the basic operation mode
 * (project publish or direct publish).
 * After this, use <code>{@link org.opencms.db.CmsDriverManager#fillPublishList(CmsDbContext, CmsPublishList)}</code>
 * to fill the actual values of the publish list.<p>
 * 
 * @author Alexander Kandzior
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.24.2.1 $
 * 
 * @since 6.0.0
 * 
 * @see org.opencms.db.CmsDriverManager#fillPublishList(CmsDbContext, CmsPublishList)
 */
public class CmsPublishList {

    /** The list of deleted Cms folder resources to be published.<p> */
    private List m_deletedFolderList;

    /** The list of direct publish resources. */
    private List m_directPublishResources;

    /** The list of new/changed/deleted Cms file resources to be published.<p> */
    private List m_fileList;

    /** The list of new/changed Cms folder resources to be published.<p> */
    private List m_folderList;

    /** The id of the project that is to be published. */
    private int m_projectId;

    /** The publish history ID.<p> */
    private CmsUUID m_publishHistoryId;

    /** Indicates if siblings of the resources in the list should also be published. */
    private boolean m_publishSiblings;

    /**
     * Constructs a publish list for a given project.<p>
     * 
     * @param project the project to publish, this should always be the id of the current project
     */
    public CmsPublishList(CmsProject project) {

        this(project, null, false);
    }

    /**
     * Constructs a publish list for a single direct publish resource.<p>
     * 
     * @param directPublishResource a VFS resource to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     */
    public CmsPublishList(CmsResource directPublishResource, boolean publishSiblings) {

        this(null, Collections.singletonList(directPublishResource), publishSiblings);
    }

    /**
     * Constructs a publish list for a list of direct publish resources.<p>
     * 
     * @param directPublishResources a list of <code>{@link CmsResource}</code> instances to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     */
    public CmsPublishList(List directPublishResources, boolean publishSiblings) {

        this(null, directPublishResources, publishSiblings);
    }

    /**
     * Internal constructor for a publish list.<p>
     * 
     * @param project the project to publish
     * @param directPublishResources the list of direct publish resources
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     */
    private CmsPublishList(CmsProject project, List directPublishResources, boolean publishSiblings) {

        m_fileList = new ArrayList();
        m_folderList = new ArrayList();
        m_deletedFolderList = new ArrayList();
        m_publishHistoryId = new CmsUUID();
        m_publishSiblings = publishSiblings;
        m_projectId = (project != null) ? project.getId() : -1;
        if (directPublishResources != null) {
            // reduce list of folders to minimum
            m_directPublishResources = Collections.unmodifiableList(CmsFileUtil.removeRedundantResources(directPublishResources));
        }
    }

    /**
     * Returns a list of folder resources with the given state.<p>
     * 
     * @return a list of folder resources with the desired state
     */
    public List getDeletedFolderList() {

        return m_deletedFolderList;
    }

    /**
     * Returns the list of resources that should be published for a "direct" publish operation.<p>
     * 
     * Will return <code>null</code> if this publish list was not initilaized for a "direct publish" but
     * for a project publish.<p>
     * 
     * @return the list of resources that should be published for a "direct" publish operation, or <code>null</code>
     */
    public List getDirectPublishResources() {

        return m_directPublishResources;
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
     * Returns the id of the project that should be published, or <code>-1</code> if this publish list
     * is initialized for a "direct publish" operation.<p>
     * 
     * @return the id of the project that should be published, or <code>-1</code>
     */
    public int getProjectId() {

        return m_projectId;
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
     * Checks if this is a publish list is used for a "direct publish" operation.<p>
     * 
     * @return true if this is a publish list is used for a "direct publish" operation
     */
    public boolean isDirectPublish() {

        return m_projectId < 0;
    }

    /**
     * Returns <code>true</code> if all siblings of the project resources are to be published.<p>
     *  
     * @return <code>true</code> if all siblings of the project resources are to be publisheds
     */
    public boolean isPublishSiblings() {

        return m_publishSiblings;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("\n[\n");
        if (isDirectPublish()) {
            result.append("direct publish of resources: ").append(m_directPublishResources.toString()).append("\n");
        } else {
            result.append("publish of project: ").append(m_projectId).append("\n");
        }
        result.append("publish history ID: ").append(m_publishHistoryId.toString()).append("\n");
        result.append("resources: ").append(m_fileList.toString()).append("\n");
        result.append("folders: ").append(m_folderList.toString()).append("\n");
        result.append("]\n");
        return result.toString();
    }

    /**
     * Adds a new/changed/deleted Cms file resource to the publish list.<p>
     * 
     * @param resource a new/changed/deleted Cms file resource
     * @throws CmsIllegalArgumentException if the specified resource is not a file or unchanged
     */
    protected void addFile(CmsResource resource) throws CmsIllegalArgumentException {

        // it is essential that this method is only visible within the db package!

        if (resource.isFolder()) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_PUBLISH_NO_CMS_FILE_1,
                resource.getRootPath()));
        }

        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_PUBLISH_UNCHANGED_RESOURCE_1,
                resource.getRootPath()));
        }

        if (!m_fileList.contains(resource)) {
            // only add files not already contained in the list
            // this is required to make sure no siblings are duplicated
            m_fileList.add(resource);
        }
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
            addFile((CmsResource)i.next());
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
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_PUBLISH_NO_FOLDER_1,
                resource.getRootPath()));
        }

        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_PUBLISH_UNCHANGED_RESOURCE_1,
                resource.getRootPath()));
        }

        if (resource.getState() == CmsResource.STATE_DELETED) {
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
            addFolder((CmsResource)i.next());
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

            if (m_deletedFolderList != null) {
                m_deletedFolderList.clear();
            }
            m_deletedFolderList = null;
        } catch (Throwable t) {
            // ignore
        }

        super.finalize();
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
     * Initializes the publish list, ensuring all internal lists are in the right order.<p>
     */
    protected void initialize() {

        if (m_folderList != null) {
            // ensure folders are sorted starting with parent folders
            Collections.sort(m_folderList, CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_fileList != null) {
            // ensure files are sorted starting with files in parent folders
            Collections.sort(m_fileList, CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_deletedFolderList != null) {
            // ensure deleted folders are sorted starting with child folders
            Collections.sort(m_deletedFolderList, CmsResource.COMPARE_ROOT_PATH);
            Collections.reverse(m_deletedFolderList);
        }
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
}