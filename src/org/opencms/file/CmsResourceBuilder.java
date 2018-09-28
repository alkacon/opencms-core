/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.db.CmsResourceState;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.util.CmsUUID;

/**
 * This class allows the developer to build a CmsResource object by filling out individual fields one after the
 * other.
 */
public class CmsResourceBuilder {

    /** The date of the last modification of the content of this resource. */
    protected long m_dateContent = System.currentTimeMillis();

    /** The size of the content. */
    protected int m_length;

    /** The creation date of this resource. */
    private long m_dateCreated;

    /** The expiration date of this resource. */
    private long m_dateExpired;

    /** The date of the last modification of this resource. */
    private long m_dateLastModified;

    /** The release date of this resource. */
    private long m_dateReleased;

    /** The flags of this resource. */
    private int m_flags;

    /** Indicates if this resource is a folder or not. */
    private boolean m_isFolder;

    /** Boolean flag whether the timestamp of this resource was modified by a touch command. */
    private boolean m_isTouched;

    /** The project id where this resource has been last modified in. */
    private CmsUUID m_projectLastModified;

    /** The id of the resource database record. */
    private CmsUUID m_resourceId;

    /** The name of a resource with it's full path from the root folder including the current site root. */
    private String m_rootPath;

    /** The number of links that point to this resource. */
    private int m_siblingCount;

    /** The state of this resource. */
    private CmsResourceState m_state;

    /** The id of the structure database record. */
    private CmsUUID m_structureId;

    /** The m type. */
    private I_CmsResourceType m_type;

    /** The id of the user who created this resource. */
    private CmsUUID m_userCreated;

    /** The id of the user who modified this resource last. */
    private CmsUUID m_userLastModified;

    /** The version number of this resource. */
    private int m_version;

    /**
     * Builds the resource.
     *
     * @return the cms resource
     */
    public CmsResource buildResource() {

        return new CmsResource(
            m_structureId,
            m_resourceId,
            m_rootPath,
            m_type,
            m_flags,
            m_projectLastModified,
            m_state,
            m_dateCreated,
            m_userCreated,
            m_dateLastModified,
            m_userLastModified,
            m_dateReleased,
            m_dateExpired,
            m_length,
            m_flags,
            m_dateContent,
            m_version);
    }

    /**
     * Gets the date content.
     *
     * @return the date content
     */
    public long getDateContent() {

        return m_dateContent;
    }

    /**
     * Gets the date created.
     *
     * @return the date created
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Gets the date expired.
     *
     * @return the date expired
     */
    public long getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Gets the date last modified.
     *
     * @return the date last modified
     */
    public long getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Gets the date released.
     *
     * @return the date released
     */
    public long getDateReleased() {

        return m_dateReleased;
    }

    /**
     * Gets the flags.
     *
     * @return the flags
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public int getLength() {

        return m_length;
    }

    /**
     * Gets the project last modified.
     *
     * @return the project last modified
     */
    public CmsUUID getProjectLastModified() {

        return m_projectLastModified;
    }

    /**
     * Gets the resource id.
     *
     * @return the resource id
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Gets the root path.
     *
     * @return the root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Gets the sibling count.
     *
     * @return the sibling count
     */
    public int getSiblingCount() {

        return m_siblingCount;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public CmsResourceState getState() {

        return m_state;
    }

    /**
     * Gets the structure id.
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public I_CmsResourceType getType() {

        return m_type;
    }

    /**
     * Gets the user created.
     *
     * @return the user created
     */
    public CmsUUID getUserCreated() {

        return m_userCreated;
    }

    /**
     * Gets the user last modified.
     *
     * @return the user last modified
     */
    public CmsUUID getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public int getVersion() {

        return m_version;
    }

    /**
     * Checks if is folder.
     *
     * @return true, if is folder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * Checks if is touched.
     *
     * @return true, if is touched
     */
    public boolean isTouched() {

        return m_isTouched;
    }

    /**
     * Sets the date content.
     *
     * @param dateContent the new date content
     */
    public void setDateContent(long dateContent) {

        m_dateContent = dateContent;
    }

    /**
     * Sets the date created.
     *
     * @param dateCreated the new date created
     */
    public void setDateCreated(long dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * Sets the date expired.
     *
     * @param dateExpired the new date expired
     */
    public void setDateExpired(long dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the date last modified.
     *
     * @param dateLastModified the new date last modified
     */
    public void setDateLastModified(long dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * Sets the date released.
     *
     * @param dateReleased the new date released
     */
    public void setDateReleased(long dateReleased) {

        m_dateReleased = dateReleased;
    }

    /**
     * Sets the flags.
     *
     * @param flags the new flags
     */
    public void setFlags(int flags) {

        m_flags = flags;
    }

    /**
     * Sets the folder.
     *
     * @param isFolder the new folder
     */
    public void setFolder(boolean isFolder) {

        m_isFolder = isFolder;
    }

    /**
     * Sets the length.
     *
     * @param length the new length
     */
    public void setLength(int length) {

        m_length = length;
    }

    /**
     * Sets the project last modified.
     *
     * @param projectLastModified the new project last modified
     */
    public void setProjectLastModified(CmsUUID projectLastModified) {

        m_projectLastModified = projectLastModified;
    }

    /**
     * Sets the resource id.
     *
     * @param resourceId the new resource id
     */
    public void setResourceId(CmsUUID resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * Sets the root path.
     *
     * @param rootPath the new root path
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Sets the sibling count.
     *
     * @param siblingCount the new sibling count
     */
    public void setSiblingCount(int siblingCount) {

        m_siblingCount = siblingCount;
    }

    /**
     * Sets the state.
     *
     * @param state the new state
     */
    public void setState(CmsResourceState state) {

        m_state = state;
    }

    /**
     * Sets the structure id.
     *
     * @param structureId the new structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the touched.
     *
     * @param isTouched the new touched
     */
    public void setTouched(boolean isTouched) {

        m_isTouched = isTouched;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(I_CmsResourceType type) {

        m_type = type;
    }

    /**
     * Sets the user created.
     *
     * @param userCreated the new user created
     */
    public void setUserCreated(CmsUUID userCreated) {

        m_userCreated = userCreated;
    }

    /**
     * Sets the user last modified.
     *
     * @param userLastModified the new user last modified
     */
    public void setUserLastModified(CmsUUID userLastModified) {

        m_userLastModified = userLastModified;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(int version) {

        m_version = version;
    }

}
