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

package org.opencms.ade.galleries.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents information about whether a gallery folder should use a custom upload action.
 */
public class CmsGalleryActionInfo implements IsSerializable {

    /** The structure id of the folder. */
    private CmsUUID m_structureId;

    /** The upload action. */
    private String m_uploadAction;

    /**
     * Creates a new instance.
     *
     * @param structureId the structure id of the folder
     * @param uploadAction the upload action
     */
    public CmsGalleryActionInfo(CmsUUID structureId, String uploadAction) {

        super();
        m_structureId = structureId;
        m_uploadAction = uploadAction;
    }

    /**
     * Default constructor for serialization.
     */
    protected CmsGalleryActionInfo() {

        // do nothing.
    }

    /**
     * Gets the structure id of the folder.
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the upload action (may be null).
     *
     * @return the upload action
     */
    public String getUploadAction() {

        return m_uploadAction;
    }

}
