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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.util.CmsUUID;

/**
 * A file resource in the OpenCms VFS.<p>
 *
 * A file resource is a CmsResource that contains an additional byte
 *  array of binary data, which is the file content.<p>
 *
 * A file object is not allowed to have sub-resources.<p>
 *
 * @since 6.0.0
 */
public class CmsFile extends CmsResource {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -5201022482708455620L;

    /** The content of this file. */
    private byte[] m_fileContent;

    /**
     * Constructor, creates a new file Object from the given resource with
     * an empty byte array as file content, if the resource does not
     * implement a file.<p>
     *
     * @param resource the base resource object to create a file from
     */
    public CmsFile(CmsResource resource) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getRootPath(),
            resource.getTypeId(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getDateReleased(),
            resource.getDateExpired(),
            resource.getSiblingCount(),
            resource.getLength(),
            resource.getDateContent(),
            resource.getVersion(),
            // if the resource already is a file, keep contents that might have been already read
            resource instanceof CmsFile ? ((CmsFile)resource).getContents() : null);
    }

    /**
     * Constructor, creates a new file object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param path the filename of this resource
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param linkCount the count of all siblings of this resource
     * @param length the size of the file content of this resource
     * @param dateContent the date of the last modification of the content of this resource
     * @param version the version number of this resource
     * @param content the binary content data of this file
     */
    public CmsFile(
        CmsUUID structureId,
        CmsUUID resourceId,
        String path,
        int type,
        int flags,
        CmsUUID projectId,
        CmsResourceState state,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        long dateReleased,
        long dateExpired,
        int linkCount,
        int length,
        long dateContent,
        int version,
        byte[] content) {

        super(
            structureId,
            resourceId,
            path,
            type,
            false,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            length,
            dateContent,
            version);

        m_fileContent = content;
        if (m_fileContent == null) {
            m_fileContent = new byte[0];
        }
    }

    /**
     * Returns a clone of this Objects instance.<p>
     *
     * @return a clone of this instance
     */
    @Override
    public Object clone() {

        byte[] newContent = new byte[getContents().length];
        System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

        CmsFile clone = new CmsFile(
            getStructureId(),
            getResourceId(),
            getRootPath(),
            getTypeId(),
            getFlags(),
            getProjectLastModified(),
            getState(),
            getDateCreated(),
            getUserCreated(),
            getDateLastModified(),
            getUserLastModified(),
            getDateReleased(),
            getDateExpired(),
            getSiblingCount(),
            getLength(),
            getDateContent(),
            getVersion(),
            newContent);

        if (isTouched()) {
            clone.setDateLastModified(getDateLastModified());
        }

        return clone;
    }

    /**
     * Returns the content of this file.<p>
     *
     * @return the content of this file
     */
    public byte[] getContents() {

        return m_fileContent;
    }

    /**
     * @see org.opencms.file.CmsResource#getLength()
     */
    @Override
    public int getLength() {

        return m_length;
    }

    /**
     * @see org.opencms.file.CmsResource#isFile()
     */
    @Override
    public boolean isFile() {

        return true;
    }

    /**
     * @see org.opencms.file.CmsResource#isFolder()
     */
    @Override
    public boolean isFolder() {

        return false;
    }

    /**
     * @see org.opencms.file.CmsResource#isTemporaryFile()
     */
    @Override
    public boolean isTemporaryFile() {

        return ((getFlags() & CmsResource.FLAG_TEMPFILE) > 0) || isTemporaryFileName(getName());
    }

    /**
     * Sets the contents of this file.<p>
     *
     * This will also set the date content, but only if the content is already set.<p>
     *
     * @param value the content of this file
     */
    public void setContents(byte[] value) {

        if (value == null) {
            value = new byte[] {};
        }
        long dateContent = System.currentTimeMillis();
        if ((m_fileContent == null) || (m_fileContent.length == 0)) {
            dateContent = m_dateContent;
        }
        m_fileContent = new byte[value.length];
        System.arraycopy(value, 0, m_fileContent, 0, value.length);

        if (m_fileContent.length > 0) {
            m_length = m_fileContent.length;
        } else {
            m_length = 0;
        }
        m_dateContent = dateContent;
    }
}