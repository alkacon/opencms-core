/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The Class CmsResourceTypeHelpBean.
 */
public class CmsResourceTypeHelpBean implements IsSerializable {

    /** root Path to help document */
    public static final String HELP_DOCUMENTS_VFS_PATH = "/system/modules/org.opencms.ade.help/documents/";
    /** Path to document for common help */
    public static final String HELP_DOCUMENTS_COMMON_VFS_PATH = HELP_DOCUMENTS_VFS_PATH + "common/";
    /** Path to document for start help */
    public static final String HELP_DOCUMENTS_START_VFS_PATH = HELP_DOCUMENTS_VFS_PATH + "start/";
    /** Path to document for editor help */
    public static final String HELP_DOCUMENTS_EDITOR_VFS_PATH = HELP_DOCUMENTS_VFS_PATH + "editor/";
    /** The Title node in ADE Help document */
    public static final String HELP_DOCUMENT_TITLE_NODE = "Title";
    /** The Text node in ADE Help document */
    public static final String HELP_DOCUMENT_TEXT_NODE = "Text";

    /** Resource type. */
    private String m_resourceType;

    /** Structure id of the resource. */
    private CmsUUID m_structureId;

    /** Title property. */
    private String m_title;

    /** RichText content. */
    private String m_content;

    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {

        return m_content;
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the structure id of the resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the content.
     *
     * @param content the new content
     */
    public void setContent(String content) {

        m_content = content;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the structure id of the resource.<p>
     *
     * @param structureId the structure id of the resource
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
