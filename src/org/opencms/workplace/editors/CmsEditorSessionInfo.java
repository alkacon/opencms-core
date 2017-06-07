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

package org.opencms.workplace.editors;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * Stores editor session data.<p>
 *
 * @since 8.0.
 */
public class CmsEditorSessionInfo {

    /** The editor session info key prefix. */
    public static final String PREFIX_EDITOR_SESSION_INFO = "editorSessionInfo";

    /** The back link for closing the editor. */
    private String m_backLink;

    /** Flag indicating if in direct edit mode. */
    private boolean m_directEdit;

    /** The id of the edited resource. */
    private CmsUUID m_editedStructureId;

    /** The locale currently edited. */
    private Locale m_elementLocale;

    /**
     * Constructor.<p>
     *
     * @param editedStructureId the id of the edited resource
     */
    public CmsEditorSessionInfo(CmsUUID editedStructureId) {

        m_editedStructureId = editedStructureId;
    }

    /**
    * Returns the session info key for the bean.<p>
    *
    * @param editedResource the edited resource
    *
    * @return the session info key for the bean
    */
    protected static String getEditorSessionInfoKey(CmsResource editedResource) {

        return PREFIX_EDITOR_SESSION_INFO + editedResource.getStructureId().getStringValue();
    }

    /**
     * Returns the back link for closing the editor.<p>
     *
     * @return the back link for closing the editor
     */
    public String getBackLink() {

        return m_backLink;
    }

    /**
     * Returns the id of the edited resource.<p>
     *
     * @return the id of the edited resource
     */
    public CmsUUID getEditedStructureId() {

        return m_editedStructureId;
    }

    /**
        * Returns the session info key for the bean.<p>
        *
        * @return the session info key for the bean
        */
    public String getEditorSessionInfoKey() {

        return PREFIX_EDITOR_SESSION_INFO + m_editedStructureId.getStringValue();
    }

    /**
     * Returns the element locale currently edited.<p>
     *
     * @return the element locale
     */
    public Locale getElementLocale() {

        return m_elementLocale;
    }

    /**
     * Returns if in direct edit mode.<p>
     *
     * @return <code>true</code> if in direct edit mode
     */
    public boolean isDirectEdit() {

        return m_directEdit;
    }

    /**
     * Sets the back link for closing the editor.<p>
     *
     * @param backLink the back link for closing the editor to set
     */
    public void setBackLink(String backLink) {

        m_backLink = backLink;
    }

    /**
     * Sets the flag indicating if in direct edit mode.<p>
     *
     * @param directEdit the flag indicating if in direct edit mode
     */
    public void setDirectEdit(boolean directEdit) {

        m_directEdit = directEdit;
    }

    /**
     * Sets the id of the edited resource.<p>
     *
     * @param editedStructureId the id of the edited resource to set
     */
    public void setEditedStructureId(CmsUUID editedStructureId) {

        m_editedStructureId = editedStructureId;
    }

    /**
     * Sets the element locale currently edited.<p>
     *
     * @param elementLocale the element locale to set
     */
    public void setElementLocale(Locale elementLocale) {

        m_elementLocale = elementLocale;
    }

}
