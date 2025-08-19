/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.util.CmsUUID;

/**
 * Simple immutable class for storing a custom content folder configuration.
 *
 * <p>There are two possibilities: Either a custom content folder should be used (then the corresponding instance
 * of this class stores the folder id), or the default content folder should be used (in which case the folder id is null).
 */
public class CmsContentFolderOption {

    /** The content folder id (may be null). */
    private CmsUUID m_folderId;

    /**
     * Creates a new instance.
     *
     *
     * @param contentFolderId the content folder id
     */
    public CmsContentFolderOption(CmsUUID contentFolderId) {

        m_folderId = contentFolderId;
    }

    /**
     * Gets the content folder id.
     *
     * <p>The result may be null.
     *
     * @return the content folder id
     */
    public CmsUUID getFolderId() {

        return m_folderId;
    }

    /**
     * Returns true if this represents the option to use the standard content folder.
     *
     * <p>This is currently implemented as the folder id being null.
     *
     * @return true if the default content folder should be used
     */
    public boolean isDefault() {

        return m_folderId == null;
    }

}
